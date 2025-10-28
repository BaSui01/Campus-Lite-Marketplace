package com.campus.marketplace.common.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Load variables from a .env file (project root or its parents) into Spring Environment.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    private static final String SOURCE_NAME = "dotenv";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path envPath = resolveEnvPath();
        if (envPath == null) return;
        Map<String, Object> props = loadEnv(envPath);
        if (props.isEmpty()) return;
        environment.getPropertySources().addFirst(new MapPropertySource(SOURCE_NAME, props));
    }

    private Path resolveEnvPath() {
        // 1) explicit ENV_FILE
        String explicit = System.getenv("ENV_FILE");
        if (explicit != null && !explicit.isBlank()) {
            Path p = Path.of(explicit);
            if (Files.isRegularFile(p)) return p;
        }
        // 2) search current dir upwards
        Path cur = Path.of("").toAbsolutePath();
        for (int i = 0; i < 6 && cur != null; i++, cur = cur.getParent()) {
            Path candidate = cur.resolve(".env");
            if (Files.isRegularFile(candidate)) return candidate;
        }
        return null;
    }

    private Map<String, Object> loadEnv(Path envFile) {
        Map<String, Object> map = new HashMap<>();
        List<String> lines;
        try {
            lines = Files.readAllLines(envFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return map;
        }
        for (String raw : lines) {
            if (raw == null) continue;
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.startsWith("export ")) line = line.substring(7).trim();
            int idx = line.indexOf('=');
            if (idx <= 0) continue;
            String key = line.substring(0, idx).trim();
            String val = line.substring(idx + 1).trim();
            if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                val = val.substring(1, val.length() - 1);
            }
            // put both UPPER_UNDERSCORE and dot.notation for Spring properties
            if (!key.isEmpty()) {
                map.put(key, val);
                String dotKey = key.toLowerCase().replace('_', '.');
                map.put(dotKey, val);
            }
        }
        return map;
    }

    @Override
    public int getOrder() {
        // early so .env values have high precedence
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
