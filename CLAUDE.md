# MyPrograms — Çalışma Kuralları

Bu dosya, projede çalışan yapay zeka asistanının (ve katkı veren herkesin) uyması gereken kuralları içerir.
Her oturumun başında okunur.

## Proje

Öğrenciler için ders programı, sınav, ödev ve ders materyali yöneten Android uygulaması.
Kotlin, Clean Architecture + MVVM, Hilt, Room, Coroutines/Flow, View Binding (XML) ve widget için Jetpack Glance.
Tüm veri cihazda tutulur; ağ katmanı yoktur.

Kaynak kökü: `app/src/main/java/com/saboon/project_2511sch/`

## Mimari kuralları (Clean Architecture)

Bağımlılık yönü her zaman içe doğrudur: `presentation → domain ← data`.

| Katman | Paket | İçerik | Neye bağımlı olabilir |
|---|---|---|---|
| Domain | `domain/model`, `domain/repository`, `domain/usecase`, `domain/alarm` | Modeller, repository arayüzleri (`I...Repository`), use case'ler | Hiçbir katmana. Android framework'üne de bağımlı olmamalı |
| Data | `data/local` (dao, entity, mapper, database), `data/repository`, `data/alarm`, `data/worker` | Room, repository implementasyonları (`...RepositoryImp`), alarm ve worker'lar | Yalnızca domain |
| Presentation | `presentation/<özellik>` | Fragment, DialogFragment, ViewModel, RecyclerAdapter, DisplayItem | Yalnızca domain |
| DI | `di` | Hilt modülleri | Hepsi (bağlama noktası) |
| Util | `util` | Katmanlardan bağımsız yardımcılar (`Resource`, `IdGenerator` vb.) | Hiçbir katmana |

Uyulacak kurallar:

- Entity sınıfları data katmanından dışarı çıkmaz; sınırda `mapper` ile domain modeline çevrilir.
- ViewModel yalnızca use case çağırır; DAO veya repository implementasyonuna doğrudan erişmez.
- Repository ve use case sonuçları `Resource` ile sarılır; akışlar `Flow<Resource<T>>` döner.
- Her özellik için okuma ve yazma ayrı use case'lerdedir (`XReadUseCase`, `XWriteUseCase`).
- Yeni bağımlılıklar Hilt ile enjekte edilir; elle nesne üretilmez.
- Mevcut adlandırma korunur: `FragmentX`, `DialogFragmentX`, `ViewModelX`, `RecyclerAdapterX`, `DisplayItemX`, `IXRepository`, `XRepositoryImp`.

Bilinen ihlal: bazı use case'ler `presentation` paketinden sınıf import ediyor (`DisplayItem*`, `FilterTask`, `DataTransferPackage`).
Bu borç `ROADMAP.md` içinde takip ediliyor. Yeni kodda bu ihlal tekrarlanmaz; dokunulan dosyada fırsat varsa düzeltilir.

## Yorum satırı kuralları

Düzenlenen **her** dosya için geçerlidir (yeni dosyalar dahil). Dokunulmayan dosyalar sırf yorum eklemek için değiştirilmez.

- **Dosya / sınıf başı:** Sınıfın (veya dosyanın) ne işe yaradığını, hangi katmanda olduğunu, kimler tarafından kullanıldığını ve önemli tasarım kararlarını anlatan ayrıntılı bir KDoc bloğu (`/** ... */`).
- **Her fonksiyonun başı:** Fonksiyonun ne yaptığını anlatan bir iki cümlelik KDoc. Gerekliyse `@param` / `@return` eklenir.
- Düzenlenen dosyada yorumu eksik olan diğer sınıf ve fonksiyonlar da aynı değişiklikte tamamlanır.
- Kod değişince yorum da güncellenir; eskimiş yorum bırakılmaz.
- Yorum dili İngilizce'dir (koddaki mevcut KDoc'larla aynı).

## Log kuralları

Düzenlenen her dosyada, anlamlı iş yapan sınıflara (repository, use case, ViewModel, worker, receiver, alarm, Fragment içindeki iş akışları) debug'ı kolaylaştıracak loglar eklenir.
Yalnızca veri taşıyan sınıflara (model, entity, DisplayItem, sabitler) log eklenmez.

