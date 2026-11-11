package com.sawti.data

import android.content.Context
import androidx.room.withTransaction
import com.sawti.data.db.AppDatabase
import com.sawti.data.db.CategoryEntity
import com.sawti.data.db.WordItemEntity
import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import com.sawti.domain.model.Category
import com.sawti.domain.model.CardDetails
import com.sawti.domain.model.FamilyRelationships
import com.sawti.domain.model.DialectCatalog
import com.sawti.domain.model.PhonemicCue
import com.sawti.domain.model.CategoryIds
import com.sawti.domain.model.RegionalDishesCatalog
import com.sawti.domain.model.CustomCategoryRules
import com.sawti.domain.repository.WordRepository
import java.io.File
import java.io.InputStream
import java.io.FilterInputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
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
                            else c.title,
                    palette = c.palette,
                    previewOnTap = c.previewOnTap,
                    sortOrder = c.sortOrder,
                    items = visibleItems.map { item ->
                        DialectCatalog.localize(item.toDomain(), settings.dialect)
                    },
                )
            }.sortedWith(
                compareBy<Category> {
                    // Display-only priority: Quran is the separate full-width hero card,
                    // then Greetings and Family share the first phone grid row.
                    when (it.id) {
                        "greetings" -> 0
                        CategoryIds.FAMILY -> 1
                        else -> 2
                    }
                }.thenBy { it.sortOrder },
            )
        }

    override suspend fun seedOrUpdateDefaults() = db.storage.cleanupIfActive {
        seedOrUpdateDefaultsUnlocked()
    }

    /** Insert by immutable concept identity. Renamed/customized rows are never rewritten.
     * One transaction under the shared storage gate also makes post-restore seeding idempotent.
     * This is the new content foundation, not an upgrade path for historical image-key seeds.
     */
    private suspend fun seedOrUpdateDefaultsUnlocked() {
        db.withTransaction {
            val categories = WordSeed.categoryEntities()
            val existingCategories = dao.categoryIds().toSet()
            dao.insertCategories(categories.filter { it.id !in existingCategories })
            val defaults = WordSeed.itemEntities()
            val keys = defaults.map { requireNotNull(it.semanticKey) }
            check(keys.size == keys.toSet().size) { "معرّف مفهوم مكرر في المفردات الأساسية" }
            val existingKeys = dao.semanticKeys().toSet()
            val missing = defaults.filter { it.semanticKey !in existingKeys }
            if (missing.isNotEmpty()) dao.insertItems(missing)
        }
    }

    private fun isCustomId(id: String) = CustomCategoryRules.canManage(id)

    override suspend fun addCustomCategory(title: String, palette: Int, sortOrder: Int): String = db.storage.access {
        addCustomCategoryUnlocked(title, palette, sortOrder)
    }

    private suspend fun addCustomCategoryUnlocked(title: String, palette: Int, sortOrder: Int): String {
        val name = title.trim().take(70)
        require(name.isNotBlank()) { "اسم القسم مطلوب" }
        require(CustomCategoryRules.validPalette(palette) && CustomCategoryRules.validSortOrder(sortOrder)) { "إعدادات القسم غير صالحة" }
        val id = "custom_" + UUID.randomUUID().toString()
        db.withTransaction {
            dao.insertCategory(CategoryEntity(id, name, palette, false, sortOrder))
        }
        return id
    }

    override suspend fun editCustomCategory(id: String, title: String, palette: Int, sortOrder: Int) = db.storage.access {
        editCustomCategoryUnlocked(id, title, palette, sortOrder)
    }

    private suspend fun editCustomCategoryUnlocked(id: String, title: String, palette: Int, sortOrder: Int) {
        require(isCustomId(id)) { "الأقسام الافتراضية غير قابلة للتعديل" }
        val name = title.trim().take(70)
        require(name.isNotBlank() && CustomCategoryRules.validPalette(palette) && CustomCategoryRules.validSortOrder(sortOrder))
        db.withTransaction {
            require(dao.categoryById(id) != null) { "القسم غير موجود" }
            require(dao.editCustomCategory(id, name, palette, sortOrder) == 1)
        }
    }

    override suspend fun deleteEmptyCustomCategory(id: String) = db.storage.access {
        deleteEmptyCustomCategoryUnlocked(id)
    }

    private suspend fun deleteEmptyCustomCategoryUnlocked(id: String) {
        require(isCustomId(id)) { "حذف الأقسام الافتراضية ممنوع" }
        db.withTransaction {
            require(CustomCategoryRules.canDelete(id, dao.countItemsInCategory(id))) {
                "القسم يحتوي بطاقات. انقلها إلى قسم مخصص آخر قبل الحذف."
            }
            require(dao.deleteEmptyCustomCategory(id) == 1) { "تعذر حذف القسم" }
        }
    }

    override suspend fun addCustomCard(categoryId: String, displayText: String, spokenText: String): Long = db.storage.access {
        addCustomCardUnlocked(categoryId, displayText, spokenText)
    }

    private suspend fun addCustomCardUnlocked(categoryId: String, displayText: String, spokenText: String): Long {
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

    override suspend fun moveCardToCustomCategory(itemId: Long, destinationId: String) = db.storage.access {
        moveCardToCustomCategoryUnlocked(itemId, destinationId)
    }

    private suspend fun moveCardToCustomCategoryUnlocked(itemId: Long, destinationId: String) {
        require(isCustomId(destinationId)) { "الوجهة يجب أن تكون قسمًا مخصصًا" }
        db.withTransaction {
            require(dao.categoryById(destinationId) != null) { "قسم الوجهة غير موجود" }
            val item = dao.wordById(itemId) ?: error("البطاقة غير موجودة")
            require(isCustomId(item.categoryId)) {
                "يمكن نقل البطاقات التي أنشأها المستخدم داخل الأقسام المخصصة فقط"
            }
            dao.moveWord(itemId, destinationId)
        }
    }

    /** Permanently removes only a caregiver-selected card from a custom category. */
    override suspend fun deleteCustomCard(itemId: Long) = withMediaMutation { retired ->
        db.withTransaction {
            val current = dao.wordById(itemId) ?: error("البطاقة غير موجودة")
            require(isCustomId(current.categoryId)) { "لا يمكن حذف بطاقة من قسم افتراضي" }
            retired.addAll(listOfNotNull(current.photoPath, current.audioPath))
            check(dao.deleteItem(itemId) == 1)
        }
    }

    /** New family cards use the ordinary word/media model; no schema duplication. */
    override suspend fun addFamilyMember(name: String, group: String, phone: String): Long = db.storage.access {
        addFamilyMemberUnlocked(name, group, phone)
    }

    private suspend fun addFamilyMemberUnlocked(name: String, group: String, phone: String): Long {
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

    override suspend fun updateFamilyGroup(id: Long, group: String) = db.storage.access {
        updateFamilyGroupUnlocked(id, group)
    }

    private suspend fun updateFamilyGroupUnlocked(id: Long, group: String) {
        val old = dao.familyItemById(id) ?: return
        if (old.subgroup == com.sawti.domain.model.FamilyRelationships.RELATIVES) return
        if (group.isNotBlank() && group != com.sawti.domain.model.FamilyRelationships.RELATIVES) {
            dao.updateFamilyGroup(id, group.trim().take(60))
        }
    }

    /** Permanently deletes only a caregiver-selected custom family card and its OWN media. */
    override suspend fun deleteFamilyMember(id: Long) = withMediaMutation { retired ->
        db.withTransaction {
            val old = dao.familyItemById(id) ?: return@withTransaction
            // Preset relationship words are not a removable person's card.
            if (old.subgroup == com.sawti.domain.model.FamilyRelationships.RELATIVES) return@withTransaction
            retired.addAll(listOfNotNull(old.photoPath, old.audioPath))
            check(dao.deleteItem(id) == 1)
        }
    }

    override suspend fun updateDetails(
        id: Long, displayText: String, ttsPrompt: String, phonemicCue: String,
        sentencePrompt: String?, phone: String, baseline: CardDetails?, group: String?,
    ) = db.storage.access {
        db.withTransaction {
            val current = dao.wordById(id) ?: error("البطاقة غير موجودة")
            val saved = CardDetails.from(current.toDomain())
            val before = baseline ?: saved
            val edited = CardDetails(displayText, ttsPrompt, phonemicCue, sentencePrompt, phone, group ?: before.group)
            val merged = saved.merge(before, edited)
            require(merged.display.isNotBlank() && merged.speech.isNotBlank()) { "اسم البطاقة والنطق مطلوبان" }
            if (merged.group != saved.group) {
                require(current.categoryId == CategoryIds.FAMILY && saved.group != FamilyRelationships.RELATIVES &&
                    FamilyRelationships.validPeopleGroup(merged.group)) { "قسم العائلة غير صالح" }
            }
            check(dao.updateEditedItem(current.copy(displayText = merged.display, ttsPrompt = merged.speech,
                phonemicCue = merged.cue, sentencePrompt = merged.sentence, phone = merged.phone,
                subgroup = merged.group, customTextFields = current.customTextFields or before.changedTextFields(edited))) == 1)
        }
    }


    override suspend fun setPhoto(id: Long, image: InputStream) {
        image.use { source ->
            withMediaMutation { retired ->
                val dir = File(filesDir, "photos").apply { require(isDirectory || mkdirs()) }
                val target = File(dir, "word_${id}_${UUID.randomUUID()}.jpg")
                retired.add(target.absolutePath)
                SafePhotoProcessor.import(source.borrowed(), target)
                replaceMedia(id, target.absolutePath, photo = true, retired = retired)
            }
        }
    }

    override suspend fun clearPhoto(id: Long) = withMediaMutation { retired ->
        replaceMedia(id, null, photo = true, retired = retired)
    }

    /** Takes responsibility for the finished private recording, including failure/cancellation. */
    override suspend fun setAudio(id: Long, file: File) = withMediaMutation(file) { retired ->
        val target = privateMedia(file.absolutePath)
        require(target != null && target.isFile && target.length() > 0L) { "ملف التسجيل غير صالح" }
        replaceMedia(id, target.absolutePath, photo = false, retired = retired)
    }

    /** Copies a picked audio file into filesDir/custom_audio/ and stores its absolute path. */
    override suspend fun importAudio(id: Long, input: InputStream, extension: String) {
        input.use { source ->
            withMediaMutation { retired ->
                SafeMediaImport.checkAudioExtension(extension)
                val dir = File(filesDir, "custom_audio").apply { require(isDirectory || mkdirs()) }
                val target = File(dir, "word_${id}_${UUID.randomUUID()}.$extension")
                retired.add(target.absolutePath)
                SafeMediaImport.copy(source.borrowed(), target, SafeMediaImport.MAX_AUDIO_BYTES)
                replaceMedia(id, target.absolutePath, photo = false, retired = retired)
            }
        }
    }

    override suspend fun clearAudio(id: Long) = withMediaMutation { retired ->
        replaceMedia(id, null, photo = false, retired = retired)
    }

    // The public import owns the stream, including validation failure/cancellation before
    // copying starts. Helpers borrow it so it is closed exactly once by the outer use block.
    private fun InputStream.borrowed(): InputStream = object : FilterInputStream(this) {
        override fun close() = Unit
    }

    private suspend fun replaceMedia(id: Long, path: String?, photo: Boolean, retired: MutableSet<String>) {
        db.withTransaction {
            val item = dao.wordById(id) ?: error("البطاقة غير موجودة")
            (if (photo) item.photoPath else item.audioPath)?.let { retired.add(it) }
            val updated = if (photo) dao.updatePhoto(id, path) else dao.updateAudio(id, path)
            check(updated == 1) { "تعذّر تحديث البطاقة" }
        }
    }

    /**
     * Serialize attachment writes with backup snapshots and all other repository writes. Cleanup
     * runs AFTER the transaction, and rechecks references under the same lock. If another
     * operation attaches the file between commit and cleanup, that reference keeps it alive.
     * Cancellation after a commit must not delete a newly referenced file.
     */
    private suspend fun <T> withMediaMutation(
        recording: File? = null,
        action: suspend (MutableSet<String>) -> T,
    ): T {
        val candidates = linkedSetOf<String>()
        recording?.let { candidates.add(it.absolutePath) }
        try {
            return withContext(Dispatchers.IO) {
                db.storage.access { action(candidates) }
            }
        } finally {
            if (candidates.isNotEmpty()) {
                withContext(NonCancellable + Dispatchers.IO) {
                    db.storage.cleanupIfActive { deleteUnreferencedMedia(candidates) }
                }
            }
        }
    }

    /** DB/query/path failures deliberately retain files; cleanup must never undo a valid save. */
    private suspend fun deleteUnreferencedMedia(candidates: Set<String>) {
        try {
            // Compare canonical paths across BOTH columns. Imported aliases can refer
            // to one file even when a raw SQL string count says they are different paths.
            val referenced = dao.mediaPaths().map { File(it).canonicalPath }.toSet()
            for (raw in candidates) {
                val file = privateMedia(raw) ?: continue
                if (file.canonicalPath !in referenced && file.isFile) file.delete()
            }
        } catch (_: Exception) {
            // Prefer an unreferenced private file to deleting a potentially referenced one.
        }
    }

    /** Never delete external files, content URIs, directories, or symlinks escaping storage. */
    private fun privateMedia(raw: String): File? {
        val file = File(raw).canonicalFile
        val root = filesDir.canonicalFile
        return file.takeIf {
            it.parentFile?.parentFile == root && it.parentFile?.name in setOf("photos", "custom_audio")
        }
    }

}

