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

   //Teste le chargement des images valide
    @Test
    void testLoadValidImages() throws IOException {
       // Création de plusieurs images valides dans le dossier temporaire
       String[] validExtensions = {"jpg", "jpeg", "png"};
       int numImages = 4; // Changez ce nombre pour tester avec plus d'images

       for (int i = 1; i <= numImages; i++) {
           String extension = validExtensions[i % validExtensions.length]; // Alterner jpg, jpeg, png
           File imageFile = new File(tempDir.toFile(), "image" + i + "." + extension);
        
           // Simuler le contenu binaire d'une image
           byte[] content;
           if (extension.equals("png")) {
              content = new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47}; // Signature PNG
            } else {
               content = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}; // Signature JPEG
            }

            Files.write(imageFile.toPath(), content);
        }

        // Définir le dossier temporaire comme source des images
        System.setProperty("images", tempDir.toString());
    
        // Charger les images
        imageLoaderService.loadImagesOnStartup();

       // Récupérer le nombre d'images chargées
       int loadedImagesCount = imageDao.retrieveAll().size();
    
       // Vérification : le nombre d'images chargées doit être égal au nombre total d'images créées
       System.out.println("Images chargées : " + loadedImagesCount);
    
       assertEquals(numImages+1, loadedImagesCount);//+1 pour l image par défaut
    }

    
    //Vérifiez que les fichiers non-images sont ignorés .
    @Test
    void testIgnoreInvalidFiles() throws IOException {
       // Création de 5 fichiers d'images valides (JPG, JPEG, PNG)
        File imageJpg1 = new File(tempDir.toFile(), "tets.jpg");
        File imageJpg2 = new File(tempDir.toFile(), "shibuya.jpg");
        File imageJpeg = new File(tempDir.toFile(), "bee.jpeg");
        File imagePng2 = new File(tempDir.toFile(), "montagne.png");

        Files.write(imageJpg1.toPath(), new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}); // Simule un JPG
        Files.write(imageJpg2.toPath(), new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}); // Simule un JPG
        Files.write(imageJpeg.toPath(), new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}); // Simule un JPEG
        Files.write(imagePng2.toPath(), new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47}); // Simule un PNG

        // Création d'un fichier non valide (ex : un fichier .txt)
        File invalidFile = new File(tempDir.toFile(), "test.txt");
        Files.write(invalidFile.toPath(), "This is not an image".getBytes());

        // Définition du dossier temporaire comme répertoire "images"
        System.setProperty("images", tempDir.toString());

        // Chargement des fichiers
        imageLoaderService.loadImagesOnStartup();

        // Vérification : Seuls les 5 fichiers images doivent être chargés
        assertEquals(5, imageDao.retrieveAll().size());//5 c'est plus l'image par défaut 
    }

}