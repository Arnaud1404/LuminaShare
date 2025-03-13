package pdl.backend;
import pdl.backend.imageProcessing.ImageVectorConversion;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Optional; 
import java.util.stream.Collectors; 
import com.pgvector.PGvector; 
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import boofcv.struct.image.GrayU8;
import boofcv.io.image.ConvertBufferedImage;
import javax.imageio.ImageIO;


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
    private static final String IMAGE_FOLDER = "src/main/resources/images";
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("jpg", "jpeg", "png");
    private static final int MAX_IMAGES = 20; // Limiter à 10 images pour éviter la surcharge

    public ImageService(ImageDao imageDao) {
        this.imageDao = imageDao;
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
            int count = 0;
            for (File file : files) {
                if (count >= MAX_IMAGES) {
                    System.out.println("Limite de " + MAX_IMAGES + " images atteinte.");
                    break;
                }
                // Vérifie que le fichier est une image valide
                if (file.isFile() && isValidImage(file.getName())) {
                    try {
                        byte[] fileContent = Files.readAllBytes(file.toPath());
                        imageDao.saveImage(file.getName(), fileContent);
                        System.out.println("Image chargée : " + file.getName());
                        count++;
                        System.gc(); // Aide à libérer la mémoire
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
            int count = 0;
            for (File file : files) {
                if (count >= MAX_IMAGES) {
                    System.out.println("Limite de " + MAX_IMAGES + " images atteinte.");
                    break;
                }
                // Vérifie que le fichier est une image valide
                if (file.isFile() && isValidImage(file.getName())) {
                    try {
                        byte[] fileContent = Files.readAllBytes(file.toPath());
                        imageDao.saveImage(file.getName(), fileContent);
                        System.out.println("Image chargée : " + file.getName());
                        count++;
                        System.gc(); // Aide à libérer la mémoire
                    } catch (IOException e) {
                        throw new RuntimeException("Erreur lors du chargement de l'image : " + file.getName());
                    }
                }
            }
        }
    }
    /**
     * Finds the top N images similar to a given image based on a specified descriptor.
     * 
     * The method retrieves the target image by ID, computes its descriptor based on the
     * provided type, and compares it against all other images in the database. It returns
     * a list of the N most similar images, sorted by Euclidean distance.
     * 
     * @param imageId The ID of the target image to compare against
     * @param n The number of similar images to return
     * @param descriptorType The type of descriptor to use (e.g., "gray", "rgb")
     * @return A list of up to N similar images
     * @throws IllegalArgumentException If the image ID is not found or descriptor type is invalid
     */
    public List<Image> findSimilarImages(long imageId, int n, String descriptorType) {
        Optional<Image> targetImageOpt = imageDao.retrieve(imageId);
        if (!targetImageOpt.isPresent()) {
            throw new IllegalArgumentException("Image avec ID " + imageId + " non trouvée");
        }

        Image targetImage = targetImageOpt.get();
        PGvector targetDescriptor = getDescriptorByType(targetImage, descriptorType);
        if (targetDescriptor == null) {
            throw new IllegalArgumentException("Descripteur invalide : " + descriptorType);
        }

        List<Image> allImages = imageDao.retrieveAll();
        return allImages.stream()
                .filter(img -> img.getId() != imageId)
                .map(img -> new ImageSimilarity(img, calculateDistance(targetDescriptor, getDescriptorByType(img, descriptorType))))
                .sorted((a, b) -> Double.compare(a.distance, b.distance))
                .limit(n) // Limiter aux N plus similaires
                .map(is -> is.image)
                .collect(Collectors.toList());
    }
    /**
     * Retrieves or computes the descriptor for an image based on the specified type.
     * 
     * @param image The image to process
     * @param descriptorType The type of descriptor ("gray" or "rgb")
     * @return The PGvector descriptor, or null if the type is unsupported
     */
    private PGvector getDescriptorByType(Image image, String descriptorType) {
        switch (descriptorType.toLowerCase()) {
            case "gray":
                return image.getDescriptor(); // Descripteur actuel (niveaux de gris)
            case "rgb":
                return computeRGBDescriptor(image); // calcul RGB
            default:
                return null; // Déclenchera une exception
        }
    }
    /**
     * Computes an RGB-based descriptor for an image.
     * 
     * This method converts the image to grayscale and generates a vector representation.
     * Note: Currently uses grayscale conversion; true RGB descriptor logic could be added.
     * 
     * @param image The image to process
     * @return The PGvector descriptor
     * @throws RuntimeException If image processing fails
     */
    private PGvector computeRGBDescriptor(Image image) {
        //  exemple de calcul  un descripteur RGB (à adapter selon tes besoins)
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image.getData()));
            GrayU8 grayImage = ConvertBufferedImage.convertFrom(bufferedImage, (GrayU8) null); // Placeholder
            return ImageVectorConversion.convertGrayU8ToVector(grayImage); // À remplacer par RGB si nécessaire
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du calcul du descripteur RGB");
        }
    }
    /**
     * Helper class to pair an image with its similarity distance.
     */
    private static class ImageSimilarity {
        Image image;
        double distance;

        ImageSimilarity(Image image, double distance) {
            this.image = image;
            this.distance = distance;
        }
    }
    /**
     * Calculates the Euclidean distance between two descriptors.
     * 
     * Handles variable-length descriptors by computing the distance over the shorter
     * vector's length, logging mismatches for debugging.
     * 
     * @param v1 The first descriptor
     * @param v2 The second descriptor
     * @return The Euclidean distance, or Double.MAX_VALUE if either descriptor is null
     */
    private double calculateDistance(PGvector v1, PGvector v2) {
        if (v1 == null || v2 == null) {
            return Double.MAX_VALUE;
        }
        float[] vec1 = v1.toArray();
        float[] vec2 = v2.toArray();
        if (vec1.length != vec2.length) {
            System.out.println("Descriptor length mismatch: " + vec1.length + " vs " + vec2.length);
            return Double.MAX_VALUE; // Treat as dissimilar
        }
        double sum = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            double diff = vec1[i] - vec2[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
 
    /**
     * Vérifie si l'extension du fichier correspond aux formats supportés.
     */
    private boolean isValidImage(String fileName) {
        String extension = getFileExtension(fileName);
        return extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase("png");
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
