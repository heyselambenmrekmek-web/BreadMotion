package com.breadmotion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** config/breadmotion.json - oyun açıkken dosyayı kaydedersen otomatik yeniden okunur. */
public final class BreadConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("breadmotion.json");
    private static long lastModified = 0L;

    /** Tüm modu aç/kapat. */
    public boolean enabled = true;
    /** Eşya değiştirirken yaylanan/zıplayan çıkış. */
    public boolean equipBounce = true;
    /** Vururken hamur gibi sıkışıp uzama (squash & stretch). */
    public boolean doughPunch = true;
    /** Boştayken elin hafifçe "kabaran hamur" gibi nefes alması. */
    public boolean breathing = true;
    /** Kamera dönerken eşyanın ağır ağır geriden gelmesi. */
    public boolean sway = true;
    /** Yemek/içerken lokma lokma titreşim. */
    public boolean nibble = true;
    /** Genel şiddet: 0.0 (yok) - 2.0 (abartılı). */
    public float intensity = 1.0f;

    public static BreadConfig load() {
        BreadConfig cfg = read();
        cfg.save();
        return cfg;
    }

    /** Dosya değiştiyse yenisini oku, değişmediyse mevcut ayarı döndür. */
    public static BreadConfig reloadIfChanged(BreadConfig current) {
        try {
            if (!Files.exists(PATH)) return current;
            long modified = Files.getLastModifiedTime(PATH).toMillis();
            if (modified == lastModified) return current;
            return read();
        } catch (Exception e) {
            return current;
        }
    }

    private static BreadConfig read() {
        BreadConfig cfg = new BreadConfig();
        try {
            if (Files.exists(PATH)) {
                try (Reader reader = Files.newBufferedReader(PATH)) {
                    BreadConfig parsed = GSON.fromJson(reader, BreadConfig.class);
                    if (parsed != null) cfg = parsed;
                }
                lastModified = Files.getLastModifiedTime(PATH).toMillis();
            }
        } catch (Exception e) {
            BreadMotionClient.LOGGER.warn("BreadMotion config okunamadı, varsayılanlar kullanılıyor", e);
        }
        cfg.intensity = Mth.clamp(cfg.intensity, 0.0f, 2.0f);
        return cfg;
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(PATH)) {
            GSON.toJson(this, writer);
        } catch (Exception e) {
            BreadMotionClient.LOGGER.warn("BreadMotion config kaydedilemedi", e);
            return;
        }
        try {
            lastModified = Files.getLastModifiedTime(PATH).toMillis();
        } catch (Exception ignored) {
        }
    }
}
