package com.atsresume;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AtsResumeBuilderApplication {

	public static void main(String[] args) {
		SpringApplication.run(AtsResumeBuilderApplication.class, args);
	}

}
