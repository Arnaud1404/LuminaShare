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

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.util.*;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfFloat;
import org.opencv.core.Core;
import org.springframework.beans.factory.annotation.Autowired;
import java.lang.reflect.Field; 
import org.springframework.stereotype.Service; 

@Service
public class ImageService {
    static {
        System.setProperty("java.library.path", "/usr/lib/jni");

        try {
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Charger OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    

    @Autowired
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
                        Mat imageMat = readImage(fileContent); // Lire l'image et la convertir en Mat avec OpenCV
                        String histogram2D = compute2DHistogram(imageMat);
                        String histogram3D = compute3DHistogram(imageMat);
                        imageDao.saveImage(file.getName(), fileContent, histogram2D, histogram3D);
                        System.out.println("Image chargée : " + file.getName());
                    } catch (IOException e) {
                        System.err.println("Erreur lors du chargement de l'image : " + file.getName());
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
        return SUPPORTED_FORMATS.contains(extension.toLowerCase());
    }
    /**
     * Récupère l'extension d'un fichier à partir de son nom.
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : fileName.substring(lastDotIndex + 1);
    }

    //Calcul les descriptor d'image
    public String compute2DHistogram(Mat image) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV);
        
        Mat hist = new Mat();
        Imgproc.calcHist(Collections.singletonList(hsv), new MatOfInt(0, 1), new Mat(), hist, new MatOfInt(50, 60), new MatOfFloat(0, 180, 0, 256));
        
        return hist.dump();
    }
    
    public String compute3DHistogram(Mat image) {
        List<Mat> bgrPlanes = new ArrayList<>();
        Core.split(image, bgrPlanes);
        Mat hist = new Mat();
        Imgproc.calcHist(bgrPlanes, new MatOfInt(0, 1, 2), new Mat(), hist, new MatOfInt(8, 8, 8), new MatOfFloat(0, 256, 0, 256, 0, 256));
        
        return hist.dump();
    }

    public Mat readImage(byte[] imageBytes) throws IOException {
        File tempFile = File.createTempFile("tempImage", ".jpg"); // Création d’un fichier temporaire en .jpg
        Files.write(tempFile.toPath(), imageBytes);//Écrire les données byte[]de l'image dans ce fichier temporaire 
        return Imgcodecs.imread(tempFile.getAbsolutePath());//Utiliser OpenCV pour lire ce fichier temporaire
    }

}
