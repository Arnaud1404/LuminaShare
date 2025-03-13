package pdl.backend.FileHandler;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.web.multipart.MultipartFile;

/**
 * Handles file system operations for images.
 * 
 * IMPORTANT: Only manages physical files. Doesn't handle database or in-memory
 * records.
 * Synchronization with database and memory should be done by ImageController.
 */
public class FileController {
    // inspiré de https://spring.io/guides/gs/uploading-files
    public static final Path directory_location = Paths.get("src/main/resources/images");

    public static void store(MultipartFile file) {

        try {
            if (file.isEmpty()) {
                throw new RuntimeException("file empty");
            }
            Path destinationFile = directory_location.resolve(
                    Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(directory_location.toAbsolutePath())) { // pour des raison de
                                                                                            // sécurité
                throw new RuntimeException("cannot store file outside current directory");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    public static void remove_from_directory(String name) {

        Path fileToDelete = Paths.get(directory_location.toString() + "/" + name);
        try {
            Files.delete(fileToDelete);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File get_file(String name) {
        Path fileToGet = Paths.get(directory_location.toString() + "/" + name);
        return fileToGet.toFile();
    }
}
