package pdl.backend;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


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
            throw new RuntimeException("Erreur : Le dossier 'images' est introuvable. Assurez-vous qu'il existe dans le répertoire de lancement du serveur.");
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
                        System.err.println("Erreur lors du chargement de l'image : " + file.getName());
                    }
                }
            }
        }
    }
    //Calculer et stocker les descripteurs lorsqu'une image est ajoutée et les récupérer ensuite 
    public void addImage(String fileName, byte[] fileContent) {
        Image img = new Image(fileName, fileContent);
        imageDao.create(img);
    }

    public Optional<int[][]> getHistogramHS(long id) {
        return Optional.ofNullable(imageDao.getHistogramHS(id));
    }

    public Optional<int[][][]> getHistogramRGB(long id) {
        return Optional.ofNullable(imageDao.getHistogramRGB(id));
    }

    /**
     * Vérifie si l'extension du fichier correspond aux formats supportés.
     */
    private boolean isValidImage(String fileName) {
        String extension = getFileExtension(fileName);
        return SUPPORTED_FORMATS.contains(extension.toLowerCase());
    }
    /**
     * Récupère l'extension d'un fichier à partir de son nom.
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : fileName.substring(lastDotIndex + 1);
    }
}
