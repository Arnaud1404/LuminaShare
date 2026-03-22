package pdl.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication
public class BackendApplication {
	public static void main(String[] args) {
		String dbUrl = System.getenv("DATABASE_URL");

		if (dbUrl == null || dbUrl.trim().isEmpty()) {
			System.err.println("FATAL ERROR: DATABASE_URL environment variable is undefined. Exiting.");
			System.exit(1);
		}

		if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
			try {
				java.net.URI dbUri = new java.net.URI(dbUrl);
				String username = dbUri.getUserInfo().split(":")[0];
				String password = dbUri.getUserInfo().split(":")[1];
				String dbUrlJdbc = "jdbc:postgresql://" + dbUri.getHost() + (dbUri.getPort() != -1 ? ":" + dbUri.getPort() : "") + dbUri.getPath();

				if (dbUri.getQuery() != null) {
					dbUrlJdbc += "?" + dbUri.getQuery();
				}

				System.setProperty("spring.datasource.url", dbUrlJdbc);
				System.setProperty("spring.datasource.username", username);
				System.setProperty("spring.datasource.password", password);
			} catch (Exception e) {
				System.err.println("FATAL ERROR: Malformed DATABASE_URL: " + e.getMessage());
				System.exit(1);
			}
		} else {
			System.setProperty("spring.datasource.url", dbUrl);
		}

		try {
			SpringApplication.run(BackendApplication.class, args);
			System.out.println("Service LuminaShare-Arnaud started successfully.");
		} catch (Exception e) {
			System.err.println("CRITICAL ERROR: Database connection or initialization failed.");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
