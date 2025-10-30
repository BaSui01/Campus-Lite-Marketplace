package com.campus.marketplace.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.env.MockEnvironment;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DotenvEnvironmentPostProcessorTest {

    private final DotenvEnvironmentPostProcessor processor = new DotenvEnvironmentPostProcessor();

    @Test
    @DisplayName("默认加载项目 .env 并保持最高优先级")
    void loadsProjectEnvWithHighPrecedence() {
        MockEnvironment environment = new MockEnvironment();
        environment.getPropertySources().addLast(new MapPropertySource("existing", Map.of("app.name", "fallback")));

        processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        MapPropertySource first = (MapPropertySource) environment.getPropertySources().iterator().next();
        assertThat(first.getName()).isEqualTo("dotenv");
        assertThat(first.getProperty("APP_NAME")).isEqualTo("campus-marketplace");
        assertThat(first.getProperty("app.name")).isEqualTo("campus-marketplace");

        MapPropertySource existing = (MapPropertySource) environment.getPropertySources().get("existing");
        assertThat(existing.getProperty("app.name")).isEqualTo("fallback");
    }

    @Test
    @DisplayName("loadEnv 可解析引号与 export 语法")
    @SuppressWarnings("unchecked")
    void loadEnvParsesQuotedValues() throws Exception {
        Path envFile = Files.createTempFile("dotenv-case", ".env");
        Files.writeString(envFile, "EXPORT_VAR=\"Value\"\nexport nested_key= spaced \n#comment\nPLAIN=ok\n");

        Method loadEnv = DotenvEnvironmentPostProcessor.class.getDeclaredMethod("loadEnv", Path.class);
        loadEnv.setAccessible(true);

        Map<String, Object> props = (Map<String, Object>) loadEnv.invoke(processor, envFile);

        assertThat(props.get("EXPORT_VAR")).isEqualTo("Value");
        assertThat(props.get("nested.key")).isEqualTo("spaced");
        assertThat(props.get("plain")).isEqualTo("ok");
    }
}
