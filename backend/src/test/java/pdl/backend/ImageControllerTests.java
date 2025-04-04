package pdl.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.http.MediaType;

import pdl.backend.FileHandler.*;
import java.util.Optional;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class ImageControllerTests {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
    private ImageDao imageDao;
	@Autowired
    private ImageService imageService;

	@BeforeAll
	public static void reset (){
		System.out.println("nb d'image dans dao"+ImageDao.getImageCount());
	}

	@Test
	@Order(1)
	public void getImageListShouldReturnSuccess() throws Exception {
		this.mockMvc.perform(get("/images")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

	@Test
	@Order(2)
	public void getImageShouldReturnNotFound() throws Exception {
		this.mockMvc.perform(get("/images/-1")).andExpect(status().isNotFound());
	}

	@Test
	@Order(3)
	public void createImageShouldReturnSuccessJPEG() throws Exception {
		ReflectionTestUtils.setField(Image.class, "count", Long.valueOf(0));

		ClassPathResource imgFile = new ClassPathResource("images_test/test_certain_est_test12312315646216.jpg");

		MockMultipartFile file_multipart = new MockMultipartFile("file", "test_certain_est_test12312315646216.jpg",
				MediaType.IMAGE_JPEG_VALUE,
				imgFile.getInputStream());
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart)).andDo(print())
				.andExpect(status().isCreated());
	}

	@Test
	@Order(4)
	public void getImageShouldReturnSuccessJPEG() throws Exception {
		this.mockMvc.perform(get("/images/0")).andExpect(status().isOk());

	}

	@Test
	@Order(5)
	public void deleteImageShouldReturnSuccessJPEG() throws Exception {
		assertTrue(FileController.file_exists("test_certain_est_test12312315646216.jpg"));
		this.mockMvc.perform(delete("/images/0")).andExpect(status().isOk());
		assertFalse(FileController.file_exists("test_certain_est_test12312315646216.jpg"));
	}

	@Test
	@Order(6)
	public void createImageShouldReturnSuccessPNG() throws Exception {
		ReflectionTestUtils.setField(Image.class, "count", Long.valueOf(0));

		ClassPathResource imgFile = new ClassPathResource("images_test/test_certain_est_test12312315646216.png");

		MockMultipartFile file_multipart = new MockMultipartFile("file", "test_certain_est_test12312315646216.png",
				MediaType.IMAGE_JPEG_VALUE,
				imgFile.getInputStream());
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart)).andDo(print())
				.andExpect(status().isCreated());
	}

	@Test
	@Order(7)
	public void getImageShouldReturnSuccessPNG() throws Exception {
		this.mockMvc.perform(get("/images/0")).andExpect(status().isOk());
	}

	@Test
	@Order(8)
	public void deleteImageShouldReturnSuccessPNG() throws Exception {
		assertTrue(FileController.file_exists("test_certain_est_test12312315646216.png"));
		this.mockMvc.perform(delete("/images/0")).andExpect(status().isOk());
		assertFalse(FileController.file_exists("test_certain_est_test12312315646216.png"));

	}

	@Test
	@Order(9)
	public void testSystemSynchronization() throws Exception {
		// TODO : Check dao, database and physical files are in sync
		return;
	}

	@Test
	@Order(10)
	public void createImageShouldReturnNoContent() throws Exception {
		ClassPathResource imgFile = new ClassPathResource("images_test/empty.txt");
		byte[] fileContent;
		fileContent = Files.readAllBytes(imgFile.getFile().toPath());

		MockMultipartFile file_multipart = new MockMultipartFile("file", fileContent);
		// attendu par POST
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart)).andDo(print())
				.andExpect(status().isNoContent());

	}

	@Test
	@Order(11)
	public void createImageShouldReturnUnsupportedMediaType() throws Exception {
		ClassPathResource imgFile = new ClassPathResource("images_test/test.gif");
		byte[] fileContent;
		fileContent = Files.readAllBytes(imgFile.getFile().toPath());

		MockMultipartFile file_multipart = new MockMultipartFile("file", fileContent);
		// attendu par POST
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart)).andDo(print())
				.andExpect(status().isUnsupportedMediaType());

	}

	@Test
	@Order(11)
	public void createImageShouldReturnUnsupportedMediaType2() throws Exception {
		ClassPathResource imgFile = new ClassPathResource("images_test/test.txt");
		byte[] fileContent;
		fileContent = Files.readAllBytes(imgFile.getFile().toPath());

		MockMultipartFile file_multipart = new MockMultipartFile("file", fileContent);
		// attendu par POST
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart)).andDo(print())
				.andExpect(status().isUnsupportedMediaType());

	}

	@Test
	@Order(12)
	public void deleteImagesShouldReturnMethodNotAllowed() throws Exception {
		this.mockMvc.perform(delete("/images")).andExpect(status().isMethodNotAllowed());
	}

	@Test
	@Order(13)
	public void deleteImageShouldReturnNotFound() throws Exception {
		this.mockMvc.perform(delete("/images/-1")).andExpect(status().isNotFound());
	}

	@Test
	@Order(14)
	public void imageShouldNotBeFound() throws Exception {
		this.mockMvc.perform(get("/images/0")).andExpect(status().isNotFound());
	}

   /**
    * Teste si l'API de redimensionnement d'image retourne un succès.
    * 
    * Étapes :
    * 1. Charge une image de test depuis les ressources.
    * 2. Ajoute l'image via l'API POST `/images`.
    * 3. Appelle l'API POST `/images/{id}/resize` pour redimensionner l'image.
    * 4. Vérifie que la réponse HTTP est un statut 200 (OK) et que le message
    *    "Image redimensionnée avec succès." est retourné.
    * 
    * @throws Exception si une erreur survient lors de l'exécution du test.
    */
  
	@Test
    @Order(15)
    public void resizeImageShouldReturnSuccess() throws Exception {
        // Charger une image de test
        ClassPathResource imgFile = new ClassPathResource("images_test/test.jpg");
        byte[] fileContent = Files.readAllBytes(imgFile.getFile().toPath());

        // Ajouter l'image
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", fileContent);
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file))
                .andExpect(status().isCreated());

        // Redimensionner l'image
        this.mockMvc.perform(MockMvcRequestBuilders.post("/images/1/resize")
		        .param("width", "300")
                .param("height", "300"))
                .andExpect(status().isOk())
                .andExpect(content().string("Image redimensionnée avec succès."));
    }

	/**
     * Teste si l'image redimensionnée a les dimensions correctes (300x300 pixels).
     * 
     * Étapes :
     * 1. Charge une image de test depuis les ressources.
     * 2. Ajoute l'image via l'API POST `/images`.
     * 3. Appelle l'API POST `/images/{id}/resize` pour redimensionner l'image.
     * 4. Récupère l'image redimensionnée depuis le DAO.
     * 5. Vérifie que l'image existe et que ses dimensions sont de 300x300 pixels.
     * 
     * @throws Exception si une erreur survient lors de l'exécution du test.
     */
	@Test
    @Order(16)
    public void resizedImageShouldHaveCorrectDimensions() throws Exception {
        ClassPathResource imgFile = new ClassPathResource("images_test/test.jpg");
        byte[] fileContent = Files.readAllBytes(imgFile.getFile().toPath());

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", fileContent);
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file))
                .andExpect(status().isCreated());

        this.mockMvc.perform(MockMvcRequestBuilders.post("/images/1/resize")
		        .param("width", "300")
                .param("height", "300"))
                .andExpect(status().isOk());

        // Vérifier les dimensions
        Optional<Image> optionalImage = imageDao.retrieve(1L);
        assertTrue(optionalImage.isPresent());
        Image resizedImage = optionalImage.get();
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(resizedImage.getData()));
        assertEquals(300, bufferedImage.getWidth());
        assertEquals(300, bufferedImage.getHeight());
    }
	@Test
    @Order(17)
    public void resizeImageShouldReturnBadRequestForInvalidDimensions() throws Exception {
        // Charger une image de test
        ClassPathResource imgFile = new ClassPathResource("images_test/test.jpg");
        byte[] fileContent = Files.readAllBytes(imgFile.getFile().toPath());

        // Ajouter l'image
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", fileContent);
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file))
                .andExpect(status().isCreated());

        // Tenter de redimensionner l'image avec des dimensions invalides
        this.mockMvc.perform(MockMvcRequestBuilders.post("/images/1/resize")
                .param("width", "-100")
                .param("height", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Les dimensions doivent être positives."));
    }
	/**
	 * Test case for verifying the functionality of inverting image colors.
	 * 
	 * This test performs the following steps:
	 * 1. Loads a test image from the resources directory.
	 * 2. Sends a POST request to upload the image to the server.
	 * 3. Sends another POST request to invert the colors of the uploaded image.
	 * 4. Verifies that the server responds with a success status and the expected message.
	 * 
	 * @throws Exception if an error occurs during the test execution.
	 */
    @Test
    @Order(18)
    public void invertImageColorsShouldReturnSuccess() throws Exception {
        // Charger une image de test
        ClassPathResource imgFile = new ClassPathResource("images_test/test.jpg");
        byte[] fileContent = Files.readAllBytes(imgFile.getFile().toPath());

        // Ajouter l'image
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", fileContent);
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file))
            .andExpect(status().isCreated());

        // Inverser les couleurs
        this.mockMvc.perform(MockMvcRequestBuilders.post("/images/1/invert"))
            .andExpect(status().isOk())
            .andExpect(content().string("Couleurs inversées avec succès."));
    }
	@Test
    @Order(20)
    public void mirrorImageShouldWorkCorrectly() throws Exception {
        // Créer une image PNG avec canal alpha
        BufferedImage testImage = new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB);
        testImage.setRGB(0, 0, 0x80FF0000); // Rouge avec alpha 50%
        testImage.setRGB(1, 0, 0x8000FF00); // Vert avec alpha 50%
        testImage.setRGB(2, 0, 0x800000FF); // Bleu avec alpha 50%
        testImage.setRGB(0, 1, 0x80FFFFFF); // Blanc avec alpha 50%
        testImage.setRGB(1, 1, 0x80000000); // Noir avec alpha 50%
        testImage.setRGB(2, 1, 0x80808080); // Gris avec alpha 50%

        // Miroir horizontal
        BufferedImage horizontalMirror = imageService.mirrorImage(testImage, true);
        assertEquals(0x800000FF, horizontalMirror.getRGB(0, 0)); // Bleu avec alpha 50% à gauche
        assertEquals(0x8000FF00, horizontalMirror.getRGB(1, 0)); // Vert avec alpha 50% au milieu
        assertEquals(0x80FF0000, horizontalMirror.getRGB(2, 0)); // Rouge avec alpha 50% à droite
        assertEquals(0x80808080, horizontalMirror.getRGB(0, 1)); // Gris avec alpha 50% à gauche
        assertEquals(0x80000000, horizontalMirror.getRGB(1, 1)); // Noir avec alpha 50% au milieu
        assertEquals(0x80FFFFFF, horizontalMirror.getRGB(2, 1)); // Blanc avec alpha 50% à droite

        // Miroir vertical
        BufferedImage verticalMirror = imageService.mirrorImage(testImage, false);
        assertEquals(0x80FFFFFF, verticalMirror.getRGB(0, 0)); // Blanc avec alpha 50% en haut
        assertEquals(0x80000000, verticalMirror.getRGB(1, 0)); // Noir avec alpha 50% au milieu
        assertEquals(0x80808080, verticalMirror.getRGB(2, 0)); // Gris avec alpha 50% en haut
        assertEquals(0x80FF0000, verticalMirror.getRGB(0, 1)); // Rouge avec alpha 50% en bas
        assertEquals(0x8000FF00, verticalMirror.getRGB(1, 1)); // Vert avec alpha 50% au milieu
        assertEquals(0x800000FF, verticalMirror.getRGB(2, 1)); // Bleu avec alpha 50% en bas
	}
	@Test
    @Order(21)
    public void mirrorImageShouldWorkForDifferentFormats() throws Exception {
        // Charger une image JPEG
        ClassPathResource jpegFile = new ClassPathResource("images_test/test.jpg");
        byte[] jpegContent = Files.readAllBytes(jpegFile.getFile().toPath());
        MockMultipartFile jpegMultipartFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", jpegContent);

        // Charger une image PNG
        ClassPathResource pngFile = new ClassPathResource("images_test/test.png");
        byte[] pngContent = Files.readAllBytes(pngFile.getFile().toPath());
        MockMultipartFile pngMultipartFile = new MockMultipartFile("file", "test.png", "image/png", pngContent);

        // Ajouter les images
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(jpegMultipartFile))
                .andExpect(status().isCreated());
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(pngMultipartFile))
                .andExpect(status().isCreated());

        // Miroir horizontal pour JPEG
        this.mockMvc.perform(MockMvcRequestBuilders.post("/images/1/mirror").param("horizontal", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string("Miroir créé avec succès."));

        // Miroir vertical pour PNG
        this.mockMvc.perform(MockMvcRequestBuilders.post("/images/2/mirror").param("horizontal", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string("Miroir créé avec succès."));
    }
	@Test
    @Order(22)
    public void rotateImageShouldWorkForARGB() throws Exception {
        // Créer une image ARGB avec canal alpha
        BufferedImage testImage = new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB);
        testImage.setRGB(0, 0, 0x80FF0000); // Rouge avec alpha 50%
        testImage.setRGB(1, 0, 0x8000FF00); // Vert avec alpha 50%
        testImage.setRGB(2, 0, 0x800000FF); // Bleu avec alpha 50%
        testImage.setRGB(0, 1, 0x80FFFFFF); // Blanc avec alpha 50%
        testImage.setRGB(1, 1, 0x80000000); // Noir avec alpha 50%
        testImage.setRGB(2, 1, 0x80808080); // Gris avec alpha 50%

        // Rotation 90°
        BufferedImage rotated90 = imageService.rotateImage(testImage, 90);
        assertEquals(2, rotated90.getWidth());
        assertEquals(3, rotated90.getHeight());
        assertEquals(0x80FFFFFF, rotated90.getRGB(0, 0)); // Blanc avec alpha 50%
        assertEquals(0x80000000, rotated90.getRGB(0, 1)); // Noir avec alpha 50%
        assertEquals(0x80808080, rotated90.getRGB(0, 2)); // Gris avec alpha 50%
        assertEquals(0x80FF0000, rotated90.getRGB(1, 0)); // Rouge avec alpha 50%
        assertEquals(0x8000FF00, rotated90.getRGB(1, 1)); // Vert avec alpha 50%
        assertEquals(0x800000FF, rotated90.getRGB(1, 2)); // Bleu avec alpha 50%

        // Rotation 180°
        BufferedImage rotated180 = imageService.rotateImage(testImage, 180);
        assertEquals(3, rotated180.getWidth());
        assertEquals(2, rotated180.getHeight());
        assertEquals(0x80808080, rotated180.getRGB(0, 0)); // Gris avec alpha 50%
        assertEquals(0x80000000, rotated180.getRGB(1, 0)); // Noir avec alpha 50%
        assertEquals(0x80FFFFFF, rotated180.getRGB(2, 0)); // Blanc avec alpha 50%
        assertEquals(0x800000FF, rotated180.getRGB(0, 1)); // Bleu avec alpha 50%
        assertEquals(0x8000FF00, rotated180.getRGB(1, 1)); // Vert avec alpha 50%
        assertEquals(0x80FF0000, rotated180.getRGB(2, 1)); // Rouge avec alpha 50%

    }
}
