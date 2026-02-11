package de.hiwiapp.hiwiapp;

import de.hiwiapp.hiwiapp.model.Role;
import de.hiwiapp.hiwiapp.model.User;
import de.hiwiapp.hiwiapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class HiwiappApplication {

	public static void main(String[] args) {
		SpringApplication.run(HiwiappApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(UserRepository repo, PasswordEncoder encoder) {
		return args -> {
			if (repo.count() == 0) {
				// Admin erstellen
				User admin = new User();
				admin.setUsername("admin");
				admin.setPassword(encoder.encode("123"));
				admin.setRole(Role.ADMIN); // <-- ENUM verwenden
				repo.save(admin);

				// Betreuer erstellen
				User betreuer = new User();
				betreuer.setUsername("betreuer");
				betreuer.setPassword(encoder.encode("123"));
				betreuer.setRole(Role.BETREUER); // <-- ENUM
				repo.save(betreuer);

				// Student erstellen
				User student = new User();
				student.setUsername("student");
				student.setPassword(encoder.encode("123"));
				student.setRole(Role.STUDENT); // <-- ENUM
				repo.save(student);

				System.out.println("Datenbank initialisiert: admin, betreuer, student (PW: 123)");
			}
		};
	}

}