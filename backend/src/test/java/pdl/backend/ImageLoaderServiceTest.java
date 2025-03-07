package pdl.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class ImageLoaderServiceTest {

    private ImageDao imageDao;
    private ImageService imageLoaderService;
    private int NumberImages = 10;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        imageDao = new ImageDao();
        imageLoaderService = new ImageService(imageDao);
    }

    // Test si le dossier images n'existe pas
    @Test
    void testFolderNotFound() {
        File imagesFolder = new File("src/main/resources/images"); // Remplace par le chemin correct si nécessaire

        if (!imagesFolder.exists()) {
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> imageLoaderService.loadImagesOnStartup());
            assertEquals(
                    "Erreur : Le dossier 'images' est introuvable. Assurez-vous qu'il existe dans le répertoire de lancement du serveur.",
                    exception.getMessage());
        } else {
            // Si le dossier existe, on ne teste pas l'exception
            assertDoesNotThrow(() -> imageLoaderService.loadImagesOnStartup());
        }
    }

    // Teste le chargement des images valide
    @Test
    void testLoadValidImages() throws IOException {
        File imageFile = new File(tempDir.toFile(), "test.jpg");
        Files.write(imageFile.toPath(), new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }); // Simule un fichier JPEG

        // Création de plusieurs images valides dans le dossier temporaire
        String[] validExtensions = { "jpg", "jpeg", "png" };
        int numImages = 3; // nombre d'extention

        for (int i = 0; i < numImages; i++) {
            String extension = validExtensions[i]; // Alterner jpg, jpeg, png
            File tempImage = new File(tempDir.toFile(), "image" + i + "." + extension);
            Files.write(tempImage.toPath(), new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }); // Simule un fichier
                                                                                                   // JPEG
        }

        System.setProperty("images", tempDir.toString()); // Définir le bon chemin une seule fois
        imageLoaderService.loadImagesOnStartup(tempDir.toString());

        for (Image imgs : imageDao.retrieveAll()) {
            System.out.println("name : " + imgs.getName());

        }

        assertEquals(numImages + 2, imageDao.retrieveAll().size()); // +2 car l'image test est chargé par défault dans
                                                                    // imageDao et la ligne 47
    }

    // Vérifiez que les fichiers non-images sont ignorés .
    @Test
    void testIgnoreInvalidFiles() throws IOException {
        File textFile = new File(tempDir.toFile(), "test.txt");
        Files.write(textFile.toPath(), "This is not an image".getBytes());

        System.setProperty("images", tempDir.toString());
        imageLoaderService.loadImagesOnStartup();

        // Vérification : Seuls les 5 fichiers images doivent être chargés
        assertEquals(NumberImages, imageDao.retrieveAll().size());// 5 c'est plus l'image par défaut
    }
}