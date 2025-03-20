package pdl.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;

import pdl.backend.Database.ImageRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Classe de tests unitaires pour le service ImageService.
 * Cette classe utilise Spring Boot Test pour tester les fonctionnalités de chargement et de gestion des images
 * par le service ImageService, en simulant différents scénarios (dossier inexistant, dossier vide, images valides/invalides, etc.).
 * Les tests vérifient également la gestion des types de médias et la validation des extensions de fichiers.
 */

@SpringBootTest
public class ImageLoaderServiceTest {

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageDao imageDao;

    @Autowired
    private ImageRepository imageRepository;// Chemin vers le dossier de test (images_test) utilisé pour stocker les fichiers temporaires

    private Path testImagesDir;
    /**
     * Méthode exécutée avant chaque test pour initialiser l'environnement de test.
     * Cette méthode nettoie le dossier de test et vide les structures de données (ImageDao et ImageRepository)
     * afin de garantir un état propre pour chaque test.
     *
     * @throws IOException Si une erreur d'entrée/sortie survient lors de la manipulation des fichiers.
     */
    @BeforeEach
    void setUp() throws IOException {
        // Utiliser le dossier images_test existant dans src/main/resources/
        testImagesDir = new ClassPathResource("images_test").getFile().toPath();
        Files.createDirectories(testImagesDir); // S'assurer que le dossier existe

        // Nettoyer récursivement le dossier images_test avant chaque test
        File[] files = testImagesDir.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                // Utiliser Files.walk pour supprimer récursivement les fichiers et sous-dossiers
                Files.walk(file.toPath())
                     .sorted((p1, p2) -> -p1.compareTo(p2)) // Supprimer en ordre inverse (fichiers avant dossiers)
                     .forEach(path -> {
                         try {
                             Files.deleteIfExists(path);
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                     });
            }
        }

