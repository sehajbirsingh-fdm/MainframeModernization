package com.bankofz.mainframemodernization.inqstmt.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:inqstmt_contract;MODE=DB2;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always"
})
class InqstmtOpenApiConformanceTest {

    private static final String STATEMENT_PATH = "/api/v1/accounts/{sortCode}/{accountNumber}/statements/{period}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void featureContractShouldDefineStatementOperationStructure() throws Exception {
        Path contractPath = Path.of("..", "..", "specs", "006-bank-statement-retrieval-modernization", "contracts", "openapi.yaml");
        Map<String, Object> openApi = parseYaml(contractPath);

        assertStatementOperationContract(openApi, "AccountStatementResponse", "ErrorResponse");
    }

    @Test
    void runtimeOpenApiPublicationShouldDefineStatementOperationStructure() throws Exception {
        Path runtimePath = Path.of("src", "main", "resources", "openapi.yaml");
        Map<String, Object> openApi = parseYaml(runtimePath);

        assertStatementOperationContract(openApi, "InqstmtAccountStatementResponse", "InqstmtErrorResponse");
    }

    @Test
    void successPayloadShouldExposeApprovedStatementFields() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/123456/00000001/statements/202607")
                        .header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.sortCode").value("123456"))
                .andExpect(jsonPath("$.accountNumber").value("00000001"))
                .andExpect(jsonPath("$.period").value("202607"))
                .andExpect(jsonPath("$.summary.periodFrom").value("20260701"))
                .andExpect(jsonPath("$.entries").isArray());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseYaml(Path path) throws Exception {
        String content = Files.readString(path);
        Object parsed = new Yaml().load(content);
        assertThat(parsed).isInstanceOf(Map.class);
        return (Map<String, Object>) parsed;
    }

    @SuppressWarnings("unchecked")
    private void assertStatementOperationContract(
            Map<String, Object> openApi,
            String successSchemaName,
            String errorSchemaName
    ) {
        Map<String, Object> paths = (Map<String, Object>) openApi.get("paths");
        assertThat(paths).containsKey(STATEMENT_PATH);

        Map<String, Object> statementPathItem = (Map<String, Object>) paths.get(STATEMENT_PATH);
        assertThat(statementPathItem).containsKey("get");

        Map<String, Object> get = (Map<String, Object>) statementPathItem.get("get");
        List<Map<String, Object>> parameters = (List<Map<String, Object>>) get.get("parameters");
        assertThat(parameters).hasSize(3);

        assertParameter(parameters, "sortCode", "path", true, "string", "^[0-9]{6}$");
        assertParameter(parameters, "accountNumber", "path", true, "string", "^[0-9]{8}$");
        assertParameter(parameters, "period", "path", true, "string", "^[0-9]{4}(0[1-9]|1[0-2])$");

        Map<String, Object> responses = (Map<String, Object>) get.get("responses");
        assertThat(responses).containsKeys("200", "400", "401", "403", "404", "500");

        assertResponseSchemaRef(responses, "200", successSchemaName);
        assertResponseSchemaRef(responses, "400", errorSchemaName);
        assertResponseSchemaRef(responses, "401", errorSchemaName);
        assertResponseSchemaRef(responses, "403", errorSchemaName);
        assertResponseSchemaRef(responses, "404", errorSchemaName);
        assertResponseSchemaRef(responses, "500", errorSchemaName);
    }

    @SuppressWarnings("unchecked")
    private void assertParameter(
            List<Map<String, Object>> parameters,
            String name,
            String in,
            boolean required,
            String type,
            String pattern
    ) {
        Map<String, Object> parameter = parameters.stream()
                .filter(item -> name.equals(item.get("name")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing parameter: " + name));

        assertThat(parameter.get("in")).isEqualTo(in);
        assertThat(parameter.get("required")).isEqualTo(required);

        Map<String, Object> schema = (Map<String, Object>) parameter.get("schema");
        assertThat(schema.get("type")).isEqualTo(type);
        assertThat(schema.get("pattern")).isEqualTo(pattern);
    }

    @SuppressWarnings("unchecked")
    private void assertResponseSchemaRef(Map<String, Object> responses, String statusCode, String expectedSchemaName) {
        Map<String, Object> response = (Map<String, Object>) responses.get(statusCode);
        Map<String, Object> content = (Map<String, Object>) response.get("content");
        assertThat(content).containsKey("application/json");

        Map<String, Object> applicationJson = (Map<String, Object>) content.get("application/json");
        Map<String, Object> schema = (Map<String, Object>) applicationJson.get("schema");
        assertThat(schema.get("$ref")).isEqualTo("#/components/schemas/" + expectedSchemaName);
    }
}
