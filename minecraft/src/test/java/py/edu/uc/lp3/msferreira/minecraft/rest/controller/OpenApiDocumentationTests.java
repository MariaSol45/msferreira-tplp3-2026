package py.edu.uc.lp3.msferreira.minecraft.rest.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void elDocumentoOpenApiTieneElTituloLaDescripcionYLaVersion() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("Minecraft API REST"))
                .andExpect(jsonPath("$.info.description", containsString("CRUD de entidades")))
                .andExpect(jsonPath("$.info.version").value("0.0.1-SNAPSHOT"));
    }

    @Test
    void elDocumentoOpenApiContieneTodosLosEndpointsExistentes() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/'].get").exists())
                .andExpect(jsonPath("$.paths['/entidades'].get").exists())
                .andExpect(jsonPath("$.paths['/entidades'].post").exists())
                .andExpect(jsonPath("$.paths['/entidades/{id}'].get").exists())
                .andExpect(jsonPath("$.paths['/entidades/{id}'].put").exists())
                .andExpect(jsonPath("$.paths['/entidades/{id}'].delete").exists())
                .andExpect(jsonPath("$.paths['/entidad'].get").exists())
                .andExpect(jsonPath("$.paths['/entidad/atacar'].get").exists())
                .andExpect(jsonPath("$.paths['/ui'].get").exists())
                .andExpect(jsonPath("$.paths['/explosiones/simular'].post").exists())
                .andExpect(jsonPath("$.paths['/api/objetos-externos'].get").exists())
                .andExpect(jsonPath("$.paths['/explosiones/validar'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/explosiones/validar'].post.responses['422']").exists())
                .andExpect(jsonPath("$.paths['/jugadas/explosion'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/jugadas/explosion'].post.responses['502']").exists())
                .andExpect(jsonPath("$.paths['/jugadas'].get").exists())
                .andExpect(jsonPath("$.paths['/jugadas/{id}'].get").exists());
    }

    @Test
    void elDocumentoOpenApiDocumentaLosCodigosRealesYLosParametros() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/entidades'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/entidades/{id}'].delete.responses['204']").exists())
                .andExpect(jsonPath("$.paths['/entidades/{id}'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/explosiones/simular'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/explosiones/simular'].post.responses['422']").exists())
                .andExpect(jsonPath("$.paths['/api/objetos-externos'].get.responses['502']").exists())
                .andExpect(content().string(containsString("Rango de detección para entidades hostiles")))
                .andExpect(content().string(containsString("tiempo transcurrido")))
                .andExpect(content().string(containsString("Objetos externos")));
    }

    @Test
    void swaggerUiEstaDisponible() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Swagger UI")));
    }
}