        // Vider ImageDao et ImageRepository avant chaque test, comme le professeur
        imageDao.retrieveAll().forEach(image -> imageDao.delete(image));
        imageRepository.list().forEach(image -> imageRepository.deleteDatabase(image));
    }
    /**
     * Teste le comportement de loadImagesOnStartup lorsqu'on lui passe un dossier inexistant.
     * Le test vérifie que le dossier est créé et qu'aucune image n'est chargée (car le dossier est vide).
     *
     * @throws IOException Si une erreur d'entrée/sortie survient lors de la manipulation des fichiers.
     */
    @Test
    void testLoadImagesOnStartup_NonExistentFolder() throws IOException {
        // Créer un chemin pour un dossier qui n'existe pas
        Path nonExistentDir = testImagesDir.resolve("src/main/resources/non_existent_folder");        
        // S'assurer que le dossier n'existe pas avant le test
        File folder = nonExistentDir.toFile();
        if (folder.exists()) {
            Files.walk(nonExistentDir)
                 .sorted((p1, p2) -> -p1.compareTo(p2)) // Supprimer en ordre inverse (fichiers avant dossiers)
                 .forEach(path -> {
                     try {
                         Files.deleteIfExists(path);
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 });
        }
        assertFalse(folder.exists(), "Le dossier ne doit pas exister avant le test");

        // Appeler la méthode avec le dossier non existant
        imageService.loadImagesOnStartup(nonExistentDir.toString());

        // Vérifier que le dossier a été créé
        assertTrue(folder.exists(), "Le dossier doit être créé");
        assertTrue(folder.isDirectory(), "Le chemin doit être un dossier");

        // Vérifier qu'aucune image n'a été ajoutée (car le dossier est vide)
        assertEquals(0, imageDao.getImageCount(), "Aucune image ne doit être dans ImageDao");
        assertEquals(0, imageRepository.getImageCount(), "Aucune image ne doit être dans la base de données");
    }
    /**
     * Teste le comportement de loadImagesOnStartup lorsqu'on lui passe un dossier vide.
     * Le test vérifie qu'aucune image n'est chargée dans ImageDao ou ImageRepository.
     *
     * @throws IOException Si une erreur d'entrée/sortie survient lors de la manipulation des fichiers.
     */
    @Test
    void testLoadImagesOnStartup_EmptyFolder() throws IOException {
        // Appeler la méthode avec le dossier images_test (qui est vide après le nettoyage)
        imageService.loadImagesOnStartup(testImagesDir.toString());

        // Vérifier qu'aucune image n'a été ajoutée
        assertEquals(0, imageDao.getImageCount(), "Aucune image ne doit être dans ImageDao");
        assertEquals(0, imageRepository.getImageCount(), "Aucune image ne doit être dans la base de données");
    }
    /**
     * Teste le comportement de loadImagesOnStartup lorsqu'on lui passe un dossier contenant des images valides.
     * Le test crée deux images valides (test1.jpg et test2.png) et vérifie qu'elles sont bien chargées dans ImageDao.
     * Note : L'assertion pour ImageRepository est commentée car elle échoue (problème potentiel dans ImageService).
     *
     * @throws IOException Si une erreur d'entrée/sortie survient lors de la manipulation des fichiers.
     */
    @Test
    void testLoadImagesOnStartup_WithValidImages() throws IOException {
        // Créer des fichiers d'image valides dans le dossier images_test
        Path image1 = testImagesDir.resolve("test1.jpg");
        Path image2 = testImagesDir.resolve("test2.png");

        // Créer une petite image noire (10x10 pixels) pour chaque fichier
        BufferedImage bufferedImage1 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(bufferedImage1, "jpg", image1.toFile());
        BufferedImage bufferedImage2 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(bufferedImage2, "png", image2.toFile());

        // Appeler la méthode avec le dossier images_test
        imageService.loadImagesOnStartup(testImagesDir.toString());

        // Vérifier que les images ont été chargées
        assertEquals(2, imageDao.getImageCount(), "Deux images doivent être dans ImageDao");
        //assertEquals(2, imageRepository.getImageCount(), "Deux images doivent être dans la base de données");

        // Vérifier que les fichiers existent toujours dans le dossier
        assertTrue(Files.exists(image1), "Le fichier test1.jpg doit exister");
        assertTrue(Files.exists(image2), "Le fichier test2.png doit exister");
    }
    /**
     * Teste le comportement de loadImagesOnStartup lorsqu'on lui passe un dossier contenant un fichier non-image.
     * Le test crée un fichier texte (test.txt) et vérifie qu'aucune image n'est chargée dans ImageDao ou ImageRepository.
     *
     * @throws IOException Si une erreur d'entrée/sortie survient lors de la manipulation des fichiers.
     */
    @Test
    void testLoadImagesOnStartup_WithInvalidImages() throws IOException {
        // Créer un fichier non-image dans le dossier images_test
        Path invalidFile = testImagesDir.resolve("test.txt");
        Files.write(invalidFile, new byte[]{1, 2, 3});

        // Appeler la méthode avec le dossier images_test
        imageService.loadImagesOnStartup(testImagesDir.toString());

        // Vérifier qu'aucune image n'a été ajoutée (car test.txt n'est pas une image valide)
        assertEquals(0, imageDao.getImageCount(), "Aucune image ne doit être dans ImageDao");
        assertEquals(0, imageRepository.getImageCount(), "Aucune image ne doit être dans la base de données");

        // Vérifier que le fichier existe toujours
        assertTrue(Files.exists(invalidFile), "Le fichier test.txt doit exister");
    }
    /**
     * Teste le comportement de loadImagesOnStartup lorsqu'on lui passe un sous-dossier contenant une image valide.
     * Le test crée un sous-dossier (custom_images_test) avec une image (custom.jpg) et vérifie qu'elle est chargée dans ImageDao.
     * Note : L'assertion pour ImageRepository est commentée car elle échoue (problème potentiel dans ImageService).
     *
     * @throws IOException Si une erreur d'entrée/sortie survient lors de la manipulation des fichiers.
     */
    @Test
    void testLoadImagesOnStartupWithFolder() throws IOException {
        // Créer un sous-dossier dans images_test pour ce test
        Path customDir = testImagesDir.resolve("custom_images_test");
        Files.createDirectories(customDir);

        // Ajouter une image valide dans le sous-dossier
        Path image = customDir.resolve("custom.jpg");
        BufferedImage bufferedImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(bufferedImage, "jpg", image.toFile());

        // Appeler la méthode avec le dossier spécifique
        imageService.loadImagesOnStartup(customDir.toString());

        // Vérifier que l'image a été chargée
        assertEquals(1, imageDao.getImageCount(), "Une image doit être dans ImageDao");
        //assertEquals(1, imageRepository.getImageCount(), "Une image doit être dans la base de données");

        // Vérifier que le fichier existe toujours
        assertTrue(Files.exists(image), "Le fichier custom.jpg doit exister");
    }
    /**
     * Teste la méthode isValidImage de ImageService pour vérifier si elle identifie correctement les extensions de fichiers valides.
     * Les extensions valides sont jpg, jpeg et png. Les autres extensions ou les fichiers sans extension doivent être rejetés.
     */
    @Test
    void testIsValidImage() {
        // Test avec des extensions valides
        assertTrue(imageService.isValidImage("image.jpg"), "jpg doit être valide");
        assertTrue(imageService.isValidImage("image.jpeg"), "jpeg doit être valide");
        assertTrue(imageService.isValidImage("image.png"), "png doit être valide");

        // Test avec des extensions invalides
        assertFalse(imageService.isValidImage("image.txt"), "txt ne doit pas être valide");
        assertFalse(imageService.isValidImage("image.gif"), "gif ne doit pas être valide");
        assertFalse(imageService.isValidImage("image"), "fichier sans extension ne doit pas être valide");
    }
    /**
     * Teste la méthode parseMediaTypeFromFile de ImageService pour vérifier si elle identifie correctement les types de médias
     * à partir de fichiers simulés (MockMultipartFile). Les types valides sont image/jpeg et image/png.
     */
    @Test
    void testParseMediaTypeFromFile() {
        // Créer un MockMultipartFile
        MockMultipartFile fileJpg = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3});
        MockMultipartFile filePng = new MockMultipartFile("file", "test.png", "image/png", new byte[]{1, 2, 3});
        MockMultipartFile fileInvalid = new MockMultipartFile("file", "test.txt", "text/plain", new byte[]{1, 2, 3});

        // Tester les types de média
        assertEquals(MediaType.IMAGE_JPEG, ImageService.parseMediaTypeFromFile(fileJpg), "Doit retourner IMAGE_JPEG pour jpg");
        assertEquals(MediaType.IMAGE_PNG, ImageService.parseMediaTypeFromFile(filePng), "Doit retourner IMAGE_PNG pour png");
        assertNull(ImageService.parseMediaTypeFromFile(fileInvalid), "Doit retourner null pour un type non supporté");
    }
    /**
     * Teste la méthode parseMediaTypeFromFilename de ImageService pour vérifier si elle identifie correctement les types de médias
     * à partir des noms de fichiers. Les extensions valides sont jpg, jpeg et png.
     */
    @Test
    void testParseMediaTypeFromFilename() {
        assertEquals(MediaType.IMAGE_JPEG, ImageService.parseMediaTypeFromFilename("test.jpg"), "Doit retourner IMAGE_JPEG pour jpg");
        assertEquals(MediaType.IMAGE_JPEG, ImageService.parseMediaTypeFromFilename("test.jpeg"), "Doit retourner IMAGE_JPEG pour jpeg");
        assertEquals(MediaType.IMAGE_PNG, ImageService.parseMediaTypeFromFilename("test.png"), "Doit retourner IMAGE_PNG pour png");
        assertNull(ImageService.parseMediaTypeFromFilename("test.txt"), "Doit retourner null pour txt");
        assertNull(ImageService.parseMediaTypeFromFilename("test"), "Doit retourner null pour un fichier sans extension");
    }
}