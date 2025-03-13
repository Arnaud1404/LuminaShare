package pdl.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import pdl.backend.FileHandler.FileController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.FileHandler;

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

        File imageFile = FileController.get_file("test_certain_est_test.jpg", "src/main/resources/images_test");
        // FileController.get_file("test.png", "src/main/resources/images_test")
        System.out.println("dossier temp " + tempDir.toFile().exists());
        InputStream input = new FileInputStream(imageFile);

        Files.copy(input, tempDir, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("dossier temp " + tempDir.toFile().exists());
        System.out.println("chemin " + tempDir.toString());

        System.out.println("chemin apres" + tempDir.toString());
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

        assertEquals(1, count);
    }
    /*
     * // Vérifiez que les fichiers non-images sont ignorés .
     * 
     * @Test
     * void testIgnoreInvalidFiles() throws IOException {
     * File textFile = new File(tempDir.toFile(), "test.txt");
     * Files.write(textFile.toPath(), "ce n'est pas une image".getBytes());
     * 
     * File imageFile = new File(tempDir.toFile(), "test_service.jpg");
     * Files.write(imageFile.toPath(), new byte[] { (byte) 0xFF, (byte) 0xD8, (byte)
     * 0xFF }); // Simule un fichier JPEG
     * 
     * System.out.println("size avant loading " + imageDao.retrieveAll().size());
     * 
     * imageLoaderService.loadImagesOnStartup(tempDir.toString());
     * 
     * File[] files = tempDir.toFile().listFiles();
     * if (files != null) {
     * for (File file : files) {
     * // Vérifie que le fichier est une image valide
     * if (file.isFile()) {
     * System.out.println("file : " + file.getName());
     * }
     * }
     * }
     * System.out.println("size avant après " + imageDao.retrieveAll().size());
     * 
     * // Vérification : Seuls les fichiers images doivent être chargés
     * assertEquals(1, imageDao.retrieveAll().size());
     * }
     */
}