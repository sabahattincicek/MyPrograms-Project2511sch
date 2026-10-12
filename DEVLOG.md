# DEVLOG

Projede yapılanların ve alınan kararların günlüğü. Yeni kayıt en üste eklenir.
Yapılacak işler için: [ROADMAP.md](ROADMAP.md)

Kayıt biçimi: `## YYYY-AA-GG SS:DD — Başlık`, altında **Yapılanlar** ve **Kararlar**.

---

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
