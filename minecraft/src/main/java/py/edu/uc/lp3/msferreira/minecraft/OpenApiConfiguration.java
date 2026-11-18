package py.edu.uc.lp3.msferreira.minecraft;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

    @Bean
    OpenAPI minecraftOpenApi(@Value("${api.version:0.0.1-SNAPSHOT}") String version) {
        return new OpenAPI().info(new Info()
                .title("Minecraft API REST")
                .description("API REST del proyecto LP3 de Minecraft: CRUD de entidades persistidas en H2, "
                        + "simulación de explosiones, consultas transitorias de POO-08 y consulta de objetos "
                        + "externos mediante WebClient.")
                .version(version));
    }
}
