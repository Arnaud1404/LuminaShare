package pdl.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import pdl.backend.Database.ImageRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class ImageLoaderServiceTest {

    private ImageDao imageDao;
    private ImageRepository imageRepository;
    private ImageService imageLoaderService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        imageLoaderService = new ImageService(imageDao, imageRepository);
    }

}