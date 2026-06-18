package de.hiwiapp;

import de.hiwiapp.model.Role;
import de.hiwiapp.model.User;
import de.hiwiapp.repository.UserRepository;
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
			// Admin erstellen
			if (repo.findByUsername("admin").isEmpty()) {
				User admin = new User();
				admin.setUsername("admin");
				admin.setPassword(encoder.encode("123"));
				admin.setRole(Role.ADMIN);
				repo.save(admin);
			}

			if (repo.findByUsername("betreuer").isEmpty()) {

				User betreuer = new User();
				betreuer.setUsername("betreuer");
				betreuer.setPassword(encoder.encode("123"));
				betreuer.setRole(Role.BETREUER);
				betreuer.setVorname("Bernd");      // WICHTIG
				betreuer.setNachname("Betreuer");  // WICHTIG
				repo.save(betreuer);

			}

			// Student erstellen (mit Excel-Werten)
			if (repo.findByUsername("student").isEmpty()) {
				User student = new User();
				student.setUsername("student");
				student.setPassword(encoder.encode("123"));
				student.setRole(Role.STUDENT);
				student.setSollStunden(30.0); // EXCEL SOLL-WERT
				student.setVorname("Max");
				student.setNachname("Mustermann");
				repo.save(student);
				System.out.println("Test-Student angelegt mit 30h Soll.");

			}



		};
	}
}