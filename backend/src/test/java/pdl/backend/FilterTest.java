package pdl.backend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import pdl.backend.FileHandler.FileController;
import pdl.backend.Image.ImageDao;

import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)

public class FilterTest {

    @Autowired
    private MockMvc mockMvc;

    private long id;

    @Test
    @Order(1)
    public void createImageSuccessJPEG() throws Exception {

        ClassPathResource imgFile = new ClassPathResource("images_test/test_certain_est_test12312315646216.jpg");

        MockMultipartFile file_multipart = new MockMultipartFile("file", "test_certain_est_test12312315646216.jpg",
                MediaType.IMAGE_JPEG_VALUE, imgFile.getInputStream());
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart))
                .andDo(print()).andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    public void applyfilterNotFound() throws Exception {
        this.mockMvc.perform(get("/images/0/filter?filter=gradienImage&number=100"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    public void applyfilterBadFilter() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc.perform(get("/images/" + id + "/filter?filter=dontexist&number=100"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    public void applyFilterGradienImage() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=gradienImage&number=50"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    public void applyFilterModif_Lum() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=modif_lum&number=50"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Order(5)
    public void applyFilterInvert() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=invert&number=50"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Order(5)
    public void applyFilterRotation() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=rotation&number=50"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Order(5)
    public void applyFilterResize() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=resize&number=50"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Order(5)
    public void applyFilterMirrorh() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=mirrorh&number=50"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Order(5)
    public void applyFilterMirrorv() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=mirrorv&number=50"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(26)
    public void deleteImageSuccessJPEG() throws Exception {
        assertTrue(FileController.file_exists("test_certain_est_test12312315646216.jpg"));
        this.mockMvc.perform(delete("/images/" + (ImageDao.getImageCount() - 1))).andExpect(status().isOk());
        assertFalse(FileController.file_exists("test_certain_est_test12312315646216.jpg"));
    }
}
