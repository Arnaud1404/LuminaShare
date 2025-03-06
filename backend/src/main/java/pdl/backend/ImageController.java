package pdl.backend;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.imageio.plugins.jpeg.JPEGImageReadParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
public class ImageController {

  @Autowired
  private ObjectMapper mapper;

  private final ImageDao imageDao;
  private final ImageService imageService;


  @Autowired
  public ImageController(ImageDao imageDao, ImageService imageService) {
    this.imageDao = imageDao;
    this.imageService = imageService;

  }

  @RequestMapping(value = "/images/{id}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
  public ResponseEntity<?> getImage(@PathVariable("id") long id) throws IOException {
    Optional<Image> img = imageDao.retrieve(id);
    if (img.isPresent()) {
      byte[] bytes = img.get().getData();
      return ResponseEntity
          .ok()
          .contentType(MediaType.IMAGE_JPEG)
          .body(bytes);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @RequestMapping(value = "/images/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteImage(@PathVariable("id") long id) {
    Optional<Image> img = imageDao.retrieve(id);
    if (img.isPresent()) {
      imageDao.delete(img.get());
      return ResponseEntity
          .ok("Image deleted successfully\n");
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  /**
    * Ajout d'une image avec indexation de ses descripteurs (histogrammes)
  */
  @RequestMapping(value = "/images", method = RequestMethod.POST)
  public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
    if (file.isEmpty()) {
      return ResponseEntity.badRequest().body("Please select a file\n");
    }

    try (InputStream inputStream = file.getInputStream()) {
      byte[] fileContent = file.getBytes();
            
      // Analyse et indexation de l'image
      String histogram2D = imageService.compute2DHistogram(imageService.readImage(fileContent)); // Génération de l'histogramme 2D
      String histogram3D = imageService.compute3DHistogram(imageService.readImage(fileContent)); // Génération de l'histogramme 3D
            
      Image img = new Image(file.getOriginalFilename(), fileContent, histogram2D, histogram3D);
      imageDao.create(img);
      return ResponseEntity.ok("Image indexed and added\n");
    } catch (IOException e) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
  }

  @RequestMapping(value = "/images", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
  @ResponseBody
  public ArrayNode getImageList() { // format attendu
    ArrayNode nodes = mapper.createArrayNode();
    List<Image> imgs = imageDao.retrieveAll();
    for (Image img : imgs) {
      ObjectNode img_json = mapper.createObjectNode();
      img_json.put("name", img.getName());
      img_json.put("id", img.getId());
      img_json.put("url", "http://localhost:8181/images/" + img.getId()); // Ajout de l'URL
      img_json.put("histogram2D", img.getHistogram2D()); // Ajout de l'histogramme 2D dans la réponse
      img_json.put("histogram3D", img.getHistogram3D()); // Ajout de l'histogramme 3D dans la réponse
      nodes.add(img_json);
    }
    return nodes;
  }
  

}
