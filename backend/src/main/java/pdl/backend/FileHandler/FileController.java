package pdl.backend.FileHandler;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.web.multipart.MultipartFile;

public class FileController {
    // insipré de https://spring.io/guides/gs/uploading-files
    private static final Path directory_location = Paths.get("src/main/resources/images");

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
                throw new FileAlreadyExistsException(destinationFile.toString(), null,
                        "Un fichier avec le même nom existe déjà. Veuillez renommer votre fichier.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile);
            }

        } catch (IOException e) {
            if (e instanceof FileAlreadyExistsException) {
                throw new RuntimeException("Un fichier avec ce nom existe déjà.", e);
            } else {
                throw new RuntimeException("Échec du stockage du fichier: " + e.getMessage(), e);
            }
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
}
