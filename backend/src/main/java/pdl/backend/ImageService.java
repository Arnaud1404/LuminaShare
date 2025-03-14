package pdl.backend;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import pdl.backend.Database.ImageRepository;
import pdl.backend.FileHandler.FileController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Service class for managing image-related operations and similarity searches.
 * 
 * This class interacts with the ImageDao to retrieve images and performs similarity
 * computations based on image descriptors. It supports variable-length descriptors
 * and handles different descriptor types (e.g., grayscale, RGB).
 */
@Service
public class ImageService {

    private final ImageDao imageDao;
    private final ImageRepository imageRepository;
    private static final String IMAGE_FOLDER = "src/main/resources/images";
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("jpg", "jpeg", "png");

    public ImageService(ImageDao imageDao, ImageRepository imageRepository) {
        this.imageDao = imageDao;
        this.imageRepository = imageRepository;
    }
    /**
     * Loads images from a predefined folder into the database on application startup.
     * 
     * This method is automatically invoked after bean initialization due to the
     * {@code @PostConstruct} annotation. It scans the 'images/' folder (defined by
     * {@code IMAGE_FOLDER}), creates the folder if it doesn’t exist, and loads valid
     * image files into the database using the {@code ImageDao}. It respects a maximum
     * limit ({@code MAX_IMAGES}) to prevent overloading.
     * 
     * <p>If the folder doesn’t exist, it is created, and the method exits without
     * loading any files. For each valid image file, it reads the content as bytes and
     * saves it to the database, logging the progress. Memory is periodically freed
     * using garbage collection.</p>
     * 
     * @throws RuntimeException If an I/O error occurs while reading an image file
     */
    @PostConstruct
    public void loadImagesOnStartup() {
        File folder = new File(IMAGE_FOLDER);
        // Vérifie si le dossier 'images/' existe, sinon lève une erreur
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Le dossier '" + IMAGE_FOLDER + "' n'existe pas. Création en cours...");
            folder.mkdirs(); // Crée le dossier s'il n'existe pas
            System.out.println("Dossier 'images' créé : " + IMAGE_FOLDER);
            return; // Pas de fichiers à charger immédiatement après création
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                // Vérifie que le fichier est une image valide
                if (file.isFile() && isValidImage(file.getName())) {
                    try {
                        byte[] fileContent = Files.readAllBytes(file.toPath());
                        Image image = imageDao.saveImage(file.getName(), fileContent);

                        if (!imageRepository.imageExists(file.getName())) {
                            imageRepository.addDatabase(image);
                        }
                        System.out.println("Image chargée : " + file.getName());
                    } catch (IOException e) {
                        throw new RuntimeException("Erreur lors du chargement de l'image : " + file.getName());
                    }
                }
            }
        }
    }
     /**
      * Loads images from a specified folder into the database.
      * 
      * This method scans a user-defined folder, creates it if it doesn’t exist, and loads
      * valid image files into the database using the {@code ImageDao}. It respects a maximum
      * limit ({@code MAX_IMAGES}) to prevent overloading. Unlike the {@code @PostConstruct}
      * version, this method allows specifying a custom folder path.
      * 
      * <p>If the folder doesn’t exist, it is created, and the method exits without
      * loading any files. For each valid image file, it reads the content as bytes and
      * saves it to the database, logging the progress. Memory is periodically freed
      * using garbage collection.</p>
      * 
      * @param name_folder The path to the folder containing images to load
      * @throws RuntimeException If an I/O error occurs while reading an image file
      */
    public void loadImagesOnStartup(String name_folder) {
        File folder = new File(name_folder);
        // Vérifie si le dossier 'images/' existe, sinon lève une erreur
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Le dossier '" + IMAGE_FOLDER + "' n'existe pas. Création en cours...");
            folder.mkdirs(); // Crée le dossier s'il n'existe pas
            System.out.println("Dossier 'images' créé : " + IMAGE_FOLDER);
            return; // Pas de fichiers à charger immédiatement après création
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                // Vérifie que le fichier est une image valide
                if (file.isFile() && isValidImage(file.getName())) {
                    try {
                        byte[] fileContent = Files.readAllBytes(file.toPath());
                        Image image = imageDao.saveImage(file.getName(), fileContent);

                        if (!imageRepository.imageExists(file.getName())) {
                            imageRepository.addDatabase(image);
                        }
                        System.out.println("Image chargée : " + file.getName());
                    } catch (IOException e) {
                        throw new RuntimeException("Erreur lors du chargement de l'image : " + file.getName());
                    }
                }
            }
        }
    }

    /**
     * Vérifie si l'extension du fichier correspond aux formats supportés.
     */
    private boolean isValidImage(String fileName) {
        String extension = getFileExtension(fileName);
        return extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")
                || extension.equalsIgnoreCase("png");
    }

    /**
     * Récupère l'extension d'un fichier à partir de son nom.
     */
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : fileName.substring(lastDotIndex + 1);
    }
     /**
      * Parses the media type of a file based on its original filename.
      * 
      * This method extracts the filename from a MultipartFile and delegates the media type
      * parsing to parseMediaTypeFromFilename. It supports common image formats like PNG
      * and JPEG.
      * 
      * @param file The MultipartFile to analyze
      * @return The MediaType corresponding to the file extension (e.g., IMAGE_PNG, IMAGE_JPEG), or null if undetermined
      */
    public static MediaType parseMediaTypeFromFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        return parseMediaTypeFromFilename(originalFilename);
    }
    /**
      * Parses the media type of a file based on its filename extension.
      * 
      * This method examines the file extension (case-insensitive) and returns the corresponding
      * MediaType for supported image formats (PNG, JPG, JPEG). If the extension is unrecognized
      * or the filename is null, it returns null.
      * 
      * @param fileName The name of the file to parse
      * @return The MediaType corresponding to the file extension (e.g., IMAGE_PNG, IMAGE_JPEG), or null if undetermined
      */
    public static MediaType parseMediaTypeFromFilename(String fileName) {
        if (fileName != null) {
            String extension = getFileExtension(fileName).toLowerCase();
            if ("png".equals(extension)) {
                return MediaType.IMAGE_PNG;
            } else if ("jpg".equals(extension) || "jpeg".equals(extension)) {
                return MediaType.IMAGE_JPEG;
            }
        }
        return null;
    }
}
