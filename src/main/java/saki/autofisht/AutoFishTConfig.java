package saki.autofisht;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutoFishTConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("autofisht");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("autofisht.json");

    public boolean jumpEnabled = true;
    public boolean spin360Enabled = true;
    public boolean noLookEnabled = true;

    private static AutoFishTConfig instance;

    public static AutoFishTConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static AutoFishTConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                AutoFishTConfig loaded = GSON.fromJson(reader, AutoFishTConfig.class);
                if (loaded != null) {
                    LOGGER.info("[AutoFishT] Config loaded from {}", CONFIG_PATH);
                    return loaded;
                }
            } catch (IOException e) {
                LOGGER.warn("[AutoFishT] Failed to read config, using defaults.", e);
            }
        }
        AutoFishTConfig fresh = new AutoFishTConfig();
        fresh.save();
        return fresh;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            LOGGER.warn("[AutoFishT] Failed to save config.", e);
        }
    }
}