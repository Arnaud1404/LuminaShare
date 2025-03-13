package pdl.backend;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import org.springframework.http.MediaType;

import static org.mockito.Mockito.*;

import pdl.backend.FileHandler.*;




@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class ImageControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
    private ImageDao imageDao;

	@BeforeAll
	public static void reset() {
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
		List<Image> mockImages = Arrays.asList(
            new Image("test.jpg", null, MediaType.IMAGE_JPEG, 100, 100, "test")
        );
        when(imageDao.retrieveAll()).thenReturn(mockImages);
		this.mockMvc.perform(get("/images")).andExpect(status().isOk());
	}

	@Test
	@Order(2)
	public void getImageShouldReturnNotFound() throws Exception {
		when(imageDao.retrieve(-1L)).thenReturn(Optional.empty());

        this.mockMvc.perform(get("/images/-1")).andExpect(status().isNotFound());
	}

	@Test
	@Order(3)
	public void createImageShouldReturnSuccessJPEG() throws Exception {
		ClassPathResource imgFile = new ClassPathResource("images_test/test_certain_est_test.jpg");
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE,
                imgFile.getInputStream());

        Image mockImage = new Image("test.jpg", file.getBytes(), MediaType.IMAGE_JPEG, 100, 100, "test");
        mockImage.setId(0L);
		doAnswer(invocation -> {
            Image arg = invocation.getArgument(0);
            arg.setId(0L); // Simulate ID being set after creation
            return null; // Void method returns null
        }).when(imageDao).create(any(Image.class));
        this.mockMvc.perform(multipart("/images").file(file))
            .andDo(print())
            .andExpect(status().isOk()
		);
	}
	@Test
	@Order(4)
	public void getImageShouldReturnSuccessJPEG() throws Exception {
		Image mockImage = new Image("test.jpg", null, MediaType.IMAGE_JPEG, 100, 100, "test");
        mockImage.setId(0L);
        when(imageDao.retrieve(0L)).thenReturn(Optional.of(mockImage));
		this.mockMvc.perform(get("/images/0")).andExpect(status().isOk()); // a besoin d'au moins 1 images dans le dossier images
	}

	@Test
	@Order(5)
	public void deleteImageShouldReturnSuccessJPEG() throws Exception {
        Image mockImage = new Image("test.jpg", null, MediaType.IMAGE_JPEG, 100, 100, "test");
        mockImage.setId(0L);
        when(imageDao.retrieve(0L)).thenReturn(Optional.of(mockImage));
        doNothing().when(imageDao).delete(mockImage);
        
		this.mockMvc.perform(delete("/images/0")).andExpect(status().isOk());
	}

	@Test
	@Order(6)
	public void createImageShouldReturnSuccessPNG() throws Exception {
		ClassPathResource imgFile = new ClassPathResource("images_test/test.png");
		MockMultipartFile file = new MockMultipartFile("file", "test.png", MediaType.IMAGE_PNG_VALUE,
                imgFile.getInputStream());

        Image mockImage = new Image("test.png", file.getBytes(), MediaType.IMAGE_PNG, 100, 100, "test");
        mockImage.setId(0L);
		doAnswer(invocation -> {
            Image arg = invocation.getArgument(0);
            arg.setId(0L); // Simulate ID being set after creation
            return null; // Void method returns null
        }).when(imageDao).create(any(Image.class));
        this.mockMvc.perform(multipart("/images").file(file))
            .andDo(print())
            .andExpect(status().isOk()
	    );
	}

	@Test
	@Order(7)
	public void getImageShouldReturnSuccessPNG() throws Exception {
		Image mockImage = new Image("test.png", null, MediaType.IMAGE_PNG, 100, 100, "test");
        mockImage.setId(0L);
        when(imageDao.retrieve(0L)).thenReturn(Optional.of(mockImage));
		this.mockMvc.perform(get("/images/0")).andExpect(status().isOk()); // a besoin d'au moins 1 images dans le
																			// dossier images
	}

	@Test
	@Order(8)
	public void deleteImageShouldReturnSuccessPNG() throws Exception {
		Image mockImage = new Image("test.png", null, MediaType.IMAGE_PNG, 100, 100, "test");
        mockImage.setId(0L);
        when(imageDao.retrieve(0L)).thenReturn(Optional.of(mockImage));
        doNothing().when(imageDao).delete(mockImage);

		this.mockMvc.perform(delete("/images/0")).andExpect(status().isOk());
	}

	@Test
	@Order(9)
	public void createImageShouldReturnBadRequest() throws Exception {
		ClassPathResource imgFile = new ClassPathResource("images_test/test.txt");
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE,
                imgFile.getInputStream());

        this.mockMvc.perform(multipart("/images").file(file)).andDo(print()).andExpect(status().isBadRequest());
	}

	@Test
	@Order(10)
	public void createImageShouldReturnUnsupportedMediaType() throws Exception {
		ClassPathResource imgFile = new ClassPathResource("images_test/test.gif");
        MockMultipartFile file = new MockMultipartFile("file", "test.gif", "image/gif",
                imgFile.getInputStream());

        this.mockMvc.perform(multipart("/images").file(file)).andDo(print()).andExpect(status().isUnsupportedMediaType());

	}

	@Test
	@Order(11)
	public void deleteImagesShouldReturnMethodNotAllowed() throws Exception {
		this.mockMvc.perform(delete("/images")).andExpect(status().isMethodNotAllowed());
	}

	@Test
	@Order(12)
	public void deleteImageShouldReturnNotFound() throws Exception {
		when(imageDao.retrieve(-1L)).thenReturn(Optional.empty());
		this.mockMvc.perform(delete("/images/-1")).andExpect(status().isNotFound());
	}

	@Test
	@Order(13)
	public void imageShouldNotBeFound() throws Exception {
		when(imageDao.retrieve(0L)).thenReturn(Optional.empty());
		this.mockMvc.perform(get("/images/0")).andExpect(status().isNotFound());
	}
}
