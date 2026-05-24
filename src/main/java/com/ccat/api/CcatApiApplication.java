package com.ccat.api;

import com.ccat.api.config.JwtProperties;
import com.ccat.api.model.entity.User;
import com.ccat.api.model.enums.UserRole;
import com.ccat.api.model.enums.UserStatus;
import com.ccat.api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Slf4j
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(JwtProperties.class)
public class CcatApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CcatApiApplication.class, args);
	}

	@Bean
	CommandLineRunner seedAdmin(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			@Value("${app.admin.email}") String adminEmail,
			@Value("${app.admin.password}") String adminPassword
	) {
		return args -> {
			if (userRepository.existsByStrEmail(adminEmail)) {
				log.info("Admin account already exists — skipping seed");
				return;
			}

			User admin = new User();
			admin.setStrUuid(UUID.randomUUID().toString());
			admin.setStrEmail(adminEmail);
			admin.setStrPasswordHash(passwordEncoder.encode(adminPassword));
			admin.setStrFirstName("Admin");
			admin.setStrLastName("CCAT");
			admin.setEmRole(UserRole.ADMIN);
			admin.setEmStatus(UserStatus.ACTIVE);
			admin.setBEmailVerified(true);
			admin.setStrLocale("en");
			admin.setIntLoginCount(0);

			userRepository.save(admin);
			log.info("Admin account created: {}", adminEmail);
		};
	}
}
