# DEVLOG

Projede yapılanların ve alınan kararların günlüğü. Yeni kayıt en üste eklenir.
Yapılacak işler için: [ROADMAP.md](ROADMAP.md)

Kayıt biçimi: `## YYYY-AA-GG SS:DD — Başlık`, altında **Yapılanlar** ve **Kararlar**.

---

## 2026-10-14 18:05 — Compose'a geçiş için tema hazırlığı

**Yapılanlar**

- ROADMAP'e "Aşama 5 — Jetpack Compose'a geçiş" eklendi.
- Compose kütüphaneleri eklendi: BOM `2026.06.00` (ui 1.11.3, material3 1.4.0) ve `lifecycle-runtime-compose` 2.10.0.
- `presentation/theme` paketi yazıldı:
  - `AppTheme`: Compose ekranlarını saran tema. Renkleri o anki XML temasından (seçili renk teması, açık / koyu) okuyor.
  - `appColorScheme`: okunan renkleri Compose renk şemasına çeviriyor; tanımlanmamış renk varsa Material 3 varsayılanı kalıyor.
  - `appComposeView`: Fragment'in görünümü olarak `AppTheme` içinde bir Compose ekranı oluşturuyor.
- `TestAppColorScheme` birim testleri yazıldı.
- Profil sayfasının açık temada ve diğer renk temalarında çalıştığı depo sahibi tarafından doğrulandı.

**Kararlar**

- Renkler Kotlin'de ikinci kez yazılmadı: geçiş boyunca XML temaları tek kaynak. Böylece 6 tema × açık / koyu renklerini iki yerde eşitlemek gerekmiyor. Tüm ekranlar Compose olunca renkler Kotlin'e taşınacak.
- Yazı tipleri ve köşe şekilleri Material 3 varsayılanında bırakıldı; XML teması da bunları değiştirmiyor.
- BOM `2026.09.00` SDK 37 ve AGP 9.1 istediği için kullanılmadı; proje yükseltmesi ayrı bir iş olarak ROADMAP'e yazıldı.

**Tespitler**

- Telefonda (koyu kahve teması) geçici bir log satırıyla 34 rengin hepsinin okunduğu ve değerlerin XML ile aynı olduğu görüldü (`primary #FFB5A0`, `surfaceContainer #291D1A`). Satır commit'lenmedi.
- Henüz hiçbir ekran Compose kullanmıyor; uygulamanın görünüşü değişmedi.

---

## 2026-10-14 17:35 — Profil sayfası yenilendi

**Yapılanlar**

- Profil sayfasının üstüne profil kartı kondu: adın baş harfleriyle yuvarlak avatar, ad, "Unvan · Sınıf · Okul" satırı ve doluysa "Hakkımda" metni. Ad boşsa avatarda kişi simgesi, bilgiler boşsa "kaleme dokun" ipucu çıkıyor.
- Bilgiler artık sayfanın içinde değil, kalem düğmesiyle açılan "Profili düzenle" penceresinde (Material çerçeveli kutular) düzenleniyor.
- Atatürk sözü kartı sade, italik bir alıntıya dönüştü.
- Seçenekler "Genel", "Veriler" ve "Hakkında" başlıklı kartlarda gruplandı; her satırın sonunda ok var. "Geliştiriciyi Destekle" ayrı, vurgulu bir kartta.
- Sayfanın altındaki sürüm yazısı hiç doldurulmuyordu; artık "Sürüm 1.1.0" görünüyor.
- Yükleniyor perdesi kaydırma alanının altında kalıyordu; üste alındı ve arkasındaki dokunuşları engelliyor.
- Baş harf ve bilgi satırı kuralları `ProfileHeaderFormatter` sınıfına çıkarıldı ve birim testleri yazıldı (`TestProfileHeaderFormatter`).
- `ProfileFragment`'e KDoc ve Timber logları eklendi; `android.util.Log` kaldırıldı. Kullanılmayan karakter kodu ve yorum satırına alınmış animasyon silindi.
- Dışa aktarma hata verirse artık kullanıcıya uyarı gösteriliyor; link açacak uygulama yoksa uygulama çökmüyor.

