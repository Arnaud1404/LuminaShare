package pdl.backend;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
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
@TestMethodOrder(OrderAnnotation.class)
public class DatabaseTests {

    @Autowired
    private ImageRepository imageRepository;

    private static BufferedImage testBufferedImage;
    private static Image testImage;
    private static long testImageId;

    @BeforeAll
    public static void setup() throws IOException {
        // Load the test image once
        ClassPathResource imgFile = new ClassPathResource("images_test/test.png");
        testBufferedImage = UtilImageIO.loadImage(imgFile.getFile().getAbsolutePath());

        // Create an Image object for testing
        testImage = new Image();
        testImage.setName("test.png");
        testImage.setType(MediaType.IMAGE_PNG);
        testImage.setSize("test"); // Just a placeholder
    }

    @Test
    @Order(1)
    public void testHistogramGeneration() {
        // Test the RGB histogram creation
        Planar<GrayU8> image = ConvertBufferedImage.convertFromPlanar(
                testBufferedImage, null, true, GrayU8.class);

        PGvector histogram = ImagePGVector.createRgbHistogram(image, 8);

        // Check dimensions and normalization
        float[] values = histogram.toArray();
        assertEquals(512, values.length);

        float sum = 0.0f;
        for (float v : values)
            sum += v;
        assertEquals(1.0f, sum, 0.01f);

        System.out.println("✓ Histogram generation works correctly");
    }

    @Test
    @Order(2)
    public void testAddImage() {
        // Add image to database
        try {
            imageRepository.addDatabase(testImage);
            testImageId = testImage.getId();
            System.out.println("✓ Added image with ID: " + testImageId);
        } catch (Exception e) {
            fail("Failed to add image: " + e.getMessage());
        }

        // Verify image appears in the list
        List<Image> images = imageRepository.list();
        boolean found = false;
        for (Image img : images) {
            if (img.getName().equals("test.png")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Image should be in database");
    }

    @Test
    @Order(3)
    public void testDeleteImage() {
        // Count images before deletion
        int beforeCount = imageRepository.list().size();

        imageRepository.deleteDatabase(testImageId);

        // Verify deletion
        int afterCount = imageRepository.list().size();
        assertEquals(beforeCount - 1, afterCount);

        System.out.println("✓ Successfully deleted image");
    }
}