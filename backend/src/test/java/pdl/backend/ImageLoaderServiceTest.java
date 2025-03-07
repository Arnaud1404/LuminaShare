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
    //Test si le dossier images n'existe pas 
    @Test
    void testFolderNotFound() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> imageLoaderService.loadImagesOnStartup());
        assertEquals("Erreur : Le dossier 'images' est introuvable. Assurez-vous qu'il existe dans le répertoire de lancement du serveur.", exception.getMessage());
    }
   //Teste le chargement des images valide
    @Test
    void testLoadValidImages() throws IOException {
        File imageFile = new File(tempDir.toFile(), "test.jpg");
        Files.write(imageFile.toPath(), new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}); // Simule un fichier JPEG
       // Création de plusieurs images valides dans le dossier temporaire
       String[] validExtensions = {"jpg", "jpeg", "png"};
       int numImages = 10; // Changez ce nombre pour tester avec plus d'images

       for (int i = 1; i <= numImages; i++) {
           String extension = validExtensions[i % validExtensions.length]; // Alterner jpg, jpeg, png
           File imageFile = new File(tempDir.toFile(), "image" + i + "." + extension);
        
          System.setProperty("images", tempDir.toString());
          imageLoaderService.loadImagesOnStartup();

          assertEquals(numImages+1, imageDao.retrieveAll().size());
        }
    }    
    //Vérifiez que les fichiers non-images sont ignorés .
    @Test
    void testIgnoreInvalidFiles() throws IOException {
        File textFile = new File(tempDir.toFile(), "test.txt");
        Files.write(textFile.toPath(), "This is not an image".getBytes());
        
        System.setProperty("images", tempDir.toString());
        imageLoaderService.loadImagesOnStartup();

        assertEquals(0, imageDao.retrieveAll().size());
        // Vérification : Seuls les 5 fichiers images doivent être chargés
        assertEquals(11, imageDao.retrieveAll().size());//5 c'est plus l'image par défaut 
    }
}