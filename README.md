# BreadMotion (Fabric, Minecraft 26.3)

İstemci tarafı (client-only) mod: birinci şahıs el/eşya animasyonlarını yumuşak, yaylı ve "hamur gibi" yapar.
Minecraft 26.3 (obfuscation yok, Mojang isimleri), Java 25, Fabric Loader 0.19.5+, Fabric API gerekli.

## Animasyonlar
- **Equip Bounce** - eşya değiştirirken yeni eşya aşağıdan fırlayıp hafif aşar, yerine oturur; eskisi hızlanarak düşer.
- **Dough Punch** - vururken el/eşya hamur gibi uzar, çarpınca jöle gibi titreyip toparlanır. Boş elde ekstra ileri atılma. Elinde ekmek varsa %50 daha sünek.
- **Breathing** - boştayken el hafifçe kabaran hamur gibi nefes alır.
- **Sway** - kamera dönerken eşya biraz geriden, ağır ağır takip eder ve yatar.
- **Nibble** - yerken/içerken lokma lokma sıkışıp gevşer.

## Ayar
`config/breadmotion.json`: her animasyon için true/false + `intensity` (0.0 - 2.0).
Dosyayı oyun açıkken kaydedersen ~2 saniye içinde otomatik yeniden okunur. `enabled: false` hepsini kapatır.

## Jar nasıl alınır
**A) GitHub (en kolay, bilgisayara bir şey kurmadan):**
1. Yeni bir GitHub deposu aç, bu klasörün içeriğini yükle.
2. Actions sekmesi > `build` > çalışma bitince `BreadMotion-jar` dosyasını indir (içinde jar var).

**B) Kendi bilgisayarında:**
1. JDK 25 ve Gradle 9.6+ kur.
2. Bu klasörde `gradle wrapper` sonra `./gradlew build`
3. Jar: `build/libs/breadmotion-1.0.0.jar` (`-sources` olanı değil)

Jar'ı `mods` klasörüne at.
