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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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
     * This method scans the 'images/' folder for image files and loads them into the
     * database using the {@code ImageDao}.
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
      * This method scans the specified folder for image files and loads them into the
      * database using the {@code ImageDao}.
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
     * Resizes a given BufferedImage to the specified target width and height.
     *
     * @param originalImage The original BufferedImage to be resized.
     * @param targetWidth The desired width of the resized image.
     * @param targetHeight The desired height of the resized image.
     * @return A new BufferedImage object with the specified dimensions.
     */
    public BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        return resizedImage;
    }
    /**
     * Inverts the colors of a given BufferedImage.
     *
     * @param originalImage The original BufferedImage to be inverted.
     * @return A new BufferedImage object with inverted colors.
     */
    public BufferedImage invertColors(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        BufferedImage invertedImage = new BufferedImage(width, height, originalImage.getType());
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgba = originalImage.getRGB(x, y);
                // Extraire les composantes alpha, rouge, vert et bleu
                int alpha = (rgba >> 24) & 0xff;//canal alpha représente la transparence des pixel pour les formats ARGB (png)
                int red = (rgba >> 16) & 0xff;
                int green = (rgba >> 8) & 0xff;
                int blue = rgba & 0xff;

                // Inverser les couleurs
                int invertedRed = 255 - red;
                int invertedGreen = 255 - green;
                int invertedBlue = 255 - blue;

                // Recomposer la couleur inversée
                int invertedRGBA;
                if (originalImage.getType() == BufferedImage.TYPE_INT_ARGB) {
                    // Conserver le canal alpha pour les images avec transparence
                    invertedRGBA = (alpha << 24) | (invertedRed << 16) | (invertedGreen << 8) | invertedBlue;
                } else {
                    // Ignorer le canal alpha pour les images sans transparence
                    invertedRGBA = (invertedRed << 16) | (invertedGreen << 8) | invertedBlue;
                }
                // Appliquer la couleur inversée au pixel
                invertedImage.setRGB(x, y, invertedRGBA);
            }
            
        }

        return invertedImage;
    }

   /**
    * Creates a mirrored version of the given BufferedImage.
    *
    * @param originalImage The original BufferedImage to be mirrored.
    * @param horizontal True for horizontal mirroring (left-right), false for vertical mirroring (top-bottom).
    * @return A new BufferedImage object with the mirrored image.
    * @throws IllegalArgumentException If the image cannot be processed.
    */
    public BufferedImage mirrorImage(BufferedImage originalImage, boolean horizontal) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // Crée une nouvelle image avec le même type que l'image originale
        BufferedImage mirroredImage = new BufferedImage(width, height, originalImage.getType());

        try {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int pixel = originalImage.getRGB(x, y);

                    if (horizontal) {
                        // Miroir horizontal : inverser gauche-droite
                        mirroredImage.setRGB(width - 1 - x, y, pixel);
                    } else {
                        // Miroir vertical : inverser haut-bas
                        mirroredImage.setRGB(x, height - 1 - y, pixel);
                    }
                }
           }
        } catch (Exception e) {
            throw new IllegalArgumentException("L'image ne peut pas être traitée : " + e.getMessage());
       }

        return mirroredImage;
    }
    /**
     * Rotates the given image by the specified angle.
     *
     * @param originalImage The original BufferedImage to be rotated.
     * @param angle The angle of rotation (must be 90, 180, or 270 degrees).
     * @return A new BufferedImage object with the rotated image.
     * @throws IllegalArgumentException If the angle is invalid or the image cannot be processed.
     */
    public BufferedImage rotateImage(BufferedImage originalImage, int angle) {
        if (angle != 90 && angle != 180 && angle != 270) {
            throw new IllegalArgumentException("Angle invalide. Seuls 90°, 180° et 270° sont pris en charge.");
        }

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage rotatedImage;

        if (angle == 90 || angle == 270) {
            // Inverser largeur et hauteur pour 90° ou 270°
            rotatedImage = new BufferedImage(height, width, originalImage.getType());
        } else {
            // Conserver largeur et hauteur pour 180°
            rotatedImage = new BufferedImage(width, height, originalImage.getType());
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = originalImage.getRGB(x, y);

                if (angle == 90) {
                    rotatedImage.setRGB(height - 1 - y, x, pixel);
                } else if (angle == 180) {
                    rotatedImage.setRGB(width - 1 - x, height - 1 - y, pixel);
                } else if (angle == 270) {
                    rotatedImage.setRGB(y, width - 1 - x, pixel);
                }
            }
        }

        return rotatedImage;
    }
    /**
     * Checks if a given filename corresponds to a valid image file.
     * 
     * @param fileName The name of the file to check
     * @return True if the file is a valid image, else false
     */
    private boolean isValidImage(String fileName) {
        String extension = getFileExtension(fileName);
        return extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")
                || extension.equalsIgnoreCase("png");
    }

    /**
     * Gets the file extension of a given filename.
     * 
     * @param fileName The name of the file to parse
     */
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : fileName.substring(lastDotIndex + 1);
    }
     /**
      * Parses the media type of a file based on its original filename.
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
