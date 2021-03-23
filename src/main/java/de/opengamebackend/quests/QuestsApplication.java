package de.opengamebackend.quests;

import de.opengamebackend.util.EnableOpenGameBackendUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@EnableOpenGameBackendUtils
@ConfigurationPropertiesScan
public class QuestsApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuestsApplication.class, args);
	}

}
