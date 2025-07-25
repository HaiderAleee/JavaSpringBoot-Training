package com.redmath.GymManagementApp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GymManagementAppApplication {
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		System.setProperty("JWT_KEY", dotenv.get("JWT_KEY"));
		SpringApplication.run(GymManagementAppApplication.class, args);
	}
}
