package com.hisaab_khata.hisaab_khata.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hisaab_khata.hisaab_khata.exception.BusinessValidationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock parser for Phase 4D. Returns 3 sample lines regardless of PDF content.
 * Replace with real OCR/PDF parser later.
 * 
 * Supports scenario-based parsing via parseWithScenario() method for testing.
 */
@Component
public class MockInvoiceParser implements InvoiceParser {

    public static final String PARSER_KEY = "MOCK:v0";
    private static final String SCENARIOS_FILE = "mock-parses/mock_purchase_scenarios.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ParsedInvoice parse(byte[] pdfBytes) {
        List<ParsedLineItem> lines = List.of(
                ParsedLineItem.builder()
                        .lineNo(1)
                        .rawName("Basmati Rice 5 Kg")
                        .quantity(new BigDecimal("10"))
                        .unit("PCS")
                        .unitPrice(new BigDecimal("450.00"))
                        .lineAmount(new BigDecimal("4500.00"))
                        .parseConfidence(new BigDecimal("0.95"))
                        .build(),
                ParsedLineItem.builder()
                        .lineNo(2)
                        .rawName("Refined Oil 1 Ltr")
                        .quantity(new BigDecimal("20"))
                        .unit("PCS")
                        .unitPrice(new BigDecimal("180.50"))
                        .lineAmount(new BigDecimal("3610.00"))
                        .parseConfidence(new BigDecimal("0.92"))
                        .build(),
                ParsedLineItem.builder()
                        .lineNo(3)
                        .rawName("Sugar 1 Kg")
                        .quantity(new BigDecimal("50"))
                        .unit("PCS")
                        .unitPrice(new BigDecimal("48.00"))
                        .lineAmount(new BigDecimal("2400.00"))
                        .parseConfidence(new BigDecimal("0.98"))
                        .build()
        );
        return ParsedInvoice.builder()
                .docKind("PDF")
                .parserKey(PARSER_KEY)
                .lines(lines)
                .build();
    }

    /**
     * Parses invoice using a scenario from the JSON fixture file.
     * 
     * @param pdfBytes PDF bytes (ignored, kept for interface compatibility)
     * @param scenario Scenario key from JSON fixture (e.g., "case0", "case3_name_variation")
     * @return ParsedInvoice with lines from the scenario
     * @throws BusinessValidationException if scenario is not found
     */
    public ParsedInvoice parseWithScenario(byte[] pdfBytes, String scenario) {
        try {
            ClassPathResource resource = new ClassPathResource(SCENARIOS_FILE);
            if (!resource.exists()) {
                throw new BusinessValidationException(
                        "Mock scenarios file not found: " + SCENARIOS_FILE, "SCENARIOS_FILE_NOT_FOUND");
            }

            InputStream inputStream = resource.getInputStream();
            JsonNode rootNode = objectMapper.readTree(inputStream);

            if (!rootNode.has(scenario)) {
                throw new BusinessValidationException(
                        "Unknown mock scenario: " + scenario, "UNKNOWN_SCENARIO");
            }

            JsonNode scenarioNode = rootNode.get(scenario);
            String docKind = scenarioNode.get("docKind").asText("PDF");
            JsonNode linesNode = scenarioNode.get("lines");

            List<ParsedLineItem> lines = new ArrayList<>();
            for (JsonNode lineNode : linesNode) {
                int lineNo = lineNode.get("lineNo").asInt();
                String rawName = lineNode.get("rawName").asText();
                BigDecimal quantity = new BigDecimal(lineNode.get("quantity").asText());
                String unit = lineNode.get("unit").asText();
                BigDecimal unitPrice = new BigDecimal(lineNode.get("unitPrice").asText());

                // Compute lineAmount = quantity * unitPrice
                BigDecimal lineAmount = quantity.multiply(unitPrice)
                        .setScale(2, RoundingMode.HALF_UP);

                // Set parseConfidence = 0.95 if not present
                BigDecimal parseConfidence = new BigDecimal("0.95");
                if (lineNode.has("parseConfidence")) {
                    parseConfidence = new BigDecimal(lineNode.get("parseConfidence").asText());
                }

                ParsedLineItem line = ParsedLineItem.builder()
                        .lineNo(lineNo)
                        .rawName(rawName)
                        .quantity(quantity)
                        .unit(unit)
                        .unitPrice(unitPrice)
                        .lineAmount(lineAmount)
                        .parseConfidence(parseConfidence)
                        .build();
                lines.add(line);
            }

            return ParsedInvoice.builder()
                    .docKind(docKind)
                    .parserKey(PARSER_KEY)
                    .lines(lines)
                    .build();

        } catch (IOException e) {
            throw new BusinessValidationException(
                    "Failed to load mock scenarios: " + e.getMessage(), "SCENARIOS_LOAD_ERROR");
        } catch (BusinessValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessValidationException(
                    "Failed to parse scenario: " + e.getMessage(), "SCENARIO_PARSE_ERROR");
        }
    }
}
