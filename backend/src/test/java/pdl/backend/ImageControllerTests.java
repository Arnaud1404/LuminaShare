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
import javax.imageio.ImageIO;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class ImageControllerTests {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
    private ImageDao imageDao;

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
  /** 
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
	*/
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
	/**
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
   */
}
