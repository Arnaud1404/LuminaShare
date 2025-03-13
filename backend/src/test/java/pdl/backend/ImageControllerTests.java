package pdl.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import org.springframework.http.MediaType;

import pdl.backend.FileHandler.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class ImageControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@BeforeAll
	public static void reset() {
		try {
			Files.createDirectories(FileController.directory_location);

			FileController.remove_from_directory("test.jpg");
			FileController.remove_from_directory("test.png");

			ReflectionTestUtils.setField(Image.class, "count", Long.valueOf(0));
		} catch (Exception e) {
			System.err.println("Error in test setup: " + e.getMessage());
		}
	}

	@Test
	@Order(1)
	public void getImageListShouldReturnSuccess() throws Exception {
		this.mockMvc.perform(get("/images")).andExpect(status().isOk());
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

		ClassPathResource imgFile = new ClassPathResource("images_test/test_certain_est_test.jpg");

		MockMultipartFile file_multipart = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
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
		assertTrue(FileController.file_exists("test.jpg"));
		this.mockMvc.perform(delete("/images/0")).andExpect(status().isOk());
		assertFalse(FileController.file_exists("test.jpg"));
	}

	@Test
	@Order(6)
	public void createImageShouldReturnSuccessPNG() throws Exception {
		ReflectionTestUtils.setField(Image.class, "count", Long.valueOf(0));

		ClassPathResource imgFile = new ClassPathResource("images_test/test.png");

		MockMultipartFile file_multipart = new MockMultipartFile("file", "test.png", MediaType.IMAGE_JPEG_VALUE,
				imgFile.getInputStream());
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart)).andDo(print())
				.andExpect(status().isCreated());
	}

	@Test
	@Order(7)
	public void getImageShouldReturnSuccessPNG() throws Exception {
		this.mockMvc.perform(get("/images/0")).andExpect(status().isOk()); // a besoin d'au moins 1 images dans le
																			// dossier images
	}

	@Test
	@Order(8)
	public void deleteImageShouldReturnSuccessPNG() throws Exception {
		assertTrue(FileController.file_exists("test.png"));
		this.mockMvc.perform(delete("/images/0")).andExpect(status().isOk());
		assertFalse(FileController.file_exists("test.png"));

	}

	@Test
	@Order(9)
	public void testSystemSynchronization() throws Exception {
		// TODO : Check dao, database and physical files are in sync
		return;
	}

	@Test
	@Order(10)
	public void createImageShouldReturnBadRequest() throws Exception {
		ClassPathResource imgFile = new ClassPathResource("images_test/test.txt");
		byte[] fileContent;
		fileContent = Files.readAllBytes(imgFile.getFile().toPath());

		MockMultipartFile file_multipart = new MockMultipartFile("file", fileContent);
		// attendu par POST
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart)).andDo(print())
				.andExpect(status().isBadRequest());

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
}
