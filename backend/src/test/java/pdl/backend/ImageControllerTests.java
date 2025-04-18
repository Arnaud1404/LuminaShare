package pdl.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
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
import pdl.backend.Image.ImageRepository;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class ImageControllerTests {

	@Autowired
	private MockMvc mockMvc;

	private String json = "application/json;charset=UTF-8";

	@BeforeAll
	public static void setup() throws IOException {
		ReflectionTestUtils.setField(Image.class, "count", Long.valueOf(1));

	}

	@Test
	@Order(1)
	public void getImageListSuccess() throws Exception {
		this.mockMvc.perform(get("/images")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().contentType(json));
	}

	@Test
	@Order(2)
	public void getImageNotFound() throws Exception {
		this.mockMvc.perform(get("/images/-1")).andExpect(status().isNotFound());
	}

	@Test
	@Order(3)
	public void createImageSuccessJPEG() throws Exception {

		ClassPathResource imgFile = new ClassPathResource("images_test/test_certain_est_test12312315646216.jpg");

		MockMultipartFile file_multipart = new MockMultipartFile("file", "test_certain_est_test12312315646216.jpg",
				MediaType.IMAGE_JPEG_VALUE, imgFile.getInputStream());
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart))
				.andDo(print()).andExpect(status().isCreated());
	}

	@Test
	@Order(4)
	public void getImageSuccessJPEG() throws Exception {
		this.mockMvc.perform(get("/images/" + (ImageDao.getImageCount() - 1))).andExpect(status().isOk());
	}

	@Test
	@Order(5)
	public void deleteImageSuccessJPEG() throws Exception {
		assertTrue(FileController.file_exists("test_certain_est_test12312315646216.jpg"));
		this.mockMvc.perform(delete("/images/" + (ImageDao.getImageCount() - 1))).andExpect(status().isOk());
		assertFalse(FileController.file_exists("test_certain_est_test12312315646216.jpg"));
	}

	@Test
	@Order(6)
	public void createImageSuccessPNG() throws Exception {

		ClassPathResource imgFile = new ClassPathResource("images_test/test_certain_est_test12312315646216.png");

		MockMultipartFile file_multipart = new MockMultipartFile("file", "test_certain_est_test12312315646216.png",
				MediaType.IMAGE_JPEG_VALUE, imgFile.getInputStream());
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart))
				.andDo(print()).andExpect(status().isCreated());
	}

	@Test
	@Order(7)
	public void putsetLikeCountOk() throws Exception {
		this.mockMvc.perform(put("/images/" + (ImageDao.getImageCount() - 1) + "/set-likes?likes=1"))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	@Order(8)
	public void putsetLikeCountBadRequest() throws Exception {
		this.mockMvc.perform(put("/images/" + (ImageDao.getImageCount() - 1) + "/set-likes?likes=-50"))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@Order(9)
	public void getImageSuccessPNG() throws Exception {
		this.mockMvc.perform(get("/images/" + (ImageDao.getImageCount() - 1))).andExpect(status().isOk());
	}

	@Test
	@Order(10)
	public void getImageSimilarSuccessHue() throws Exception {
		this.mockMvc.perform(get("/images/" + (ImageDao.getImageCount() - 1) + "/similar?number=5&descriptor=huesat"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(json));
	}

	@Test
	@Order(11)
	public void getImageSimilarSuccessRGBCube() throws Exception {
		this.mockMvc.perform(get("/images/" + (ImageDao.getImageCount() - 1) + "/similar?number=5&descriptor=rgbcube"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(json));
	}

	@Test
	@Order(12)
	public void getImageSimilarBadRequestDescriptor() throws Exception {
		this.mockMvc.perform(get("/images/" + (ImageDao.getImageCount() - 1) +
				"/similar?number=5&descriptor=bad"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@Order(13)
	public void getImageSimilarBadRequestNumber() throws Exception {
		this.mockMvc.perform(get("/images/" + (ImageDao.getImageCount() - 1) + "/similar?number=-1&descriptor=huesat"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@Order(14)
	public void getcheckLikeStatusSucessFalse() throws Exception {
		this.mockMvc.perform(get("/images/" + (ImageDao.getImageCount() - 1) + "/like-status?userid=admin"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(json))
				.andExpect(jsonPath("isLiked").value("false"));
	}

	@Test
	@Order(15)
	public void getToggleLikeSucess() throws Exception {
		this.mockMvc.perform(put("/images/" + (ImageDao.getImageCount() - 1) + "/toggle-like?userid=admin"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(json));

	}

	@Test
	@Order(16)
	public void getToggleLikeBadRequest() throws Exception {
		this.mockMvc.perform(put("/images/" + (ImageDao.getImageCount() - 1) + "/toggle-like?userid="))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@Order(17)
	public void getToggleLikeNotFound() throws Exception {
		this.mockMvc.perform(put("/images/" + 0 + "/toggle-like?userid=admin"))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	@Order(18)
	public void deleteImageSuccessPNG() throws Exception {
		assertTrue(FileController.file_exists("test_certain_est_test12312315646216.png"));
		this.mockMvc.perform(delete("/images/" + (ImageDao.getImageCount() - 1))).andExpect(status().isOk());
		assertFalse(FileController.file_exists("test_certain_est_test12312315646216.png"));

	}

	@Test
	@Order(19)
	public void createImageNoContent() throws Exception {
		ClassPathResource imgFile = new ClassPathResource("images_test/empty.txt");
		byte[] fileContent;
		fileContent = Files.readAllBytes(imgFile.getFile().toPath());

		MockMultipartFile file_multipart = new MockMultipartFile("file", fileContent);
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart))
				.andDo(print()).andExpect(status().isNoContent());
	}

	@Test
	@Order(20)
	public void createImageUnsupportedMediaType() throws Exception {
		ClassPathResource imgFile = new ClassPathResource("images_test/test.gif");
		byte[] fileContent;
		fileContent = Files.readAllBytes(imgFile.getFile().toPath());

		MockMultipartFile file_multipart = new MockMultipartFile("file", fileContent);
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart))
				.andDo(print()).andExpect(status().isUnsupportedMediaType());

	}

	@Test
	@Order(21)
	public void createImageUnsupportedMediaType2() throws Exception {
		ClassPathResource imgFile = new ClassPathResource("images_test/test.txt");
		byte[] fileContent;
		fileContent = Files.readAllBytes(imgFile.getFile().toPath());

		MockMultipartFile file_multipart = new MockMultipartFile("file", fileContent);
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart))
				.andDo(print()).andExpect(status().isUnsupportedMediaType());

	}

	@Test
	@Order(22)
	public void deleteImagesMethodNotAllowed() throws Exception {
		this.mockMvc.perform(delete("/images")).andExpect(status().isMethodNotAllowed());
	}

	@Test
	@Order(23)
	public void deleteImageNotFound() throws Exception {
		this.mockMvc.perform(delete("/images/-1")).andExpect(status().isNotFound());
	}

	@Test
	@Order(24)
	public void imageShouldNotBeFound() throws Exception {
		this.mockMvc.perform(get("/images/0")).andExpect(status().isNotFound());
	}

	@Test
	@Order(25)
	public void getImageSimilarBadRequestId() throws Exception {
		this.mockMvc.perform(get("/images/-1/similar?number=5&descriptor=huesat")).andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@Order(26)
	public void getUserImageSucess() throws Exception {
		this.mockMvc.perform(get("/images/user/admin")).andDo(print())
				.andExpect(content().contentType(json));
	}

	@Test
	@Order(27)
	public void getcheckLikeStatusBadRequest() throws Exception {
		this.mockMvc.perform(get("/images/" + 0 + "/like-status?userid="))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@Order(28)
	public void putsetLikeCountBadID() throws Exception {
		this.mockMvc.perform(put("/images/" + 0 + "/set-likes?likes=1"))
				.andDo(print())
				.andExpect(status().isNotFound());
	}
}
