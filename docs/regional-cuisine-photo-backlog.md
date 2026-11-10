# Sawti: dedicated cuisine images by dialect — complete

Shared word photos are assigned by persistent Room semantic keys. Country-specific cuisine entries are **not** assigned Libyan photos merely because their names look similar. All 120 dishes below now have their own bundled 512 × 512 WebP image, generated individually using the existing Libyan food images as visual references. The set contains 15 images for each of the eight non-Libyan dialects.

Assets are mapped explicitly in `WordImageCatalog` using each dish’s persistent semantic key. Existing Libyan images, vocabulary, recorded audio and user-selected photos are unchanged. Custom `photoPath` images retain priority in `ItemImage`. No database migration or image network request is needed.

The machine-readable inventory with semantic keys, drawable names and SHA-256 checksums is `regional-cuisine-images.csv`. All 120 exported files were decoded, checked at 512 × 512, checked for byte-identical duplicates, and reviewed on dialect contact sheets. The Algerian berkoukes image was regenerated after review to correct the pasta grain size.

## أكلات تونسية — 15 dishes

- كسكسي بالحوت
- لبلابي
- بريك
- كفتاجي
- طاجين تونسي
- صحن تونسي
- شربة لسان عصفور
- ملوخية تونسية
- مرقة جلبانة
- مقرونة بالصلصة
- عجة بالمرقاز
- سلاطة مشوية
- فريكاسي
- مقروض قيرواني
- بامبالوني

## أكلات جزائرية — 15 dishes

- رشتة
- شخشوخة بسكرية
- كسكسي جزائري
- طاجين الزيتون
- شربة فريك
- بوراك جزائري
- كرانتيكا
- طاجين الحلو
- بركوكس
- جواز بطاطا
- مطبقة / محاجب
- دوارة
- قرنون معمر
- قلب اللوز
- مقروط العسل

## أكلات مغربية — 15 dishes

- طاجين بالبرقوق
- كسكسو سبع خضار
- بسطيلة بالحوت
- حريرة مغربية
- طنجية مراكشية
- رفيسة بالدجاج
- زعلوك
- تكتوكة
- طاجين كفتة بالبيض
- شوا مغربي
- مسمن
- بغرير
- سفة مدفونة
- شباكية
- سلو

## أكلات مصرية — 15 dishes

- كشري
- ملوخية بالتقلية
- حواوشي
- فول مدمس
- طعمية
- حمام محشي
- فتة بالخل والتوم
- صينية بطاطس
- كباب وحواوشي
- محشي مشكل
- مسقعة بلدي
- كوارع وفتة
- رقاق باللحمة
- شربة لسان عصفور
- أم علي

## أكلات سورية — 15 dishes

- كبة مقلية
- شاكرية برز
- يبرق ورق عنب
- شاورما دجاج
- فتة حمص
- مقلوبة باذنجان
- فتوش
- تبولة
- كباب حلبي
- شيش برك
- كوسا بلبن / شيخ المحشي
- متبل باذنجان
- حراق بأصبعه
- حلاوة الجبن
- كنافة نابلسية

## أكلات عراقية — 15 dishes

- دولمة عراقية
- سمك مسكوف
- قوزي على تمن
- تشريب لحم
- باجة
- كبة تمن / حلب
- كبة موصلية
- برياني عراقي
- مطبق سمك
- فافير / مخلمة
- بامية عراقية
- شلغم مطبوخ
- هريسة
- كليجة
- زلابية

## أكلات سعودية — 15 dishes

- كبسة لحم
- مندي
- جريش
- قرصان
- سليق طائفي
- حنيذ
- مطازيز
- مرقوق
- مظبي دجاج
- بخاري
- مدفون
- كبدة بالحوار
- معصوب
- حنيني
- لقيمات

## أكلات عربية — 15 dishes

- أرز باللحم
- دجاج مشوي
- شوربة عدس
- يخنة خضار
- مشويات مشكلة
- سلطة خضراء
- سمك مطهو
- فطائر مخبوزة
- حساء خضار
- لحم مسلوق
- خبز مخبوز
- ثمار وبقوليات
- بيض مسلوق
- تمر وحليب
- حلوى عربية


Existing user-selected photos and recordings are never replaced by this asset addition.
