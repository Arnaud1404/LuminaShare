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
        // Utilisation d'un dossier temporaire pour le test afin de ne pas modifier l'état réel
        String testFolderPath = "src/test/resources/test_images"; // Chemin temporaire pour éviter d'impacter le dossier réel
        File testFolder = new File(testFolderPath);

        // S'assurer que le dossier n'existe pas avant le test (nettoyage)
        if (testFolder.exists()) {
            testFolder.delete(); // Supprime le dossier s'il existe déjà
        }

        // Configurer le service avec ce dossier temporaire (via une méthode ou un mock si nécessaire)
        // Si ImageService ne permet pas de passer un dossier dynamiquement ici, utilisez la surcharge existante
        imageLoaderService.loadImagesOnStartup(testFolderPath);

        // Vérifier que le dossier a été créé
        assertTrue(testFolder.exists(), "Le dossier devrait avoir été créé par loadImagesOnStartup");
        assertTrue(testFolder.isDirectory(), "Le chemin créé devrait être un dossier");

        // Vérifier que la méthode ne lève pas d'exception
        assertDoesNotThrow(() -> imageLoaderService.loadImagesOnStartup(testFolderPath),
                "loadImagesOnStartup ne devrait pas lever d'exception");

        // Nettoyage après le test (optionnel)
        testFolder.delete();
    }

    // Teste le chargement des images valide
    @Test
    void testLoadValidImages() throws IOException {
        File imageFile = new File(tempDir.toFile(), "test_service.jpg");
        Files.write(imageFile.toPath(), new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }); // Simule un fichier JPEG

        // Création de plusieurs images valides dans le dossier temporaire
        String[] validExtensions = { "jpg", "jpeg", "png" };
        int numImages = 3; // nombre d'extention

        for (int i = 0; i < numImages; i++) { // pour créer une image de chaque extention
            String extension = validExtensions[i];
            File tempImage = new File(tempDir.toFile(), "image" + i + "." + extension);
            Files.write(tempImage.toPath(), new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }); // Simule un fichier
                                                                                                   // JPEG
        }

        System.setProperty("images", tempDir.toString()); // Définir le bon chemin une seule fois
        imageLoaderService.loadImagesOnStartup(tempDir.toString());

        File[] files = tempDir.toFile().listFiles();
        int count = 0;
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    count++;
                }
            }
        }
        System.out.println("size avant loading " + imageDao.retrieveAll().size());

        assertEquals(numImages + 1, count); // +1 car la ligne 47
    }

    // Vérifiez que les fichiers non-images sont ignorés .
    @Test
    void testIgnoreInvalidFiles() throws IOException {
        File textFile = new File(tempDir.toFile(), "test.txt");
        Files.write(textFile.toPath(), "ce n'est pas une image".getBytes());

        File imageFile = new File(tempDir.toFile(), "test_service.jpg");
        Files.write(imageFile.toPath(), new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }); // Simule un fichier JPEG

        System.out.println("size avant loading " + imageDao.retrieveAll().size());

        imageLoaderService.loadImagesOnStartup(tempDir.toString());

        File[] files = tempDir.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                // Vérifie que le fichier est une image valide
                if (file.isFile()) {
                    System.out.println("file : " + file.getName());
                }
            }
        }
        System.out.println("size avant après " + imageDao.retrieveAll().size());

        // Vérification : Seuls les fichiers images doivent être chargés
        assertEquals(1, imageDao.retrieveAll().size());
    }
}