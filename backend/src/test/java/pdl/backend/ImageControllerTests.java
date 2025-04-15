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
import pdl.backend.Image.Image;
import pdl.backend.Image.ImageDao;
import pdl.backend.Image.ImageService;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class ImageControllerTests {

	@Autowired
	private MockMvc mockMvc;

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
		// ReflectionTestUtils.setField(Image.class, "count", Long.valueOf(0));

		ClassPathResource imgFile = new ClassPathResource("images_test/test_certain_est_test12312315646216.jpg");

		MockMultipartFile file_multipart = new MockMultipartFile("file", "test_certain_est_test12312315646216.jpg",
				MediaType.IMAGE_JPEG_VALUE, imgFile.getInputStream());
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart))
				.andDo(print()).andExpect(status().isCreated());
	}

	@Test
	@Order(4)
	public void getImageShouldReturnSuccessJPEG() throws Exception {
		this.mockMvc.perform(get("/images/" + (ImageDao.getImageCount() - 1))).andExpect(status().isOk());
	}

	@Test
	@Order(5)
	public void deleteImageShouldReturnSuccessJPEG() throws Exception {
		assertTrue(FileController.file_exists("test_certain_est_test12312315646216.jpg"));
		this.mockMvc.perform(delete("/images/" + (ImageDao.getImageCount() - 1))).andExpect(status().isOk());
		assertFalse(FileController.file_exists("test_certain_est_test12312315646216.jpg"));
	}

	@Test
	@Order(6)
	public void createImageShouldReturnSuccessPNG() throws Exception {

		ClassPathResource imgFile = new ClassPathResource("images_test/test_certain_est_test12312315646216.png");

		MockMultipartFile file_multipart = new MockMultipartFile("file", "test_certain_est_test12312315646216.png",
				MediaType.IMAGE_JPEG_VALUE, imgFile.getInputStream());
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart))
				.andDo(print()).andExpect(status().isCreated());
	}

	@Test
	@Order(7)
	public void getImageShouldReturnSuccessPNG() throws Exception {
		this.mockMvc.perform(get("/images/" + (ImageDao.getImageCount() - 1))).andExpect(status().isOk());
	}

	@Test
	@Order(8)
	public void deleteImageShouldReturnSuccessPNG() throws Exception {
		assertTrue(FileController.file_exists("test_certain_est_test12312315646216.png"));
		this.mockMvc.perform(delete("/images/" + (ImageDao.getImageCount() - 1))).andExpect(status().isOk());
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
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart))
				.andDo(print()).andExpect(status().isNoContent());
	}

	@Test
	@Order(11)
	public void createImageShouldReturnUnsupportedMediaType() throws Exception {
		ClassPathResource imgFile = new ClassPathResource("images_test/test.gif");
		byte[] fileContent;
		fileContent = Files.readAllBytes(imgFile.getFile().toPath());

		MockMultipartFile file_multipart = new MockMultipartFile("file", fileContent);
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart))
				.andDo(print()).andExpect(status().isUnsupportedMediaType());

	}

	@Test
	@Order(11)
	public void createImageShouldReturnUnsupportedMediaType2() throws Exception {
		ClassPathResource imgFile = new ClassPathResource("images_test/test.txt");
		byte[] fileContent;
		fileContent = Files.readAllBytes(imgFile.getFile().toPath());

		MockMultipartFile file_multipart = new MockMultipartFile("file", fileContent);
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart))
				.andDo(print()).andExpect(status().isUnsupportedMediaType());

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
