package pdl.backend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

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
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    public void applyfilterNotFoundJPEG() throws Exception {
        this.mockMvc.perform(get("/images/0/filter?filter=gradienImage&number=100"))

                .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    public void applyfilterBadFilterJPEG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc.perform(get("/images/" + id +
                "/filter?filter=dontexist&number=100"))

                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    public void applyFilterGradienImageJPEG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=gradienImage&number=50"))

                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    public void applyFilterGradienImageBadNumberJPEG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=gradienImage&number=-50"))

                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    public void applyFilterGradienImageBadargumentJPEG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=gradienImage&number=bad"))

                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    public void applyFilterModif_LumJPEG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=modif_lum&number=50"))

                .andExpect(status().isOk());
    }

    @Test
    @Order(8)
    public void applyFilterModif_LumBadArgumentJPEG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=modif_lum&number=bad"))

                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    public void applyFilterInvertJPEG() throws Exception { // the argument is not important so doesn't need test about
                                                           // it
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=invert&number=50"))

                .andExpect(status().isOk());
    }

    @Test
    @Order(10)
    public void applyFilterRotationBadAngleJPEG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=rotation&number=50"))

                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    public void applyFilterRotation90JPEG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=rotation&number=90"))

                .andExpect(status().isOk());
    }

    @Test
    @Order(12)
    public void applyFilterRotation180JPEG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=rotation&number=180"))

                .andExpect(status().isOk());
    }

    @Test
    @Order(13)
    public void applyFilterRotation270JPEG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=rotation&number=270"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(14)
    public void applyFilterResizeSquareJPEG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=resize&number=500"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(15)
    public void applyFilterResizeRectangleJPEG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id +
                        "/filter?filter=resize&number=500&height=300"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(16)
    public void applyFilterResizeBadWidthJPEG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=resize&number=-50"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(17)
    public void applyFilterResizeBadHeightJPEG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id +
                        "/filter?filter=resize&number=500&height=-50"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(18)
    public void applyFilterMirrorhJPEG() throws Exception { // we don't care about the arguement for this filter
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=mirrorh&number=50"))
                .andExpect(status().isOk());
    }

    @Order(19)
    public void applyFilterMirrorvJPEG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=mirrorv&number=50"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(20)
    public void deleteImageSuccessJPEG() throws Exception {
        assertTrue(FileController.file_exists("test_certain_est_test12312315646216.jpg"));
        this.mockMvc.perform(delete("/images/" + (ImageDao.getImageCount() - 1))).andExpect(status().isOk());
        assertFalse(FileController.file_exists("test_certain_est_test12312315646216.jpg"));
    }

    @Test
    @Order(21)
    public void createImageSuccessPNG() throws Exception {

        ClassPathResource imgFile = new ClassPathResource("images_test/image_pour_filtre.png");

        MockMultipartFile file_multipart = new MockMultipartFile("file", "image_pour_filtre.png",
                MediaType.IMAGE_PNG_VALUE, imgFile.getInputStream());
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(file_multipart))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(22)
    public void applyfilterNotFoundPNG() throws Exception {
        this.mockMvc.perform(get("/images/0/filter?filter=gradienImage&number=100"))

                .andExpect(status().isNotFound());
    }

    @Test
    @Order(23)
    public void applyfilterBadFilterPNG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc.perform(get("/images/" + id +
                "/filter?filter=dontexist&number=100"))

                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(24)
    public void applyFilterGradienImagePNG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=gradienImage&number=50"))

                .andExpect(status().isOk());
    }

    @Test
    @Order(25)
    public void applyFilterGradienImageBadNumberPNG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=gradienImage&number=-50"))

                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(26)
    public void applyFilterGradienImageBadargumentPNG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=gradienImage&number=bad"))

                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(27)
    public void applyFilterModif_LumPNG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=modif_lum&number=50"))

                .andExpect(status().isOk());
    }

    @Test
    @Order(28)
    public void applyFilterModif_LumBadArgumentPNG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=modif_lum&number=bad"))

                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(29)
    public void applyFilterInvertPNG() throws Exception { // the argument is not important so doesn't need test about
                                                          // it
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=invert&number=50"))

                .andExpect(status().isOk());
    }

    @Test
    @Order(30)
    public void applyFilterRotationBadAnglePNG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=rotation&number=50"))

                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(31)
    public void applyFilterRotation90PNG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=rotation&number=90"))

                .andExpect(status().isOk());
    }

    @Test
    @Order(32)
    public void applyFilterRotation180PNG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=rotation&number=180"))

                .andExpect(status().isOk());
    }

    @Test
    @Order(33)
    public void applyFilterRotation270PNG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=rotation&number=270"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(34)
    public void applyFilterResizeSquarePNG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=resize&number=500"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(35)
    public void applyFilterResizeRectanglePNG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id +
                        "/filter?filter=resize&number=500&height=300"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(36)
    public void applyFilterResizeBadWidthPNG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=resize&number=-50"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(37)
    public void applyFilterResizeBadHeightPNG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id +
                        "/filter?filter=resize&number=500&height=-50"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(38)
    public void applyFilterMirrorhPNG() throws Exception { // we don't care about the arguement for this filter
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=mirrorh&number=50"))
                .andExpect(status().isOk());
    }

    @Order(39)
    public void applyFilterMirrorvPNG() throws Exception {
        id = ImageDao.getImageCount() - 1;
        this.mockMvc
                .perform(get("/images/" + id + "/filter?filter=mirrorv&number=50"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(40)
    public void deleteImageSuccessPNG() throws Exception {
        assertTrue(FileController.file_exists("image_pour_filtre.png"));
        this.mockMvc.perform(delete("/images/" + (ImageDao.getImageCount() - 1))).andExpect(status().isOk());
        assertFalse(FileController.file_exists("image_pour_filtre.png"));
    }
}