**Kararlar**

- Depo sahibine iki tasarım yönü gösterildi; "profil kartı + gruplu liste" seçildi.
- Düzenleme ayrı bir `DialogFragment` yerine `MaterialAlertDialog` ile yapıldı; uygulama dikey kullanıldığı için yeterli.

**Tespitler**

- Telefonda (koyu tema) sayfa ve düzenleme penceresi açıldı, çökme yok. Kaydetme telefonda denenmedi.

---

## 2026-10-12 17:10 — Release imzalama kuruldu, 1.1.0 yayınlandı

**Yapılanlar**

- `app/build.gradle.kts` içine isteğe bağlı release imzalama eklendi: `keystore/properties` dosyası varsa release derlemesi imzalanıyor, yoksa imzasız kalıyor.
- `keystore/` ve `keystore.properties` `.gitignore`'a eklendi.
- `1.1.0` release APK'sı imzalandı; imza doğrulandı ve sertifikasının GitHub'daki `1.0.0` APK'sıyla aynı olduğu görüldü (SHA-256 `a7c902d9…b05d`).
- GitHub'da `1.1.0` sürüm sayfası imzalı APK ve sürüm notlarıyla oluşturuldu.

**Kararlar**

- Şifreler depoda ve sohbette tutulmuyor; yalnızca depo sahibinin bilgisayarındaki git dışı dosyada duruyor ve onu yalnızca Gradle okuyor.
- Dosya elle ayrıştırılıyor (`java.util.Properties` ile değil): Windows yolundaki ters eğik çizgiler ve tırnak işaretleri öyle bozuluyordu.
- GitHub'daki otomatik derleme ve F-Droid imzasız derlemeye devam ediyor; anahtar oralarda yok.
- APK, `1.1.0` etiketiyle aynı uygulama kodundan derlendi (etiketten sonra `app/src` değişmedi; tek fark derleme betiğindeki imzalama ayarı).

**Tespitler**

- İmzalı APK telefona kurulmadı: paket adı mağaza sürümüyle aynı ve mağaza sürümüne dokunulmuyor.
- GitHub'daki `1.0.0` sürümünde `debug` adlı APK da release sertifikasıyla imzalanmış.

---

## 2026-10-12 16:50 — 1.1.0 etiketlendi

**Yapılanlar**

