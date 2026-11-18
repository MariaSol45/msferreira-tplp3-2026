package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import py.edu.uc.lp3.msferreira.minecraft.rest.exception.ApiExternaExceptionHandler;
import py.edu.uc.lp3.msferreira.minecraft.service.ApiExternaException;
import py.edu.uc.lp3.msferreira.minecraft.service.ApiExternaService;
import py.edu.uc.lp3.msferreira.minecraft.service.ApiExternaService.ObjetoExterno;

class ObjetoExternoControllerTests {

    private final ApiExternaService apiExternaService = mock(ApiExternaService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new ObjetoExternoController(apiExternaService))
            .setControllerAdvice(new ApiExternaExceptionHandler())
            .build();

    @Test
    void devuelveLaListaDeObjetosExternosEnJson() throws Exception {
        when(apiExternaService.listarObjetos()).thenReturn(List.of(
                new ObjetoExterno("1", "Google Pixel 6 Pro", Map.of("color", "Cloudy White")),
                new ObjetoExterno("2", "Apple iPhone 12 Mini", null)));

        mockMvc.perform(get("/api/objetos-externos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Google Pixel 6 Pro"))
                .andExpect(jsonPath("$[0].data.color").value("Cloudy White"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].data").value(org.hamcrest.Matchers.nullValue()));

        verify(apiExternaService).listarObjetos();
    }

    @Test
    void falloDeLaApiExternaDevuelveBadGateway() throws Exception {
        when(apiExternaService.listarObjetos())
                .thenThrow(new ApiExternaException("No se pudo consultar la API externa: timeout"));

        mockMvc.perform(get("/api/objetos-externos"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("Error al consultar la API externa"))
                .andExpect(jsonPath("$.estado").value(502));
    }
}
