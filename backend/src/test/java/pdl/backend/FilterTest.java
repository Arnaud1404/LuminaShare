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

        ClassPathResource imgFile = new ClassPathResource("images_test/image_pour_filtre.png");

        MockMultipartFile file_multipart = new MockMultipartFile("file", "image_pour_filtre.png",
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
    public void applyFilterGradienImageBadNumber() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=gradienImage&number=-50"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    public void applyFilterGradienImageBadargument() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=gradienImage&number=bad"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    public void applyFilterModif_Lum() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=modif_lum&number=50"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(8)
    public void applyFilterModif_LumBadArgument() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=modif_lum&number=bad"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    public void applyFilterInvert() throws Exception { // the argument is not important so doesn't need test about it
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=invert&number=50"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(10)
    public void applyFilterRotationBadAngle() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=rotation&number=50"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    public void applyFilterRotation90() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=rotation&number=90"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(12)
    public void applyFilterRotation180() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=rotation&number=180"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(13)
    public void applyFilterRotation270() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=rotation&number=270"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(14)
    public void applyFilterResizeSquare() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=resize&number=50"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(14)
    public void applyFilterResize() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=resize&number=500&height=300"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(14)
    public void applyFilterResizeBadWidth() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=resize&number=-50"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Order(14)
    public void applyFilterResizeBadHeight() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=resize&number=500&height=-50"))
                .andDo(print())
                .andExpect(status().isBadRequest());
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
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(26)
    public void deleteImageSuccessJPEG() throws Exception {
        assertTrue(FileController.file_exists("image_pour_filtre.png"));
        this.mockMvc.perform(delete("/images/" + (ImageDao.getImageCount() - 1))).andExpect(status().isOk());
        assertFalse(FileController.file_exists("image_pour_filtre.png"));
    }
}
