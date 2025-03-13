package pdl.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;

import com.pgvector.PGvector;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import pdl.backend.Database.ImageRepository;
import pdl.backend.imageProcessing.ImagePGVector;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class DatabaseTests {

    @Autowired
    private ImageRepository imageRepository;

    private static BufferedImage testImage;
    private static long testImageId;

    @BeforeAll
    public static void setup() throws IOException {
        ClassPathResource imgFile = new ClassPathResource("images_test/test.png");
        testImage = UtilImageIO.loadImage(imgFile.getFile().getAbsolutePath());
    }

    @Test
    @Order(1)
    public void testRgbHistogramGeneration() {
        assertNotNull(testImage, "Test image should be loaded");

        Planar<GrayU8> image = ConvertBufferedImage.convertFromPlanar(
                testImage, null, true, GrayU8.class);

        PGvector histogram = ImagePGVector.createRgbHistogram(image, 8);

        float[] values = histogram.toArray();
        assertEquals(512, values.length, "8^3 bins expected");

        float sum = 0.0f;
        for (float value : values) {
            sum += value;
        }
        assertEquals(1.0f, sum, 0.001f, "Histogram should be normalized (sum ≈ 1)");
    }

    @Test
    @Order(2)
    public void testAddImageToDatabase() throws IOException {
        ClassPathResource imgFile = new ClassPathResource("images_test/test.png");
        Image img = new Image(
                imgFile.getFile().getParent(),
                "test.png",
                new byte[100], // valeur non utilisée pour les tests
                MediaType.IMAGE_PNG,
                testImage.getWidth(),
                testImage.getHeight(),
                "Test image");
        try {
            imageRepository.addDatabase(img);
            testImageId = img.getId();
        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            throw e;
        }

        List<Image> images = imageRepository.list();
        boolean found = false;
        for (Image image : images) {
            if (image.getName().equals("test.png")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Image should be added to database");
    }
}

/*
 * @Test
 * 
 * @Order(3)
 * public void testDeleteImageFromDatabase() {
 * int initialCount = imageRepository.list().size();
 * 
 * Image img = new Image();
 * img.setId(testImageId);
 * 
 * imageRepository.deleteDatabase(img);
 * 
 * int finalCount = imageRepository.list().size();
 * assertEquals(initialCount - 1, finalCount, "Image should be deleted");
 * }
 * }
 */