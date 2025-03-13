package pdl.backend;

import org.springframework.http.MediaType;
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

import java.util.Optional; // Ajouté
import java.util.stream.Collectors; // Ajouté pour stream
import com.pgvector.PGvector; // Ajouté pour manipuler les vecteurs

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
     * Méthode exécutée après l'initialisation de l'application.
     * Charge les images du dossier 'images/' et les stocke dans ImageDao.
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
    /*
     * Récupère l’image cible par ID.
     * Compare son descripteur avec ceux de toutes les autres images en utilisant la distance euclidienne.
     * Trie les images par similarité et retourne les N premières.
    */
    public List<Image> findSimilarImages(long imageId, int n, String descriptorType) {
        Optional<Image> targetImageOpt = imageDao.retrieve(imageId);
        if (!targetImageOpt.isPresent()) {
            throw new IllegalArgumentException("Image avec ID " + imageId + " non trouvée");
        }

        Image targetImage = targetImageOpt.get();
        PGvector targetDescriptor = getDescriptorByType(targetImage, descriptorType);
        if (targetDescriptor == null) {
            throw new IllegalDescriptorException("Descripteur invalide : " + descriptorType);
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

    private PGvector getDescriptorByType(Image image, String descriptorType) {
        switch (descriptorType.toLowerCase()) {
            case "gray":
                return image.getDescriptor(); // Descripteur actuel (niveaux de gris)
            case "rgb":
                return computeRGBDescriptor(image); // À implémenter si besoin
            default:
                return null; // Déclenchera une exception
        }
    }

    private PGvector computeRGBDescriptor(Image image) {
        // Exemple : Calculer un descripteur RGB (à adapter selon tes besoins)
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image.getData()));
            GrayU8 grayImage = ConvertBufferedImage.convertFrom(bufferedImage, (GrayU8) null); // Placeholder
            return ImageVectorConversion.convertGrayU8ToVector(grayImage); // À remplacer par RGB si nécessaire
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du calcul du descripteur RGB");
        }
    }

    private static class ImageSimilarity {
        Image image;
        double distance;

        ImageSimilarity(Image image, double distance) {
            this.image = image;
            this.distance = distance;
        }
    }

    private double calculateDistance(PGvector v1, PGvector v2) {
        if (v1 == null || v2 == null) {
            return Double.MAX_VALUE;
        }
        float[] vec1 = v1.toArray();
        float[] vec2 = v2.toArray();
        if (vec1.length != vec2.length) {
            System.out.println("Descriptor length mismatch: " + vec1.length + " vs " + vec2.length);
            return Double.MAX_VALUE;
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

    public static MediaType parseMediaTypeFromFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        return parseMediaTypeFromFilename(originalFilename);
    }

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
