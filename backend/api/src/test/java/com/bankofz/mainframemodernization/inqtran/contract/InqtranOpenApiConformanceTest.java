package com.bankofz.mainframemodernization.inqtran.contract;

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
        "spring.datasource.url=jdbc:h2:mem:inqtran_contract;MODE=DB2;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always"
})
class InqtranOpenApiConformanceTest {

    private static final String INQTRAND_PATH = "/api/v1/accounts/{sortCode}/{accountNumber}/transactions/{date}/{time}/{reference}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void featureContractShouldDefineInqtrandOperationStructure() throws Exception {
        Path contractPath = Path.of("..", "..", "specs", "005b-inqtrand-transaction-inquiry-modernization", "contracts", "openapi.yaml");
        Map<String, Object> openApi = parseYaml(contractPath);

        assertInqtrandOperationContract(openApi, "TransactionDetailInquiryResponse", "TransactionDetail", "ErrorResponse");
    }

    @Test
    void runtimeOpenApiPublicationShouldDefineInqtrandOperationStructure() throws Exception {
        Path runtimePath = Path.of("src", "main", "resources", "openapi.yaml");
        Map<String, Object> openApi = parseYaml(runtimePath);

        assertInqtrandOperationContract(openApi, "InqtrandDetailResponse", "InqtranTransaction", "InqtranErrorResponse");
    }

    @Test
    void successPayloadShouldExposeApprovedFoundAndNotFoundDetailOutcomes() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/123456/00000001/transactions/20260728/143015/000000000123")
                .header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.transaction.transactionId").value("123456-00000001-20260728-143015-000000000123"));

        mockMvc.perform(get("/api/v1/accounts/123456/00000001/transactions/20990101/000000/999999999999")
                        .header("Authorization", "Bearer valid-inqacc-inquirer-token"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.found").value(false))
                .andExpect(jsonPath("$.transaction").isEmpty());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseYaml(Path path) throws Exception {
        String content = Files.readString(path);
        Object parsed = new Yaml().load(content);
        assertThat(parsed).isInstanceOf(Map.class);
        return (Map<String, Object>) parsed;
    }

    @SuppressWarnings("unchecked")
    private void assertInqtrandOperationContract(
            Map<String, Object> openApi,
            String successSchemaName,
            String transactionSchemaName,
            String errorSchemaName
    ) {
        Map<String, Object> paths = (Map<String, Object>) openApi.get("paths");
        assertThat(paths).containsKey(INQTRAND_PATH);

        Map<String, Object> inqtrandPathItem = (Map<String, Object>) paths.get(INQTRAND_PATH);
        assertThat(inqtrandPathItem).containsKey("get");

        Map<String, Object> get = (Map<String, Object>) inqtrandPathItem.get("get");
        List<Map<String, Object>> parameters = (List<Map<String, Object>>) get.get("parameters");
        assertThat(parameters).hasSize(5);

        assertParameter(parameters, "sortCode", "path", true, "string", "^[0-9]{6}$", null, null);
        assertParameter(parameters, "accountNumber", "path", true, "string", "^[0-9]{8}$", null, null);
        assertParameter(parameters, "date", "path", true, "string", "^[0-9]{8}$", null, null);
        assertParameter(parameters, "time", "path", true, "string", "^[0-9]{6}$", null, null);
        assertParameter(parameters, "reference", "path", true, "string", "^[0-9]{12}$", null, null);

        Map<String, Object> responses = (Map<String, Object>) get.get("responses");
        assertThat(responses).containsKeys("200", "400", "401", "403", "500");

        assertResponseSchemaRef(responses, "200", successSchemaName);
        assertResponseSchemaRef(responses, "400", errorSchemaName);
        assertResponseSchemaRef(responses, "500", errorSchemaName);

        Map<String, Object> components = (Map<String, Object>) openApi.get("components");
        Map<String, Object> schemas = (Map<String, Object>) components.get("schemas");

        Map<String, Object> successSchema = (Map<String, Object>) schemas.get(successSchemaName);
        List<String> required = (List<String>) successSchema.get("required");
        assertThat(required).contains("found", "transaction");

        Map<String, Object> properties = (Map<String, Object>) successSchema.get("properties");
        assertThat(properties).containsKeys("found", "transaction");

        Map<String, Object> transactionSchema = (Map<String, Object>) schemas.get(transactionSchemaName);
        assertThat(transactionSchema).isNotNull();
        Map<String, Object> transactionProperties = (Map<String, Object>) transactionSchema.get("properties");
        assertThat(transactionProperties).containsKeys("transactionId", "reference");

        Map<String, Object> transactionIdProperty = (Map<String, Object>) transactionProperties.get("transactionId");
        assertThat(transactionIdProperty.get("pattern"))
            .isEqualTo("^[0-9]{6}-[0-9]{8}-[0-9]{8}-[0-9]{6}-[0-9]{12}$");

        Map<String, Object> referenceProperty = (Map<String, Object>) transactionProperties.get("reference");
        assertThat(referenceProperty.get("pattern")).isEqualTo("^[0-9]{12}$");

        Map<String, Object> errorSchema = (Map<String, Object>) schemas.get(errorSchemaName);
        assertThat(errorSchema).isNotNull();
        List<String> errorRequired = (List<String>) errorSchema.get("required");
        assertThat(errorRequired).contains("code", "message", "correlationId");

        Map<String, Object> errorProperties = (Map<String, Object>) errorSchema.get("properties");
        assertThat(errorProperties).containsKey("correlationId");
        Map<String, Object> correlationIdProperty = (Map<String, Object>) errorProperties.get("correlationId");
        assertThat(correlationIdProperty.get("type")).isEqualTo("string");
        assertThat(correlationIdProperty.get("nullable")).isNotEqualTo(true);
    }

    @SuppressWarnings("unchecked")
    private void assertParameter(
            List<Map<String, Object>> parameters,
            String name,
            String in,
            boolean required,
            String type,
            String pattern,
            Integer minimum,
            Integer defaultValue
    ) {
        Map<String, Object> parameter = parameters.stream()
                .filter(item -> name.equals(item.get("name")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing parameter: " + name));

        assertThat(parameter.get("in")).isEqualTo(in);
        assertThat(parameter.get("required")).isEqualTo(required);

        Map<String, Object> schema = (Map<String, Object>) parameter.get("schema");
        assertThat(schema.get("type")).isEqualTo(type);
        if (pattern != null) {
            assertThat(schema.get("pattern")).isEqualTo(pattern);
        }
        if (minimum != null) {
            Number min = (Number) schema.get("minimum");
            assertThat(min.intValue()).isEqualTo(minimum);
        }
        if (defaultValue != null) {
            Number defaultNumber = (Number) schema.get("default");
            assertThat(defaultNumber.intValue()).isEqualTo(defaultValue);
        }
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
