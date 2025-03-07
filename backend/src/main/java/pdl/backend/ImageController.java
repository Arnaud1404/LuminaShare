package pdl.backend;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.imageio.plugins.jpeg.JPEGImageReadParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import pdl.backend.FileHandler.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.invoker.HttpRequestValues.Metadata;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
public class ImageController {

  @Autowired
  private ObjectMapper mapper;

  private final ImageDao imageDao;

  @Autowired
  public ImageController(ImageDao imageDao) {
    this.imageDao = imageDao;
  }

  @RequestMapping(value = "/images/{id}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
  public ResponseEntity<?> getImage(@PathVariable("id") long id) throws IOException {
    Optional<Image> img = imageDao.retrieve(id);
    if (!img.isPresent()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found");

    }
    byte[] bytes = img.get().getData();
    return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
  }

  @RequestMapping(value = "/images/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteImage(@PathVariable("id") long id) {
    Optional<Image> img = imageDao.retrieve(id);
    if (img.isPresent()) {
      FileController.remove_from_directory(img.get().getName());
      imageDao.delete(img.get());
      return ResponseEntity
          .ok("Image deleted successfully\n");
    if (!img.isPresent()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found");

    }
    imageDao.delete(img.get());
    return ResponseEntity.ok("Image deleted successfully\n");
  }

  @RequestMapping(value = "/images", method = RequestMethod.POST)
  public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile file,
      RedirectAttributes redirectAttributes) {
       if (file == null || file.getOriginalFilename() == null) {
        return ResponseEntity.badRequest().body("Invalid file.");
    }

    // Vérifie que l'extension du fichier est jpg, jpeg ou png
    String filename = file.getOriginalFilename();
    String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

    if (!(extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png"))) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                             .body("Only JPG, JPEG, and PNG images are supported.");
    }

    // Vérifie que le fichier n'est pas vide après validation du type
    if (file.isEmpty()) {
        return ResponseEntity.badRequest().body("Please select a file.");
    }

    try {
      BufferedImage buff_img = ImageIO.read(file.getInputStream());
      Image img = new Image(file.getOriginalFilename(), file.getBytes(), file.getContentType(), buff_img.getWidth(),buff_img.getHeight(),
          file.getResource().getDescription());
      imageDao.create(img);
      FileController.store(file);

      // System.out.println("affiche");
      // System.out.println("truc :" + "la mort est là");
      return ResponseEntity
          .ok("Image added\n");
    } catch (IOException e2) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        Image img = new Image(file.getOriginalFilename(), file.getBytes());
        imageDao.create(img);
        return ResponseEntity.ok("Image added successfully.");
    } catch (IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while saving the image.");
    }
  }

  @RequestMapping(value = "/images", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
  @ResponseBody
  public ArrayNode getImageList() { // format attendu
    ArrayNode nodes = mapper.createArrayNode();
    List<Image> imgs = imageDao.retrieveAll();
    for (Image img : imgs) {
      ObjectNode img_json = mapper.createObjectNode();
      img_json.put("id", img.getId());
      img_json.put("name", img.getName());
      img_json.put("type", img.getType());
      img_json.put("size", img.getSize());
      img_json.put("description", img.getDesciption());

      img_json.put("src/main/resources/images", "/images/" + img.getId());
      nodes.add(img_json);
    }
    return nodes;
  }

}
