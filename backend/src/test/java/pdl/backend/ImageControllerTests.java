package pdl.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
		// reset Image class static counter
		ReflectionTestUtils.setField(Image.class, "count", Long.valueOf(1));
		try {
			FileController.remove_from_directory("test.jpg");

		} catch (RuntimeException e) {
		}
		try {
			FileController.remove_from_directory("test.png");

		} catch (RuntimeException e) {
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

		ClassPathResource imgFile = new ClassPathResource("images_test/test_certain_est_test.jpg");

		MockMultipartFile file_multipart = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
				imgFile.getInputStream());
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart)).andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	@Order(4)
	public void getImageShouldReturnSuccessJPEG() throws Exception {
		this.mockMvc.perform(get("/images/1")).andExpect(status().isOk()); // a besoin d'au moins 1 images dans le
																			// dossier images
	}

	@Test
	@Order(5)
	public void deleteImageShouldReturnSuccessJPEG() throws Exception {
		this.mockMvc.perform(delete("/images/1")).andExpect(status().isOk());
	}

	@Test
	@Order(6)
	public void createImageShouldReturnSuccessPNG() throws Exception {
		ReflectionTestUtils.setField(Image.class, "count", Long.valueOf(0));

		ClassPathResource imgFile = new ClassPathResource("images_test/test_certain_est_test.jpg");

		MockMultipartFile file_multipart = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
				imgFile.getInputStream());
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart)).andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	@Order(7)
	public void getImageShouldReturnSuccessPNG() throws Exception {
		this.mockMvc.perform(get("/images/2")).andExpect(status().isOk()); // a besoin d'au moins 1 images dans le
																			// dossier images
	}

	@Test
	@Order(8)
	public void deleteImageShouldReturnSuccessPNG() throws Exception {
		this.mockMvc.perform(delete("/images/2")).andExpect(status().isOk());
	}

	@Test
	@Order(9)
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
	@Order(10)
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
	public void deleteImagesShouldReturnMethodNotAllowed() throws Exception {
		this.mockMvc.perform(delete("/images")).andExpect(status().isMethodNotAllowed());
	}

	@Test
	@Order(12)
	public void deleteImageShouldReturnNotFound() throws Exception {
		this.mockMvc.perform(delete("/images/-1")).andExpect(status().isNotFound());
	}

	@Test
	@Order(13)
	public void imageShouldNotBeFound() throws Exception {
		this.mockMvc.perform(get("/images/0")).andExpect(status().isNotFound());
	}
}
