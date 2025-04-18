package pdl.backend.FileHandler;

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

    /**
     * Stores an uploaded file in the designated directory.
     * 
     * Saves the file with the original filename in the "src/main/resources/images"
     * directory.
     * 
     * @param file The MultipartFile to store
     * @throws RuntimeException If the file is empty, the destination is outside the
     *                          directory,
     *                          or an I/O error occurs during storage
     */
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

            if (Files.exists(destinationFile)) {
                return;
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    /**
     * Removes a file from the designated directory.
     * 
     * Deletes the file with the given name from the "src/main/resources/images"
     * directory.
     * 
     * @param name The name of the file to delete
     * @throws RuntimeException If an I/O error occurs during deletion
     */
    public static void remove_from_directory(String name) {

        Path fileToDelete = Paths.get(directory_location.toString() + "/" + name);
        try {
            Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File get_file(String name) {
        Path fileToGet = Paths.get(directory_location.toString() + "/" + name);
        return fileToGet.toFile();
    }

    /**
     * Checks if a file exists in the images directory
     * 
     * @param name The filename to check
     * @return true if the file exists, else false
     */
    public static boolean file_exists(String name) {
        Path filePath = Paths.get(directory_location.toString() + "/" + name);
        return Files.exists(filePath);
    }

    /**
     * Counts image files in the directory
     * 
     * @return number of image files in the directory
     */
    public static long count_files() {
        try {
            return Files.list(directory_location)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
                    })
                    .count();
        } catch (IOException e) {
            throw new RuntimeException("Failed to count files in directory", e);
        }
    }
}
