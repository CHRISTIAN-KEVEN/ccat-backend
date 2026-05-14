package com.ccat.api;

import com.ccat.api.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(JwtProperties.class)
public class CcatApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CcatApiApplication.class, args);
	}

}
