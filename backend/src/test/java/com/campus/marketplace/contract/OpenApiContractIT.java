package com.campus.marketplace.contract;

import com.campus.marketplace.integration.IntegrationTestBase;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 契约测试：校验 OpenAPI 文档有效且包含路径
 */
class OpenApiContractIT extends IntegrationTestBase {

    @Test
    @DisplayName("OpenAPI 文档可用且包含路径")
    void openApiIsValidAndHasPaths() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(json, null, options);
        OpenAPI openAPI = parseResult.getOpenAPI();

        assertThat(openAPI).withFailMessage("OpenAPI 解析失败: %s", parseResult.getMessages()).isNotNull();
        assertThat(openAPI.getPaths()).isNotNull();
        assertThat(openAPI.getPaths().keySet()).isNotEmpty();
    }
}
