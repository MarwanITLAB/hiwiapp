package de.hiwiapp;

import de.hiwiapp.model.Role;
import de.hiwiapp.model.User;
import de.hiwiapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;

@SpringBootApplication
public class HiwiappApplication {

	public static void main(String[] args) {
		SpringApplication.run(HiwiappApplication.class, args);
	}

	/**
	 * Legt beim Start die nötigen Konten an – aber nur, wenn sie noch nicht existieren.
	 *  - app.demo-users=true  (lokal):  admin/betreuer/student mit Passwort "123"
	 *  - app.demo-users=false (Server): nur ein Admin, Passwort aus APP_ADMIN_PASSWORD
	 *                                   oder zufällig (steht dann einmalig im Log)
	 */
	@Bean
	CommandLineRunner initDatabase(UserRepository repo, PasswordEncoder encoder,
								   @Value("${app.demo-users:false}") boolean demoUsers,
								   @Value("${app.admin-password:}") String adminPasswort) {
		return args -> {
			// Admin erstellen
			if (repo.findByUsername("admin").isEmpty()) {
				String pw = adminPasswort;
				if (pw == null || pw.isBlank()) {
					pw = demoUsers ? "123" : zufallsPasswort();
					if (!demoUsers) {
						System.out.println("=====================================================");
						System.out.println(" Admin-Konto angelegt. Benutzer: admin  Passwort: " + pw);
						System.out.println(" Bitte nach dem ersten Login sofort ändern!");
						System.out.println("=====================================================");
					}
				}
				User admin = new User();
				admin.setUsername("admin");
				admin.setPassword(encoder.encode(pw));
				admin.setRole(Role.ADMIN);
				repo.save(admin);
			}

			if (!demoUsers) return; // Auf dem Server keine Test-Konten

			if (repo.findByUsername("betreuer").isEmpty()) {
				User betreuer = new User();
				betreuer.setUsername("betreuer");
				betreuer.setPassword(encoder.encode("123"));
				betreuer.setRole(Role.BETREUER);
				betreuer.setVorname("Bernd");
				betreuer.setNachname("Betreuer");
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

	private static String zufallsPasswort() {
		String zeichen = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
		SecureRandom r = new SecureRandom();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 14; i++) sb.append(zeichen.charAt(r.nextInt(zeichen.length())));
		return sb.toString();
	}
}
