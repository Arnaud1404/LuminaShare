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

@Service
public class ImageService {

    private final ImageDao imageDao;
    private static final String IMAGE_FOLDER = "src/main/resources/images";
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("jpg", "jpeg", "png");

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
            for (File file : files) {
                // Vérifie que le fichier est une image valide
                if (file.isFile() && isValidImage(file.getName())) {
                    try {
                        byte[] fileContent = Files.readAllBytes(file.toPath());
                        imageDao.saveImage(file.getName(), fileContent);
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
            for (File file : files) {
                // Vérifie que le fichier est une image valide
                if (file.isFile() && isValidImage(file.getName())) {
                    try {
                        byte[] fileContent = Files.readAllBytes(file.toPath());
                        imageDao.saveImage(file.getName(), fileContent);
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
    private static boolean isValidImage(String fileName) {
        String extension = getFileExtension(fileName);
        return SUPPORTED_FORMATS.contains(extension.toLowerCase());
    }

    /**
     * Récupère l'extension d'un fichier à partir de son nom.
     */
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : fileName.substring(lastDotIndex + 1);
    }

    public static MediaType parseMediaTypeFromFilename(String filename) {
        if (!isValidImage(filename))
            return null;
        String lowerFilename = filename.toLowerCase();
        String extension = getFileExtension(lowerFilename);
        switch (extension) {
            case "png":
                return MediaType.IMAGE_PNG;
            case "jpeg":
            case "jpg":
                return MediaType.IMAGE_JPEG;
            default:
                return null;
        }
    }

    public static MediaType parseMediaTypeFromFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        MediaType mediaType = parseMediaTypeFromFilename(originalFilename);
        return mediaType;
    }

}
