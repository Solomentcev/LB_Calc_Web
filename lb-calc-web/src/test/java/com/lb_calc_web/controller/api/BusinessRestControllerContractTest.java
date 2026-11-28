package com.lb_calc_web.controller.api;

import com.lb_calc_web.handler.RestExceptionHandler;
import com.lb_calc_web.security.jwt.JwtService;
import com.lb_calc_web.service.ALSService;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.service.EmployeeService;
import com.lb_calc_web.service.LBService;
import com.lb_calc_web.service.LBCService;
import com.lb_calc_web.service.LCService;
import com.lb_calc_web.service.ProjectService;
import com.lb_calc_web.service.export.ProjectExcelExportService;
import com.lb_calc_web.service.validation.size.SizeBounds;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Проверяет HTTP-контракт валидации основных бизнес REST API. */
@WebMvcTest(controllers = {
        ALSRestController.class,
        LBRestController.class,
        LCRestController.class,
        LBCRestController.class,
        ProjectRestController.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(RestExceptionHandler.class)
class BusinessRestControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ALSService alsService;
    @MockitoBean
    private EquipmentCatalogService equipmentCatalogService;
    @MockitoBean
    private EmployeeService employeeService;
    @MockitoBean
    private LBService lbService;
    @MockitoBean
    private LCService lcService;
    @MockitoBean
    private LBCService lbcService;
    @MockitoBean
    private ProjectService projectService;
    @MockitoBean
    private ProjectExcelExportService projectExcelExportService;
    @MockitoBean
    private SizeBounds sizeBounds;
    @MockitoBean
    private JwtService jwtService;

    @Test
    void als_shouldReturn400ForInvalidGeometry() throws Exception {
        assertInvalid("/api/v1/alss", """
                {"height":0,"width":500,"depth":500,"upperFrame":50,
                 "bottomFrame":50,"countCells":3,"lbList":[]}
                """);
    }

    @Test
    void lb_shouldReturn400ForInvalidGeometry() throws Exception {
        assertInvalid("/api/v1/lbs", """
                {"type":"TYPE1","height":1940,"width":500,"depth":500,
                 "upperFrame":50,"bottomFrame":50,"doorThickness":0,
                 "countCells":3,"directionDoorOpening":"LEFT",
                 "colorBody":"Blue","colorDoor":"White"}
                """);
    }

    @Test
    void lc_shouldReturn400ForInvalidGeometry() throws Exception {
        assertInvalid("/api/v1/lcs", """
                {"height":0,"width":300,"depth":500,
                 "upperFrame":50,"bottomFrame":50}
                """);
    }

    @Test
    void lbc_shouldReturn400ForInvalidGeometry() throws Exception {
        assertInvalid("/api/v1/lbcs", """
                {"type":"TYPE1","height":1940,"width":500,"depth":500,
                 "upperFrame":50,"bottomFrame":50,"doorThickness":20,
                 "countCells":0,"directionDoorOpening":"LEFT",
                 "colorBody":"Blue","colorDoor":"White"}
                """);
    }

    @Test
    void project_shouldCascadeValidationToAls() throws Exception {
        assertInvalid("/api/v1/projects", """
                {"name":"Тестовый проект","company":"Заказчик",
                 "alsList":[{"height":0,"width":500,"depth":500,
                 "upperFrame":50,"bottomFrame":50,"countCells":3,"lbList":[]}]}
                """);
    }

    private void assertInvalid(String uri, String json) throws Exception {
        mockMvc.perform(post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errors").isArray());
    }
}
