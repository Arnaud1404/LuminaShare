package pdl.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Service
public class ImageService {
  private static final List<String> SUPPORTED_FORMATS = Arrays.asList("jpg", "jpeg", "png", "bmp");

    @Autowired
    private ImageDao imageDao;

    public void loadImagesFromDirectory(String directoryPath) {
        File folder = new File(directoryPath);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (isValidImage(file)) {
                    try {
                        byte[] imageData = Files.readAllBytes(file.toPath());
                        imageDao.saveImage(file.getName(), imageData); // ✅ Utilisation de la nouvelle méthode
                    } catch (IOException e) {
                        System.err.println("Erreur de lecture du fichier : " + file.getName());
                    }
                }
            }
        }
    }

    private boolean isValidImage(File file) {
        String name = file.getName().toLowerCase();
        return file.isFile() && SUPPORTED_FORMATS.stream().anyMatch(name::endsWith);
    }
}
