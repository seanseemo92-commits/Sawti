package com.sawti.data

import android.content.Context
import androidx.room.withTransaction
import com.sawti.data.db.AppDatabase
import com.sawti.data.db.CategoryEntity
import com.sawti.data.db.SeedExtras
import com.sawti.data.db.WordItemEntity
import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import com.sawti.domain.model.Category
import com.sawti.domain.model.DialectCatalog
import com.sawti.domain.model.TtsEndings
import com.sawti.domain.model.PhonemicCue
import com.sawti.domain.model.CategoryIds
import com.sawti.domain.model.RegionalDishesCatalog
import com.sawti.domain.model.ArabicDialect
import com.sawti.domain.model.CustomCategoryRules
import com.sawti.domain.repository.WordRepository
import java.io.File
import java.io.InputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class RoomWordRepository(
    private val db: AppDatabase,
    context: Context,
    private val settingsRepository: SettingsRepository,
) : WordRepository {

    private val dao = db.wordDao()
    private val filesDir = context.applicationContext.filesDir
    private val prefs = context.applicationContext.getSharedPreferences("seed", Context.MODE_PRIVATE)

    override fun observeCategories(): Flow<List<Category>> =
        combine(dao.observeCategories(), dao.observeItems(), settingsRepository.state) { categories, items, settings ->
            val byCategory = items.groupBy { it.categoryId }
            categories.map { c ->
                val regionalCuisine = c.id == RegionalDishesCatalog.CATEGORY_ID
                val visibleItems = byCategory[c.id].orEmpty().filter { item ->
                    !regionalCuisine || RegionalDishesCatalog.visibleForDialect(item.subgroup, settings.dialect)
                }
                Category(
                    id = c.id,
                    title = if (regionalCuisine) RegionalDishesCatalog.forDialect(settings.dialect).title
                            else DialectCatalog.text(c.title, settings.dialect),
                    palette = c.palette,
                    previewOnTap = c.previewOnTap,
                    sortOrder = c.sortOrder,
                    // A regional dish has a carefully written TTS prompt; never run the
                    // generic display-label translator over a customized dish.
                    items = visibleItems.map { item ->
                        if (regionalCuisine) item.toDomain()
                        else DialectCatalog.localize(item.toDomain(), settings.dialect)
                    },
                )
            }
        }

    override suspend fun seedOrUpdateDefaults() {
        if (dao.categoryCount() == 0) {
            db.withTransaction {
                dao.insertCategories(WordSeed.categoryEntities())
                dao.insertItems(WordSeed.itemEntities() + regionalSeedItems())
            }
            prefs.edit().putInt(KEY_SEED_VERSION, WordSeed.VERSION).apply()
        } else {
            val previous = prefs.getInt(KEY_SEED_VERSION, 0)
            if (previous < WordSeed.VERSION) {
                if (previous >= 15) {
                    // v15/v16 -> v17: insert missing regional cuisine words ONLY. No sort, prompt, TTS,
                    // photo, recording, favourite or other existing value is updated.
                    topUpCuisineOnly()
                } else {
                    topUpSeed(previous)
                }
                prefs.edit().putInt(KEY_SEED_VERSION, WordSeed.VERSION).apply()
            }
        }
        // Restore and upgrade compatibility: assigning a key never overwrites personal data.
        backfillSemanticImageKeys()
    }

    /**
     * Assign the shared semantic asset to each existing built-in Room ID.
     * Match unchanged names first, then legacy renames, then unique category/subgroup/
     * position with matching default emoji for caregiver-renamed built-in words.
     * Regional cuisine, custom people and custom cards remain untouched.
     */
    private suspend fun backfillSemanticImageKeys() {
        db.withTransaction {
            val defaults = WordSeed.itemEntities().filter { it.semanticKey != null }
            val byName = defaults.associateBy { Triple(it.categoryId, it.subgroup, it.displayText) }
            val byPosition = defaults.groupBy { Triple(it.categoryId, it.subgroup, it.sortOrder) }
            val historical = WordSeed.renames.mapNotNull { (category, old, current) ->
                val target = defaults.firstOrNull { it.categoryId == category && it.displayText == current }
                target?.semanticKey?.let { Triple(category, "", old) to it }
            }.toMap()
            for (item in dao.itemsWithoutSemanticKeys()) {
                val signature = Triple(item.categoryId, item.subgroup, item.displayText)
                val key = byName[signature]?.semanticKey
                    ?: historical[signature]
                    ?: byPosition[Triple(item.categoryId, item.subgroup, item.sortOrder)]
                        ?.singleOrNull()
                        ?.takeIf { it.emoji.isNotBlank() && it.emoji == item.emoji }
                        ?.semanticKey
                if (key != null) dao.setSemanticKeyIfMissing(item.id, key)
            }
        }
    }

    /** Additional country-specific dishes have independent keys/images, not Libyan ones. */
    private fun regionalSeedItems(): List<WordItemEntity> =
        RegionalDishesCatalog.additionalDialects().mapIndexed { index, (dialect, dish) ->
            WordItemEntity(
                categoryId = RegionalDishesCatalog.CATEGORY_ID,
                subgroup = RegionalDishesCatalog.subgroupFor(dialect),
                displayText = dish.displayText,
                ttsPrompt = dish.ttsPrompt,
                phonemicCue = dish.phonemicCue,
                sentencePrompt = dish.sentencePrompt,
                emoji = dish.emoji,
                sortOrder = index % 15,
            )
        }

    /**
     * Insert-only update. Existing personalized cards and recordings are never changed,
     * and old Libyan cards stay with their original IDs even when changing dialects.
     */
    private suspend fun topUpCuisineOnly() {
        db.withTransaction {
            val all = dao.itemKeys().map { Triple(it.categoryId, it.subgroup, it.displayText) }.toSet()
            val newDefaults = WordSeed.itemEntities().filter {
                it.categoryId == RegionalDishesCatalog.CATEGORY_ID &&
                    it.displayText in RegionalDishesCatalog.forDialect(ArabicDialect.LIBYAN)
                        .dishes.map { d -> d.displayText }.toSet()
            } + regionalSeedItems()
            val missing = newDefaults.filter {
                Triple(it.categoryId, it.subgroup, it.displayText) !in all
            }
            if (missing.isNotEmpty()) dao.insertItems(missing)
        }
    }

    /**
     * Brings an existing install up to the current vocabulary without touching user data:
     * photos, recordings, phone numbers and favourites are never deleted. New words are added,
     * missing sentence prompts are filled in (never overwriting an edited one), and old built-in
     * words that were never customised and are no longer in the list are removed.
     * Default favourites are applied only by the update that introduces them. Runs once per version.
     */
    private suspend fun topUpSeed(previousVersion: Int) {
        db.withTransaction {
            if (previousVersion < 15) {
                // Update only old built-in placeholders. Photos, voice, phone, IDs and
                // personalized sentence hints remain untouched.
                dao.correctMelonPlaceholder()
                dao.clearLegacyActionPlaceholder()
            }
            val seedCategories = WordSeed.categoryEntities()
            val existingCategories = dao.categoryIds().toSet()
            dao.insertCategories(seedCategories.filter { it.id !in existingCategories })
            seedCategories.filter { it.id in existingCategories }
                .forEach { dao.updateCategory(it.id, it.title, it.palette, it.previewOnTap, it.sortOrder) }

            val seedItems = WordSeed.itemEntities() + regionalSeedItems()
            val seedKeys = seedItems.map { Triple(it.categoryId, it.subgroup, it.displayText) }.toSet()

            // 1. old default words that now have a Libyan name are renamed in place (photo/audio/phone kept)
            for ((category, oldName, newName) in WordSeed.renames) {
                val target = seedItems.firstOrNull { it.categoryId == category && it.displayText == newName } ?: continue
                val current = dao.itemKeys()
                val alreadyThere = current.any { it.categoryId == category && it.displayText == newName }
                val old = current.firstOrNull { it.categoryId == category && it.displayText == oldName }
                if (!alreadyThere && old != null) {
                    dao.renameItem(old.id, target.displayText, target.ttsPrompt, target.phonemicCue, target.sentencePrompt)
                }
            }
            val existing = dao.itemKeys()

            // Never guess that a card is disposable just because its picture and audio
            // are empty: the caregiver may have edited phone, favourite, cue or sentence.
            // Explicit deletion belongs in caregiver actions, not in automatic seeding.
            val obsolete: List<com.sawti.data.db.ItemKey> = emptyList()

            // natural spoken endings (هْ rule) for every remaining word, without touching anything else
            existing.filter { e -> obsolete.none { it.id == e.id } }.forEach { e ->
                val fixed = TtsEndings.normalize(e.ttsPrompt)
                if (fixed != e.ttsPrompt) dao.updateTts(e.id, fixed)
            }

            val kept = existing.filter { e -> obsolete.none { it.id == e.id } }
                .associateBy { Triple(it.categoryId, it.subgroup, it.displayText) }
            val applyFavorites = previousVersion < SeedExtras.FAVORITES_SINCE_VERSION
            val missing = mutableListOf<WordItemEntity>()
            for (item in seedItems) {
                val match = kept[Triple(item.categoryId, item.subgroup, item.displayText)]
                if (match == null) {
                    missing += if (applyFavorites) item else item.copy(isFavorite = false)
                } else {
                    dao.updateSort(match.id, item.sortOrder)
                    // words migrated from the first app version only have the plain name as TTS text
                    if (match.ttsPrompt == match.displayText && item.ttsPrompt != match.ttsPrompt) {
                        dao.updateTtsAndCue(match.id, item.ttsPrompt, item.phonemicCue)
                    }
                    item.sentencePrompt?.let { dao.fillSentencePrompt(match.id, it) }
                    if (applyFavorites && item.isFavorite) dao.setFavorite(match.id, true)
                }
            }
            if (missing.isNotEmpty()) dao.insertItems(missing)
        }
    }

    private fun isCustomId(id: String) = CustomCategoryRules.canManage(id)

    override suspend fun addCustomCategory(title: String, palette: Int, sortOrder: Int): String {
        val name = title.trim().take(70)
        require(name.isNotBlank()) { "اسم القسم مطلوب" }
        require(CustomCategoryRules.validPalette(palette) && CustomCategoryRules.validSortOrder(sortOrder)) { "إعدادات القسم غير صالحة" }
        val id = "custom_" + UUID.randomUUID().toString()
        db.withTransaction {
            dao.insertCategory(CategoryEntity(id, name, palette, false, sortOrder))
        }
        return id
    }

    override suspend fun editCustomCategory(id: String, title: String, palette: Int, sortOrder: Int) {
        require(isCustomId(id)) { "الأقسام الافتراضية غير قابلة للتعديل" }
        val name = title.trim().take(70)
        require(name.isNotBlank() && CustomCategoryRules.validPalette(palette) && CustomCategoryRules.validSortOrder(sortOrder))
        db.withTransaction {
            require(dao.categoryById(id) != null) { "القسم غير موجود" }
            require(dao.editCustomCategory(id, name, palette, sortOrder) == 1)
        }
    }

    override suspend fun deleteEmptyCustomCategory(id: String) {
        require(isCustomId(id)) { "حذف الأقسام الافتراضية ممنوع" }
        db.withTransaction {
            require(CustomCategoryRules.canDelete(id, dao.countItemsInCategory(id))) {
                "القسم يحتوي بطاقات. انقلها إلى قسم مخصص آخر قبل الحذف."
            }
            require(dao.deleteEmptyCustomCategory(id) == 1) { "تعذر حذف القسم" }
        }
    }

    override suspend fun addCustomCard(categoryId: String, displayText: String, spokenText: String): Long {
        require(isCustomId(categoryId)) { "يمكن إضافة بطاقات مخصصة إلى الأقسام المخصصة فقط" }
        val display = displayText.trim().take(100)
        require(display.isNotBlank()) { "نص البطاقة مطلوب" }
        val spoken = spokenText.trim().ifBlank { display }.take(250)
        return db.withTransaction {
            require(dao.categoryById(categoryId) != null) { "القسم غير موجود" }
            dao.insertItem(WordItemEntity(
                categoryId = categoryId, displayText = display, ttsPrompt = spoken,
                phonemicCue = PhonemicCue.derive(spoken), sortOrder = 10_000,
            ))
        }
    }

    override suspend fun moveCardToCustomCategory(itemId: Long, destinationId: String) {
        require(isCustomId(destinationId)) { "الوجهة يجب أن تكون قسمًا مخصصًا" }
        db.withTransaction {
            require(dao.categoryById(destinationId) != null) { "قسم الوجهة غير موجود" }
            val item = dao.wordById(itemId) ?: error("البطاقة غير موجودة")
            require(item.categoryId != CategoryIds.FAMILY ||
                item.subgroup != com.sawti.domain.model.FamilyRelationships.RELATIVES) {
                "بطاقات صلات العائلة الافتراضية لا يمكن نقلها"
            }
            dao.moveWord(itemId, destinationId)
        }
    }

    /** Permanently removes only a caregiver-selected card from a custom category. */
    override suspend fun deleteCustomCard(itemId: Long) {
        val item = db.withTransaction {
            val current = dao.wordById(itemId) ?: error("البطاقة غير موجودة")
            require(isCustomId(current.categoryId)) { "لا يمكن حذف بطاقة من قسم افتراضي" }
            dao.deleteItem(itemId)
            current
        }
        // Backups may contain rows with shared media references. Only remove an app-private
        // attachment if NO other word still refers to it.
        listOfNotNull(item.photoPath, item.audioPath).distinct().forEach { path ->
            if (dao.mediaReferenceCount(path) == 0) {
                withContext(Dispatchers.IO) { deletePrivateMedia(path) }
            }
        }
    }

    /** New family cards use the ordinary word/media model; no schema duplication. */
    override suspend fun addFamilyMember(name: String, group: String, phone: String): Long {
        val cleanName = name.trim().take(100)
        require(cleanName.isNotBlank()) { "اسم الشخص مطلوب" }
        require(com.sawti.domain.model.FamilyRelationships.validPeopleGroup(group)) {
            "يجب اختيار قسم لأفراد العائلة"
        }
        return dao.insertItem(
            WordItemEntity(
                categoryId = CategoryIds.FAMILY,
                subgroup = group.trim().take(60),
                displayText = cleanName,
                ttsPrompt = cleanName,
                phonemicCue = PhonemicCue.derive(cleanName),
                phone = phone.trim().take(40),
                sortOrder = 10_000,
            ),
        )
    }

    override suspend fun updateFamilyGroup(id: Long, group: String) {
        val old = dao.familyItemById(id) ?: return
        if (old.subgroup == com.sawti.domain.model.FamilyRelationships.RELATIVES) return
        if (group.isNotBlank() && group != com.sawti.domain.model.FamilyRelationships.RELATIVES) {
            dao.updateFamilyGroup(id, group.trim().take(60))
        }
    }

    /** Permanently deletes only a caregiver-selected custom family card and its OWN media. */
    override suspend fun deleteFamilyMember(id: Long) {
        val old = dao.familyItemById(id) ?: return
        // Preset relationship words are not a removable person's card.
        if (old.subgroup == com.sawti.domain.model.FamilyRelationships.RELATIVES) return
        db.withTransaction { dao.deleteItem(id) }
        // Never delete files beyond app-private storage (legacy rows may contain an external URI).
        listOfNotNull(old.photoPath, old.audioPath).forEach { raw ->
            withContext(Dispatchers.IO) {
                runCatching {
                    val file = File(raw).canonicalFile
                    val base = filesDir.canonicalFile
                    if (file.parentFile == File(base, "photos") ||
                        file.parentFile == File(base, "custom_audio") ||
                        file.parentFile == File(base, "recordings")
                    ) file.delete()
                }
            }
        }
    }

    override suspend fun updateDetails(
        id: Long, displayText: String, ttsPrompt: String, phonemicCue: String,
        sentencePrompt: String?, phone: String,
    ) {
        dao.updateDetails(id, displayText, ttsPrompt, phonemicCue, sentencePrompt, phone)
    }

    override suspend fun setFavorite(id: Long, favorite: Boolean) {
        dao.setFavorite(id, favorite)
    }

    /** Copies the picked image into app storage so it survives even if the gallery item moves. */
    override suspend fun setPhoto(id: Long, image: InputStream) {
        withContext(Dispatchers.IO) {
            val dir = File(filesDir, "photos").apply { require(isDirectory || mkdirs()) }
            val target = File(dir, "word_${id}_${System.nanoTime()}.jpg")
            // Stream to a bounded temporary file, sample-decode, rotate using EXIF, and
            // store only a compressed app-private JPEG. Old media stays until DB succeeds.
            SafePhotoProcessor.import(image, target)
            try {
                val old = dao.photoPath(id)
                dao.updatePhoto(id, target.absolutePath)
                deletePrivateMedia(old)
            } catch (e: Exception) {
                target.delete()
                throw e
            }
        }
    }

    override suspend fun clearPhoto(id: Long) {
        withContext(Dispatchers.IO) {
            deletePrivateMedia(dao.photoPath(id))
            dao.updatePhoto(id, null)
        }
    }

    override suspend fun setAudio(id: Long, file: File) {
        withContext(Dispatchers.IO) {
            val old = dao.audioPath(id)
            dao.updateAudio(id, file.absolutePath)
            if (old != file.absolutePath) deletePrivateMedia(old)
        }
    }

    /** Copies a picked audio file into filesDir/custom_audio/ and stores its absolute path. */
    override suspend fun importAudio(id: Long, input: InputStream, extension: String) {
        withContext(Dispatchers.IO) {
            SafeMediaImport.checkAudioExtension(extension)
            val dir = File(filesDir, "custom_audio").apply { require(isDirectory || mkdirs()) }
            val target = File(dir, "word_${id}_${System.nanoTime()}.$extension")
            SafeMediaImport.copy(input, target, SafeMediaImport.MAX_AUDIO_BYTES)
            try {
                val old = dao.audioPath(id)
                dao.updateAudio(id, target.absolutePath)
                if (old != target.absolutePath) deletePrivateMedia(old)
            } catch (e: Exception) {
                target.delete()
                throw e
            }
        }
    }

    override suspend fun clearAudio(id: Long) {
        withContext(Dispatchers.IO) {
            deletePrivateMedia(dao.audioPath(id))
            dao.updateAudio(id, null)
        }
    }

    /** Never delete arbitrary paths from a database imported through a user-selected backup. */
    private fun deletePrivateMedia(raw: String?) {
        if (raw.isNullOrBlank()) return
        runCatching {
            val file = File(raw).canonicalFile
            val root = filesDir.canonicalFile
            val allowed = setOf("photos", "custom_audio", "recordings")
            if (file.isFile && file.parentFile?.parentFile == root &&
                file.parentFile?.name in allowed) {
                file.delete()
            }
        }
    }

    private companion object {
        const val KEY_SEED_VERSION = "seed_version"
    }
}
