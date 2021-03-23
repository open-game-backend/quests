package de.opengamebackend.quests;

import de.opengamebackend.util.EnableOpenGameBackendUtils;
import de.opengamebackend.util.config.ApplicationConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@EnableOpenGameBackendUtils
@ConfigurationPropertiesScan
public class QuestsApplication {
	@Bean
	@Profile("!test")
	public OpenAPI customOpenAPI(ApplicationConfig applicationConfig) {
		return new OpenAPI().info(new Info()
				.title("Open Game Backend Quests")
				.version(applicationConfig.getVersion())
				.description("Provides quests that can be generated for and completed by players.")
				.license(new License().name("MIT").url("https://github.com/open-game-backend/quests/blob/develop/LICENSE")));
	}

	public static void main(String[] args) {
		SpringApplication.run(QuestsApplication.class, args);
	}
}