- `main` → `production` birleştirildi (#69) ve birleştirme commit'ine `1.1.0` etiketi atıldı (`eeae583`).
- F-Droid tarif dosyasındaki `commit` alanına etiketin commit'i yazıldı.

**Kararlar**

- Sürüm numarası `1.1.0` depo sahibi tarafından onaylandı.
- GitHub Release henüz oluşturulmadı; imzalı APK gerekiyor ve imza anahtarı depo sahibinde.

---

## 2026-10-12 16:30 — F-Droid hazırlığı ve 1.1.0 sürüm numarası

**Yapılanlar**

- Sürüm `1.1.0` (kod 6) yapıldı; `app_version` metni de `1.1.0` oldu.
- `dependenciesInfo` kapatıldı: APK'ya Google anahtarıyla şifrelenmiş bağımlılık bilgisi bloğu artık eklenmiyor (F-Droid şartı). Release APK'sında bloğun olmadığı doğrulandı.
- `fastlane/metadata/android` altına İngilizce ve Türkçe sürüm notu (`changelogs/6.txt`), simge ve dörder ekran görüntüsü eklendi; açıklama metinlerindeki satır başı boşlukları temizlendi.
- F-Droid tarif dosyası yazıldı: `fdroid/com.saboon.project_2511sch.yml`.
- Release derlemesi imzasız olarak sorunsuz çıktı (yaklaşık 10 MB); birim testleri ve lint geçti.

**Kararlar**

- Sürüm numarası `1.1.0`: `1.0.0`'dan beri yeni özellikler eklendi (devamsızlık tarihleri, boş günleri gizleme). Depo sahibine önerildi, itiraz gelmedi; etiket atılmadan önce onayı alınacak.
- `production`'a birleştirme ve `1.1.0` etiketi henüz yapılmadı; sürüm numarası onayı bekliyor.
- Ekran görüntüleri debug uygulamasındaki örnek veriyle alındı; İngilizce görüntülerde ders adları Türkçe.
- Tarif dosyası uygulama derlemesinde kullanılmıyor; yalnızca F-Droid başvurusuna eklenecek dosyanın depoda saklanan kopyası.
- Lisans alanı `AGPL-3.0-only` yazıldı; depo "or later" demiyor.

**Tespitler**

- Uygulama F-Droid'de yok (`fdroiddata` içinde tarif dosyası bulunmuyor). Daha önce başvuru yapılıp yapılmadığı görülemedi.
- Uygulamanın kendi izinleri arasında internet yok; APK'da kütüphanelerden gelen `WAKE_LOCK`, `ACCESS_NETWORK_STATE` ve `FOREGROUND_SERVICE` izinleri var.
- F-Droid'in kendi derleme aracı burada çalıştırılmadı; asıl derleme kontrolü onların tarafında olacak.
- GitLab hesabı açılamadı: doğrulama kodu gelmedi.

---

## 2026-10-12 15:50 — Yedek dosyasındaki tarih biçimi düzeltildi

**Yapılanlar**

- Yedek dosyası adında ve `exportDate` alanında dakika yerine ayın günü yazılıyordu (`HHddss`). Biçim `DataTransferPackage.EXPORT_DATE_FORMAT` sabitine alındı ve `yyyyMMdd_HHmmss` olarak düzeltildi.
- `ExportDataUseCase` için KDoc ve Timber logları eklendi.
- `TestDataTransferPackage` eklendi. `testDebugUnitTest`: 84 testin 84'ü geçti.
- GitHub Actions iş akışı (#66) birleştirildi; ilk çalıştırmada `gradlew` çalıştırma izni eksikti, düzeltildikten sonra 5 dk 24 sn'de geçti.

**Kararlar**

- Eski yedekler etkilenmiyor: `exportDate` yalnızca bilgi amaçlı bir metin, içe aktarma onu yorumlamıyor.
- Yerel git ayarında `core.filemode` kapatıldı; Windows'ta `gradlew`'in çalıştırma izninin her `git add`'de geri alınmasını önlemek için.

**Tespitler**

- `ExportDataUseCase` `Context`'e bağlı olduğu için birim testi yazılmadı; yedek alma telefonda denenmedi.

---

## 2026-10-12 15:30 — GitHub Actions ile otomatik derleme, test ve lint

**Yapılanlar**

- `.github/workflows/ci.yml` eklendi: her PR'da ve `main` / `production`'a her push'ta debug derlemesi, birim testleri ve lint çalışıyor; raporlar çıktı olarak yükleniyor.
- Lint'i kıran tek hata giderildi: hiçbir yerde kullanılmayan, Türkçe çevirisi olmayan şablon metni `hello_blank_fragment` silindi.
- `gradlew` git'te çalıştırılabilir olarak işaretlendi; `.gitattributes` ile satır sonlarının LF kalması sabitlendi (Linux'ta çalışabilmesi için).

**Kararlar**

- Lint hata verirse CI kırmızı olur; uyarılar (405 adet) CI'yı kırmaz.
- Cihaz gerektiren DAO testleri CI'da çalıştırılmıyor; emülatör kurulumu ayrı bir iş.
- CI kontrolü henüz zorunlu yapılmadı; birkaç PR'da sorunsuz çalıştığı görüldükten sonra zorunlu yapılacak.

---

## 2026-10-12 15:10 — Branch koruması açıldı, domain katmanı ekran sınıflarından ayrıldı

**Yapılanlar**

- GitHub'da `main` ve `production` için branch koruması açıldı: PR zorunlu (onay sayısı 0), doğrudan ve zorla push kapalı, branch silme kapalı, yönetici dahil.
- `FilterTask` ve `DataTransferPackage` presentation'dan `domain/model` altına taşındı.
- Domain use case'leri artık ekran satırı (`DisplayItem*`) üretmiyor, domain modeli döndürüyor:
  - `GetCourseDisplayItemListUseCase` → `GetCoursesGroupedByTagUseCase` (`CourseGroup`)
  - `GetFileDisplayItemListUseCase` → `GetFilesGroupedByCourseUseCase` (`CourseFiles`)
  - `GetTaskDisplayItemUseCase` → `GetCourseTasksUseCase` (`CourseTasks`)
  - `GetHomeDisplayItemsUseCase` → `GetHomeTimelineUseCase` (`HomeTimeline`, `HomeDay`, `HomeTimelineItem`)
  - `GetTagDisplayItemListUseCase` silindi; etiket listesi `TagReadUseCase.getAll()` ile geliyor.
- Satırları (başlık, içerik, alt bilgi) oluşturan eşleyiciler presentation'a eklendi: `toTagDisplayItems`, `toCourseDisplayItems`, `toSFileDisplayItems`, `toTaskDisplayItems`, `toHomeDisplayItems`.
- `Resource.mapData` yardımcı fonksiyonu eklendi.
- `RecurrenceRule` ve `GetHomeTimelineUseCase` Android ICU takvimi yerine `java.util.Calendar` kullanıyor.
- İçi boş ve kullanılmayan `SFileReadUseCase` silindi.
- Dokunulan ViewModel'lere (`Tag`, `Course`, `SFile`, `Home`) KDoc ve Timber logları eklendi.
- Testler: 4 yeni use case testi, satır eşleyicileri, `Resource.mapData`, `RecurrenceRule`. `testDebugUnitTest`: 82 testin 82'si geçti.
- Telefonda denendi (örnek veri yeniden eklendi, bir etiketle): ana sayfa, ders listesi (etiket grupları) ve ders detayı öncekiyle aynı göründü, çökme yok.

**Kararlar**

- Liste satırlarının nasıl dizileceği (başlık, alt bilgi, boş durum) presentation'ın işi sayıldı; domain yalnızca gruplanmış veriyi veriyor.
- Ana ekranda "hiç görev yok" ile "bu aralıkta görev yok" ayrımı `HomeTimeline.hasTasks` ile korunuyor; ekran davranışı değişmedi.
- Android takvimi yerine Java takvimi: haftalık / günlük / aylık adımlar aynı sonucu veriyor, kod artık birim testinde çalışabiliyor.
- Domain modellerindeki `Parcelable` şimdilik kabul edilen istisna olarak bırakıldı.
- Branch korumasında onay sayısı 0: tek kişilik depoda PR sahibinin kendi PR'ını birleştirebilmesi için.

**Tespitler**

- Domain'de kalan Android bağımlılıkları: `ExportDataUseCase` / `ImportDataUseCase` içinde `Context`, dosya deposu arayüzünde ve iki use case'te `Uri`, modellerde `Parcelable`. ROADMAP'e yazıldı.
- Data katmanı da presentation'a bağımlı: `SettingsRepositoryImp` → `SettingsConstants`, `WidgetUpdateWorker` → `WidgetHome`.
- `ViewModelTag` ve `ViewModelCourse` alarm eşitlemesi için repository'lere doğrudan erişiyor.
- Yedek dosyası adındaki tarih biçimi hatalı (`HHddss`: dakika yerine gün yazıyor).
- Dört ViewModel (`Tag`, `Course`, `SFile`, `Home`) ve `WidgetHome` için birim testi yazılmadı; değişen mantık test edilen use case ve eşleyicilerde.
- Etiket listesi, dosya listesi (örnek veride dosya yok) ve ana ekran widget'ı telefonda denenmedi.

---

## 2026-10-12 13:50 — Devamsızlık doğru tarihle kaydediliyor

**Yapılanlar**

- `LessonAbsenceWriteUseCase` eklendi: devamsızlık ekleme ve silmenin tek yeri. Dersi id ile veritabanından yeniden okuyup yalnızca devamsızlık listesini değiştiriyor.
- Bildirim: `AlarmReceiver` devamsızlık bildirimine oturum tarihini ekliyor, `NotificationActionReceiver` bunu `IncrementAbsenceWorker`'a iletiyor, worker o tarihle kaydediyor.
- Elle ekleme: "+" düğmesi artık tarih seçici açıyor (`AbsenceDialogs`). Ana ekranda varsayılan tarih o satırın oturumu, ders detayında dersin en son başlamış oturumu (`GetDefaultAbsenceDateUseCase`).
- Silme: "-" düğmesi o ders saatinin kayıtlı devamsızlıklarını listeliyor, seçilen siliniyor.
- `ViewModelTask`'a `addAbsence`, `removeAbsence`, `getDefaultAbsenceDate` eklendi. Adaptörler artık görevi kendileri değiştirmiyor, yalnızca tıklamayı bildiriyor.
- Dokunulan dosyalarda `android.util.Log` Timber'a çevrildi (`FragmentHome`, `AlarmReceiver`, `NotificationActionReceiver`, `IncrementAbsenceWorker`), KDoc'lar tamamlandı.
- Testler: `TestLessonAbsenceWriteUseCase` (10), `TestGetDefaultAbsenceDateUseCase` (6), `TestAbsenceDialogs` (4), `TestViewModelTask` (7). `testDebugUnitTest`: 37 testin 37'si geçti.
- Telefonda denendi: ders detayında "+" ile 05.10.2026 eklendi, listede göründü, "-" ile silindi; ana ekranda "+" tarih seçiciyi açtı.

**Kararlar**

- Bildirimin oturum tarihi olarak bildirimin gösterildiği an kullanılıyor. Devamsızlık alarmı oturum bittiğinde çaldığı için bu an o oturumun gününe denk geliyor; kullanıcı günler sonra cevaplasa da tarih değişmiyor.
- Eski sürümün gösterdiği, tarih taşımayan bildirimler cevaplanırsa cevaplandığı an kaydediliyor.
- "-" için hangi kaydın silineceği soruluyor. Eski davranış (dersin kayıtlı tarihiyle eşleşeni sil) elle seçilen tarihlerle çalışmazdı.
- Devamsızlık değişince alarmlar yeniden kurulmuyor; devamsızlık dersin saatini değiştirmiyor.
- Tarih seçiciden gelen gün, yerel saatle öğlen 12:00 olarak kaydediliyor; böylece her saat diliminde seçilen günde kalıyor.
- Ana ekranda da tarih soruluyor (istek "elle eklemede tarih sorsun" olduğu için), ama o satırın günü hazır seçili geliyor.

**Tespitler**

- Eski kodda ana ekrandaki "+" / "-" dersin kayıtlı tarihini o haftanın tarihiyle değiştirerek kaydediyordu; yeni akışta bu yol kullanılmıyor.
- Bildirim akışı cihazda gerçek bildirimle denenmedi; alıcı dışarıya kapalı olduğu için adb ile tetiklenemedi.
- `IncrementAbsenceWorker`, `AlarmReceiver`, `NotificationActionReceiver`, `FragmentHome` ve adaptörler için birim testi yazılmadı (Android'e bağlı sınıflar); mantıkları test edilen use case'lere taşındı.
- Bu değişiklikten önce kaydedilmiş devamsızlıkların tarihleri olduğu gibi kaldı.

## 2026-10-12 13:30 — Devamsızlık tarihleri listesi eklendi

**Yapılanlar**

- `AbsenceRecord` domain modeli ve `GetCourseAbsenceRecordsUseCase` eklendi: bir dersin görevlerinden devamsızlık kayıtlarını çıkarır, yeniden eskiye sıralar.
- `ViewModelTask` içine `absenceRecordsState` eklendi (`tasksState`'ten türetiliyor).
- `FragmentCourseDetails`: devamsızlık yazısına basınca tarihleri ve ders saatinin adını gösteren bir pencere açılıyor; kayıt yoksa "Kayıtlı devamsızlık yok." yazıyor. Toplam sayı artık aynı kaynaktan hesaplanıyor.
- Dokunulan dosyalara KDoc ve Timber logları eklendi; `ViewModelTask`'taki kullanılmayan import'lar kaldırıldı.
- Testler: `TestGetCourseAbsenceRecordsUseCase` (6), `TestViewModelTask` (5), ortak örnek veriler için `TestBaseTask`. `testDebugUnitTest`: 15 testin 15'i geçti.
- Debug uygulamasının veritabanına adb ile örnek veri eklendi (Fizik, Programlama, Türk Dili; ders saatleri, devamsızlıklar, bir sınav, bir ödev).
- Telefonda denendi: Fizik dersinde "Devamsızlık: 3 oturum" ve pencerede 3 tarih doğru göründü.

**Kararlar**

- Devamsızlık hesabı fragment'tan alınıp use case'e taşındı; fragment yalnızca gösteriyor, mantık birim testiyle korunuyor.
- Use case veritabanına gitmiyor, ViewModel'in elindeki görev listesini kullanıyor; böylece liste ekrandaki görevlerle her zaman aynı.
- Aynı tarih iki kez kayıtlıysa iki satır gösteriliyor, çünkü sayı da iki sayıyor.
- Pencere için ayrı bir DialogFragment yazılmadı; salt okunur kısa bir liste için `MaterialAlertDialogBuilder` yeterli.
- Kayıtlı tarihlerin doğruluğu bu işte düzeltilmedi; ayrı iş olarak ROADMAP'e yazıldı.

**Tespitler**

- Kayıtlı devamsızlık tarihleri her zaman gerçek devamsızlık günü değil. Ders detayındaki "+" düğmesi ve bildirimdeki "Katılmadım" (`IncrementAbsenceWorker`) dersin kayıtlı tarihini (`lesson.date`) ekliyor. Yalnızca ana ekrandaki "+" o haftanın tarihini ekliyor.
- Ana ekrandaki "+" / "-" düğmeleri dersi `date = o haftanın tarihi` olarak güncellemeye gönderiyor; bu, dersin kayıtlı tarihini değiştiriyor olabilir. Koddan görüldü, cihazda doğrulanmadı.
- "-" düğmesi yalnızca `task.date` ile eşleşen kaydı siliyor.
- `GetCourseAbsenceRecordsUseCase` ilk yüklemede boş listeyle bir kez fazladan çalışıyor (logda `tasks=0`); zararsız.

## 2026-10-12 13:10 — PR'lar birleşti, debug sürümü telefonda çalışıyor

**Yapılanlar**

- #60, #62 ve #61 depo sahibi tarafından `main`'e birleştirildi. `main-test` yerelde ve GitHub'da silindi.
- Debug derlemesine `applicationIdSuffix = ".debug"` ve `versionNameSuffix = "-debug"` eklendi.
- Debug sürümü kablosuz hata ayıklama ile telefona (SM-A515F, Android 13) kuruldu ve açıldı; çökme yok, logda `Timber planted` satırı görüldü.
- `CLAUDE.md` içine "Telefonda çalıştırma" bölümü eklendi.

**Kararlar**

- Debug sürümü ayrı paket adıyla kurulacak. Sebep: telefonda Play Store'dan kurulu sürüm (`1.0.0-ct1`, kod 5) var, imzası farklı olduğu için debug sürümü üstüne kurulamıyor; kaldırmak gerçek veriyi silerdi.
- Mağaza sürümü hiçbir koşulda kaldırılmayacak ve verisi silinmeyecek.
- Uygulamayı etkileyen her değişiklikten sonra debug sürümü telefona kurulup açılacak.
- Asistan PR'ları kendisi birleştiremiyor: otomatik izin denetleyicisi hem birleştirmeyi hem de asistanın kendi izin dosyasını yazmasını engelledi. İzin kuralını depo sahibi ekleyecek; o zamana kadar birleştirmeyi depo sahibi yapıyor.

**Tespitler**

- Provider `authorities` değerleri `${applicationId}` / `context.packageName` kullandığı için paket eki ek değişiklik gerektirmedi.
- Debug sürümü ayrı uygulama olduğundan verisi boş başlar; mağaza sürümündeki dersler onda görünmez.

## 2026-10-12 12:10 — Test altyapısı alındı, branch'ler toparlandı, site düzenlendi

**Yapılanlar**

- `main-test` branch'i `chore/test-infrastructure` içine birleştirildi: test bağımlılıkları (MockK, Truth, `kotlinx-coroutines-test`, `room-testing`), 5 DAO testi, `TestUserRepository` ve temel test sınıfları geldi. Örnek testler (`ExampleUnitTest`, `ExampleInstrumentedTest`) silindi.
- Aynı birleştirmeyle hiçbir yerden çağrılmayan fonksiyonlar kaldırıldı: course ve tag için `activationById`, course için `getAllByProgramTableIds` (DAO, repository, arayüz ve use case katmanlarında).
- `app/build.gradle.kts` çakışması çözüldü; Timber ve test bağımlılıkları birlikte duruyor.
- `androidx.test:runner` bağımlılığı `implementation` yerine `androidTestImplementation` yapıldı.
- `testDebugUnitTest` çalıştırıldı: 4 testin 4'ü geçti (`TestUserRepository`). DAO testleri derlendi ama çalıştırılmadı (cihaz gerekiyor).
- `fdroid` branch'i silindi.
- `absence-view`, `feature/course-absence-count` adıyla güncel `main` üzerine alındı ve PR açıldı.
- Site: `docs/index.html` içindeki "Keep Android Open" bölümü sayfanın en üstünden footer'ın hemen üstüne taşındı (PR #61).

**Kararlar**

- `main-test` elle taşınmadı, doğrudan birleştirildi. Önceki kayıttaki "elle taşınması gerekecek" tespiti yanlıştı: branch'in ana koddaki tek değişikliği kullanılmayan fonksiyonları silmekti ve tek çakışma `build.gradle.kts` içindeydi. Birleştirme, özgün commit geçmişini de korudu.
- Test runner'ı uygulama bağımlılığından çıkarıldı; release APK'sına test kütüphanesi girmemeli.
- PR'lar asistan tarafından birleştirilmedi; birleştirme kararı depo sahibinde. Bu yüzden `chore/test-infrastructure`, `chore/project-workflow-docs` üzerine kurulu ve PR'lar sırayla birleştirilmeli.
- `main-test`, test altyapısı PR'ı birleşene kadar silinmedi.
- Devamsızlık özelliği cihazda denenmeden birleştirilmeyecek; arayüz değişikliği içeriyor.
- Sitedeki bölüm kaldırılmadı, yalnızca aşağı alındı; metinler aynı.

## 2026-10-12 11:20 — Log, test ve commit kuralları eklendi, Timber kuruldu

**Yapılanlar**

- `CLAUDE.md` içine üç bölüm eklendi: log kuralları, test kuralları, commit ve push kuralları.
- Timber bağımlılığı eklendi (`com.jakewharton.timber:timber:5.0.1`).
- `Project2511schApplication` içinde Timber yalnızca debug derlemelerinde dikiliyor; sınıfa ve fonksiyonlarına KDoc eklendi, kullanılmayan `android.os.Build` import'u kaldırıldı.

**Kararlar**

- Log için `android.util.Log` yerine Timber kullanılacak. Sebep: `Log` birim testlerinde hata veriyor, Timber ağaç dikilmediğinde sessiz kalıyor.
- Debug kontrolü `BuildConfig.DEBUG` yerine `ApplicationInfo.FLAG_DEBUGGABLE` ile yapıldı; projede `BuildConfig` üretimi açık değil ve sırf bunun için açılmadı.
- Mevcut `android.util.Log` çağrıları (15 dosya) toplu çevrilmedi; her dosya dokunulduğunda çevrilecek.
- Birim testi yalnızca dokunulan dosyalar için yazılacak. Saf arayüz sınıfları (Fragment, adapter, Application) zorunlu kapsamın dışında.
- Mevcut test üslubu (`main-test` branch'i: JUnit4, MockK, Truth, `runTest`, `TestX` adlandırması) korunacak.
- Commit ve PR'larda Claude / yapay zeka ibaresi olmayacak; commit'ler yalnızca `sabahattincicek` adıyla atılacak.

**Tespitler**

- Test bağımlılıkları (MockK, Truth, coroutines-test) yalnızca `main-test` branch'inde var, `main`'de yok. İlk test yazılmadan önce `main`'e alınmaları gerekiyor; ROADMAP Aşama 2'ye eklendi.
- `main-test`, `main`'den 52 commit geride ve bazı DAO / repository fonksiyonlarını siliyor; doğrudan birleştirmek yerine testlerin elle taşınması gerekecek.

## 2026-10-12 11:01 — Proje düzeni ve branch yapısı kuruldu

**Yapılanlar**

- `ROADMAP.md` oluşturuldu: işler aşamalara bölündü, her iş işaretlenebilir kutu ile yazıldı.
- `DEVLOG.md` oluşturuldu (bu dosya).
- `CLAUDE.md` oluşturuldu: Clean Architecture katman kuralları, yorum satırı kuralları, ROADMAP/DEVLOG güncelleme kuralı ve branch stratejisi yazıldı.
- `production` branch'i `1.0.0` etiketinden (`182b58a`) açıldı ve GitHub'a gönderildi.
- Bu dosyalar `chore/project-workflow-docs` branch'inde hazırlandı.

**Kararlar**

- `production` yayınlanmış kodu temsil eder ve `1.0.0` etiketinden başlar. `main`, `1.0.0`'dan 43 commit ileride olduğu ve `versionName` değeri `1.0.0-ct1` (test sürümü) olduğu için `main`'in ucu yayınlanmış sayılmadı.
- `main` geliştirme branch'i olarak kalır; ayrı bir `develop` branch'i açılmadı. Tek geliştirici için iki kalıcı branch yeterli, ayrıca GitHub Pages `main:/docs` üzerinden yayınlandığı için `main`'in yerinde kalması gerekiyor.
- İşler `feature/`, `fix/`, `chore/` branch'lerinde yapılır ve PR ile `main`'e girer. Yayındaki sürüme acil düzeltme `production`'dan açılan `hotfix/` branch'i ile yapılır.
- Yorum satırı kuralı yalnızca dokunulan dosyalara uygulanır; bütün kod tabanı tek seferde yorumlanmayacak.
- Kod içi yorumlar İngilizce (mevcut KDoc'larla aynı), `ROADMAP.md`, `DEVLOG.md` ve `CLAUDE.md` Türkçe yazılır.
- Eski branch'ler (`fdroid`, `main-test`, `absence-view`) silinmedi; gözden geçirilmeleri ROADMAP'e iş olarak eklendi.

**Tespitler**

- Domain katmanındaki 9 use case dosyası `presentation` paketinden sınıf import ediyor (`DisplayItem*`, `FilterTask`, `DataTransferPackage`). Clean Architecture'a aykırı; ROADMAP Aşama 1'e eklendi.
