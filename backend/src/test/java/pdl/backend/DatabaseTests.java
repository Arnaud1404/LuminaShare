package pdl.backend;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
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
import org.springframework.test.util.ReflectionTestUtils;

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
    private static float[] rgbArray;

    @BeforeAll
    public static void setup() throws IOException {
        ReflectionTestUtils.setField(Image.class, "count", Long.valueOf(0));

        ClassPathResource imgFile = new ClassPathResource("images_test/test.png");
        testBufferedImage = UtilImageIO.loadImage(imgFile.getFile().getAbsolutePath());
        byte[] imgData = Files.readAllBytes(imgFile.getFile().toPath());

        testImage = new Image(null, "test.png", imgData,
                MediaType.IMAGE_PNG,
                testBufferedImage.getWidth(),
                testBufferedImage.getHeight(),
                "Test database image");
        testImageId = testImage.getId();
    }

    @Test
    @Order(1)
    public void testHistogramGeneration() {
        Planar<GrayU8> image = ConvertBufferedImage.convertFromPlanar(
                testBufferedImage, null, true, GrayU8.class);

        PGvector histogram = ImagePGVector.createRgbHistogram(image, 8);

        rgbArray = histogram.toArray();
        assertEquals(512, rgbArray.length);

        float sum = 0.0f;
        for (float v : rgbArray)
            sum += v;
        assertEquals(1.0f, sum, 0.01f);
    }

    // @Test
    // @Order(2)
    // public void testAddImage() {
    //     long before = imageRepository.getImageCount();
    //     imageRepository.addDatabase(testImage);
    //     assertNotNull(imageRepository.getById(testImageId));
    //     long after = imageRepository.getImageCount();
    //     assertEquals(before + 1, after);

    // }

    // @Test
    // @Order(3)
    // public void testDeleteImage() {
    //     long before = imageRepository.getImageCount();

    //     imageRepository.deleteDatabase(testImageId);

    //     long after = imageRepository.getImageCount();

    //     assertEquals(before - 1, after);
    // }
}