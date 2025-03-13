package pdl.backend.FileHandler;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.web.multipart.MultipartFile;

/**
 * Handles file storage and deletion operations in a designated directory.
 * 
 * This class provides static utility methods to store uploaded files (MultipartFile)
 * and remove files from a predefined directory ("src/main/resources/images").
 * It includes basic security checks to prevent storing files outside the intended directory.
 * Inspired by the Spring guide: https://spring.io/guides/gs/uploading-files.
 */
public class FileController {
    // insipré de https://spring.io/guides/gs/uploading-files
    private static final Path directory_location = Paths.get("src/main/resources/images");
   /**
    * Stores an uploaded file in the designated directory.
    * 
    * Saves the provided MultipartFile to the "src/main/resources/images" directory using
    * its original filename. Includes security checks to ensure the file is stored within
    * the intended directory and throws exceptions for empty files or I/O errors.
    * 
    * @param file The MultipartFile to store
    * @throws RuntimeException If the file is empty, the destination is outside the directory,
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
     * Deletes the file with the specified name from the "src/main/resources/images" directory.
     * Throws an exception if the deletion fails due to an I/O error (e.g., file not found).
     * 
     * @param name The name of the file to delete
     * @throws RuntimeException If an I/O error occurs during deletion
     */
    public static void remove_from_directory(String name) {

        Path fileToDelete = Paths.get(directory_location.toString() + "/" + name);
        try {
            Files.delete(fileToDelete);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
