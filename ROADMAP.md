# ROADMAP

Uygulama için yapılacak işler. Biten işin kutusu `[x]` olarak işaretlenir.
Yapılanların ayrıntısı ve alınan kararlar için: [DEVLOG.md](DEVLOG.md)

## Aşama 0 — Proje düzeni ve çalışma altyapısı

- [x] `ROADMAP.md` dosyasını oluştur
- [x] `DEVLOG.md` dosyasını oluştur
- [x] Mimari, yorum satırı ve branch kurallarını `CLAUDE.md` içine yaz
- [x] `1.0.0` etiketinden `production` branch'ini aç ve GitHub'a gönder
- [x] Log, test ve commit kurallarını `CLAUDE.md` içine yaz
- [x] Timber bağımlılığını ekle ve `Project2511schApplication` içinde debug derlemeleri için dik
- [x] `fdroid` branch'ini sil (tamamen `main`'e birleşmişti)
- [x] `absence-view` branch'ini `feature/course-absence-count` olarak güncel `main` üzerine al ve PR aç
- [x] Açık PR'ları `main`'e birleştir: #60 (kurallar ve Timber), #62 (test altyapısı), #61 (site)
- [x] `main-test` branch'ini sil
- [x] Debug sürümünü mağaza sürümünün yanına kurulacak şekilde ayarla (`.debug` paket eki) ve telefonda çalıştır
- [ ] Asistanın PR birleştirebilmesi için izin kuralını ekle (`/permissions` → `PowerShell(gh pr merge *)`)
- [ ] GitHub'da `main` ve `production` için branch koruması aç (doğrudan push kapalı, PR zorunlu)

## Aşama 1 — Clean Architecture borçları

- [ ] `DisplayItem*` sınıflarına olan bağımlılığı domain use case'lerinden kaldır (`GetHomeDisplayItemsUseCase`, `GetTaskDisplayItemUseCase`, `GetCourseDisplayItemListUseCase`, `GetTagDisplayItemListUseCase`, `GetFileDisplayItemListUseCase`, `SFileReadUseCase`)
- [ ] `FilterTask` sınıfını presentation'dan domain'e taşı
- [ ] `DataTransferPackage` sınıfını presentation'dan domain'e taşı (`ExportDataUseCase`, `ImportDataUseCase`)
- [ ] Domain katmanında Android framework bağımlılığı kalıp kalmadığını denetle
- [ ] Dokunulan dosyalara sınıf ve fonksiyon KDoc'larını ekle (sürekli iş)

## Aşama 2 — Test altyapısı

- [x] Test bağımlılıklarını ekle (MockK, Truth, `kotlinx-coroutines-test`, `room-testing`)
- [x] `main-test` branch'indeki DAO ve repository testlerini güncel kodun üzerine al
- [ ] DAO testlerini cihazda veya emülatörde çalıştır (`connectedDebugAndroidTest`); şimdiye kadar yalnızca derlendikleri doğrulandı
- [ ] Dokunulan dosyalar için birim testlerini yaz (sürekli iş)
- [ ] Dokunulan dosyalardaki `android.util.Log` çağrılarını Timber'a çevir (sürekli iş; 15 dosyada kullanım var)
- [ ] PR'larda derleme, test ve lint çalıştıran GitHub Actions iş akışı ekle

## Aşama 3 — Sıradaki sürüm

- [ ] Ders detayındaki devamsızlık sayısı özelliğini (`feature/course-absence-count`) cihazda dene ve birleştir
- [x] Sitedeki "Keep Android Open" bölümünü sayfanın altına taşı (#61)
- [ ] `1.0.0` sonrası `main`'de biriken değişiklikleri (ana ekranda boş gün başlıklarını gizleme ayarı vb.) sürüm notlarına dök
- [ ] `versionCode` / `versionName` değerlerini yeni sürüm için güncelle
- [ ] `main` → `production` birleştir, etiketle, GitHub Release oluştur
- [ ] F-Droid yayınını tamamla (README'de "Coming Soon")

## Aşama 4 — Yeni özellikler

- [ ] Bulut senkronizasyonu: isteğe bağlı ve güvenli cihazlar arası eşitleme
- [ ] Program paylaşımı: ders programını başka kullanıcıyla paylaşma
- [ ] Performans analizleri: çalışma alışkanlıkları ve sınav ilerlemesi için görsel istatistikler
