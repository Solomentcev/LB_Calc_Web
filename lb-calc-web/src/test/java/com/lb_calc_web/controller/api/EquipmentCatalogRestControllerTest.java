package com.lb_calc_web.controller.api;

import com.lb_calc_web.entity.EquipmentCatalogEntity;
import com.lb_calc_web.handler.RestExceptionHandler;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Проверяет HTTP-ответы REST API каталога оборудования. */
@WebMvcTest(EquipmentCatalogRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RestExceptionHandler.class)
class EquipmentCatalogRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EquipmentCatalogService service;

    @MockBean
    private JwtService jwtService;

    @Test
    void getUnknownPosition_shouldReturnNotFound() throws Exception {
        when(service.get("missing"))
                .thenThrow(new NoSuchElementException("Оборудование не найдено: missing"));

        mockMvc.perform(get("/api/v1/equipment-catalog/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message")
                        .value("Запрашиваемый объект не найден"));
    }

    @Test
    void setDefaultForUnknownPosition_shouldReturnNotFound() throws Exception {
        when(service.setDefault("missing", true))
                .thenThrow(new NoSuchElementException("Оборудование не найдено: missing"));

        mockMvc.perform(patch("/api/v1/equipment-catalog/missing/default")
                        .param("defaultPosition", "true"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void setDefaultWithInvalidState_shouldReturnBadRequest() throws Exception {
        when(service.setDefault("inactive", true))
                .thenThrow(new IllegalArgumentException(
                        "Деактивированная модель не может быть моделью по умолчанию"));

        mockMvc.perform(patch("/api/v1/equipment-catalog/inactive/default")
                        .param("defaultPosition", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message")
                        .value("Некорректный запрос"));
    }

    @Test
    void createWithInvalidCatalogData_shouldReturnBadRequest() throws Exception {
        when(service.create(any(EquipmentCatalogEntity.class)))
                .thenThrow(new IllegalArgumentException("Некорректная категория"));

        mockMvc.perform(post("/api/v1/equipment-catalog")
                        .contentType("application/json")
                        .content("""
                                {
                                  "catalogPositionId": "TEST",
                                  "category": "INVALID",
                                  "name": "Test",
                                  "description": "",
                                  "width": 100,
                                  "height": 100,
                                  "depth": 100,
                                  "displayWidth": 0,
                                  "displayHeight": 0,
                                  "minimumModuleWidth": 0,
                                  "supportsBarcode1d": false,
                                  "supportsBarcode2d": false,
                                  "supportsRfid": false,
                                  "supportsPrint": false,
                                  "active": true,
                                  "defaultPosition": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void deleteUnknownPosition_shouldReturnNotFound() throws Exception {
        doThrow(new NoSuchElementException("Оборудование не найдено: missing"))
                .when(service).delete("missing");

        mockMvc.perform(delete("/api/v1/equipment-catalog/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));
    }
}
