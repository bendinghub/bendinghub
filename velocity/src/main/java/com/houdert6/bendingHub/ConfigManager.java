package com.houdert6.bendingHub;

import com.moandjiezana.toml.Toml;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static Toml toml;

    static void initialize(Path dataFolder, Logger logger) {
        Path tomlConfig = dataFolder.resolve("config.toml");
        if (!Files.exists(tomlConfig)) {
            try {
                Files.createDirectories(dataFolder);
                try (InputStream in = ConfigManager.class.getClassLoader().getResourceAsStream("config.toml"); FileOutputStream out = new FileOutputStream(tomlConfig.toFile())) {
                    if (in == null) {
                        throw new FileNotFoundException("config.toml resource not found in plugin jar");
                    }
                    in.transferTo(out);
                }
            } catch (IOException e) {
                logger.error("Failed to save default bendinghub proxy config", e);
                return;
            }
        }
        try {
            toml = new Toml().read(new InputStreamReader(Files.newInputStream(tomlConfig)));
        } catch (IOException e) {
            logger.error("Failed to read bendinghub proxy config: ", e);
        }
    }

    /**
     * Gets a toml config
     */
    public static Toml toml() {
        return toml;
    }
}
