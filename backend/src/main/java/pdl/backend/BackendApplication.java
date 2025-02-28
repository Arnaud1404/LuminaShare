package pdl.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;

@SpringBootApplication
public class BackendApplication {

@Component
public class ImageInitializer implements CommandLineRunner {

    private static final String IMAGE_FOLDER = "backend/src/main/resources/images"; 

    @Autowired
    private ImageService imageService; // Service pour charger les images

    @Override
    public void run(String... args) throws Exception {
        File folder = new File(IMAGE_FOLDER);

        // Si le dossier n'existe pas, lever une exception
        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("Le dossier " + IMAGE_FOLDER + " est introuvable. Veuillez le créer.");
        }

        // Charger les images dans la base de données
        imageService.loadImagesFromDirectory(IMAGE_FOLDER);
    }
}
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