- Log için **Timber** kullanılır (`timber.log.Timber`). `android.util.Log` kullanılmaz; birim testlerinde "not mocked" hatası verir, Timber ise ağaç dikilmediğinde sessizce hiçbir şey yapmaz.
- Timber, `Project2511schApplication` içinde yalnızca debug derlemelerinde dikilir; release derlemesi log üretmez.
- `TAG` sabiti tanımlanmaz; Timber etiketi sınıf adından kendisi üretir.
- Seviyeler: `Timber.d` akış ve durum, `Timber.i` önemli olaylar, `Timber.w` beklenmeyen ama kurtarılabilir durumlar, `Timber.e(exception, "...")` hatalar.
- Neyin loglanacağı: fonksiyona giriş ve önemli parametreler (id gibi), dallanma kararları, işlem sonucu (`Resource.Success` / `Resource.Error`), yakalanan her exception.
- Mesajlar İngilizce yazılır ve biçimlendirme Timber'a bırakılır: `Timber.d("Task updated: id=%s", task.id)`.
- Kişisel veri (ad, e-posta, not içeriği, dosya içeriği) loglanmaz; id yeterlidir.
- Düzenlenen dosyada mevcut `android.util.Log` çağrıları varsa aynı değişiklikte Timber'a çevrilir.

## Test kuralları

Düzenlenen her dosya için birim testi yazılır veya mevcut testi güncellenir. Bütün kod tabanı için sıfırdan test yazılmaz; kapsam dokunulan dosyalarla büyür.

- Önce mevcut testlere bakılır. Daha önce yazılmış testler `main-test` branch'indedir (DAO testleri, `TestUserRepository`, `TestBaseDao`, `TestBaseRepository`); `main`'e alınana kadar oradan örnek alınır ve tekrar yazılmaz.
- Test edilecekler: use case, repository implementasyonu, mapper, ViewModel ve `util` içindeki mantık. Saf Android arayüz sınıfları (Fragment, DialogFragment, RecyclerAdapter, Application, özel View) için birim testi zorunlu değildir; içlerindeki mantık test edilebilir bir sınıfa çıkarılabiliyorsa çıkarılır.
- Birim testleri `app/src/test`, cihaz gerektirenler (Room DAO) `app/src/androidTest` altında, kaynak dosyayla aynı pakette durur.
- Mevcut üslup korunur: JUnit4, MockK, Truth, `kotlinx-coroutines-test` (`runTest`); sınıf adı `TestX`, test adı ters tırnak içinde cümle, gövde `GIVEN / WHEN / THEN` bölümlü, ortak örnek veriler `TestBase...` sınıfında.
- Hem başarılı yol hem hata yolu (`Resource.Error`, fırlatılan exception) test edilir.
- İş bitmeden `./gradlew testDebugUnitTest` çalıştırılır; sonuç olduğu gibi raporlanır.

## Commit ve push kuralları

- Commit'ler yalnızca depo sahibinin adıyla atılır (git'te ayarlı kullanıcı: `sabahattincicek`).
- Commit mesajına, PR başlığına ve PR açıklamasına Claude veya yapay zeka ile ilgili hiçbir şey yazılmaz: `Co-Authored-By` satırı, "Generated with Claude Code" notu vb. eklenmez.
- `--author` ile veya git ayarı değiştirilerek farklı bir kimlik kullanılmaz.
- Commit mesajları İngilizce ve kısadır (mevcut geçmişle aynı).

## ROADMAP.md ve DEVLOG.md

- `ROADMAP.md`: Yapılacak işler, aşama aşama. Bir iş bitince kutusu `[x]` yapılır. Yeni çıkan işler ilgili aşamaya eklenir.
- `DEVLOG.md`: Yapılanların ve alınan kararların günlüğü. Yeni kayıt **en üste** eklenir, başlığı `## YYYY-AA-GG SS:DD — Başlık` biçimindedir.
- Her iş bittiğinde ikisi de aynı değişiklik içinde güncellenir.

## Branch stratejisi

| Branch | Amaç |
|---|---|
| `production` | Yayınlanmış kod. Her release buradan alınır ve burada etiketlenir (`1.0.0` gibi) |
| `main` | Geliştirme / entegrasyon. Varsayılan branch. GitHub Pages `main:/docs` üzerinden yayınlanır |
| `feature/<ad>` | Yeni özellik. `main`'den açılır, PR ile `main`'e döner |
| `fix/<ad>` | Hata düzeltmesi. `main`'den açılır, PR ile `main`'e döner |
| `chore/<ad>` | Dokümantasyon, yapılandırma, bakım |
| `hotfix/<ad>` | Yayındaki sürüm için acil düzeltme. `production`'dan açılır, hem `production`'a hem `main`'e birleştirilir |

- `main` ve `production` üzerine doğrudan commit atılmaz; iş her zaman ayrı bir branch'te yapılır.
- Release akışı: `main` → PR → `production`, ardından `versionCode` / `versionName` artırılır, etiket atılır, GitHub Release oluşturulur.

## Derleme ve test

```
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```

Room şeması `app/schemas` altında tutulur; veritabanı değişikliğinde sürüm artırılır ve migration yazılır.
