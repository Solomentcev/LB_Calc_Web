package com.lb_calc_web.controller.api;

import com.lb_calc_web.handler.RestExceptionHandler;
import com.lb_calc_web.service.EquipmentCatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
}
