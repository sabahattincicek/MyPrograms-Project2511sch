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
- [ ] Bu düzenleme branch'ini (`chore/project-workflow-docs`) `main`'e birleştir
- [ ] Eski branch'leri gözden geçir: `fdroid` (tamamen birleşmiş), `main-test` (9 birleşmemiş commit), `absence-view` (1 birleşmemiş commit, yalnızca yerelde)
- [ ] GitHub'da `main` ve `production` için branch koruması aç (doğrudan push kapalı, PR zorunlu)

## Aşama 1 — Clean Architecture borçları

- [ ] `DisplayItem*` sınıflarına olan bağımlılığı domain use case'lerinden kaldır (`GetHomeDisplayItemsUseCase`, `GetTaskDisplayItemUseCase`, `GetCourseDisplayItemListUseCase`, `GetTagDisplayItemListUseCase`, `GetFileDisplayItemListUseCase`, `SFileReadUseCase`)
- [ ] `FilterTask` sınıfını presentation'dan domain'e taşı
- [ ] `DataTransferPackage` sınıfını presentation'dan domain'e taşı (`ExportDataUseCase`, `ImportDataUseCase`)
- [ ] Domain katmanında Android framework bağımlılığı kalıp kalmadığını denetle
- [ ] Dokunulan dosyalara sınıf ve fonksiyon KDoc'larını ekle (sürekli iş)

## Aşama 2 — Test altyapısı

- [ ] Test bağımlılıklarını `main`'e ekle (MockK, Truth, `kotlinx-coroutines-test`, `room-testing`; `main-test` branch'inde mevcut)
- [ ] `main-test` branch'indeki DAO ve repository testlerini güncel `main` üzerine taşı
- [ ] Dokunulan dosyalar için birim testlerini yaz (sürekli iş)
- [ ] Dokunulan dosyalardaki `android.util.Log` çağrılarını Timber'a çevir (sürekli iş; 15 dosyada kullanım var)
- [ ] PR'larda derleme, test ve lint çalıştıran GitHub Actions iş akışı ekle

## Aşama 3 — Sıradaki sürüm

- [ ] `absence-view` branch'indeki ders detayında devamsızlık sayısı özelliğini tamamla ve birleştir
- [ ] `1.0.0` sonrası `main`'de biriken değişiklikleri (ana ekranda boş gün başlıklarını gizleme ayarı vb.) sürüm notlarına dök
- [ ] `versionCode` / `versionName` değerlerini yeni sürüm için güncelle
- [ ] `main` → `production` birleştir, etiketle, GitHub Release oluştur
- [ ] F-Droid yayınını tamamla (README'de "Coming Soon")

## Aşama 4 — Yeni özellikler

- [ ] Bulut senkronizasyonu: isteğe bağlı ve güvenli cihazlar arası eşitleme
- [ ] Program paylaşımı: ders programını başka kullanıcıyla paylaşma
- [ ] Performans analizleri: çalışma alışkanlıkları ve sınav ilerlemesi için görsel istatistikler
