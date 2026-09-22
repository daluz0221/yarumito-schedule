package co.edu.ieruralyarumito.backend.controller;

import co.edu.ieruralyarumito.backend.dto.AreaResponse;
import co.edu.ieruralyarumito.backend.service.AreaService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AreaControllerTest {

    @Mock
    private AreaService areaService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AreaController(areaService))
                .build();
    }

    @Test
    void listarAreas_debeRetornarOk() throws Exception {
        UUID areaId = UUID.randomUUID();
        AreaResponse response = new AreaResponse();
        response.setId(areaId);
        response.setNombre("Matemáticas");
        response.setCodigo("MAT");

        when(areaService.listarAreas()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/areas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(areaId.toString()))
                .andExpect(jsonPath("$[0].nombre").value("Matemáticas"))
                .andExpect(jsonPath("$[0].codigo").value("MAT"));
    }
}
