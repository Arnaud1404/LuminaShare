package pdl.backend.FileHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.web.multipart.MultipartFile;

public class FileController {

    private static final Path directory_location = Paths.get("src/main/resources/images");

    public static void store(MultipartFile file) {

        try {

            if (file.isEmpty()) {
                System.out.println("file empty");
            }
            Path destinationFile = directory_location.resolve(
                    Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(directory_location.toAbsolutePath())) { // pour des raison de
                                                                                            // sécurité
                System.out.println("cannot store file outside current directory");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile);
            }

        } catch (IOException e) {
            System.out.println(e);
        }

    }
}
