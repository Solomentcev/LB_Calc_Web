package com.lb_calc_web.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Проверяет передачу признака явно заданной комплектации через REST JSON. */
class EquipmentSelectionDtoJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void lcDto_shouldAcceptEquipmentProvidedButNotSerializeIt() throws Exception {
        String request = """
                {
                  "equipmentProvided": true,
                  "selectedEquipment": []
                }
                """;

        LCDTO dto = objectMapper.readValue(request, LCDTO.class);
        String response = objectMapper.writeValueAsString(dto);

        assertTrue(dto.isEquipmentProvided());
        assertFalse(response.contains("equipmentProvided"));
    }

    @Test
    void lbcDto_shouldAcceptEquipmentProvidedButNotSerializeIt() throws Exception {
        String request = """
                {
                  "equipmentProvided": true,
                  "selectedEquipment": []
                }
                """;

        LBCDTO dto = objectMapper.readValue(request, LBCDTO.class);
        String response = objectMapper.writeValueAsString(dto);

        assertTrue(dto.isEquipmentProvided());
        assertFalse(response.contains("equipmentProvided"));
    }
}
