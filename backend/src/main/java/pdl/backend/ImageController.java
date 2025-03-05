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

  @RequestMapping(value = "/images", method = RequestMethod.POST)
  public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile file,
      RedirectAttributes redirectAttributes) {
    boolean is_jpeg = false;
    if (file.isEmpty())
      return ResponseEntity.badRequest().body("please select file\n");
    // MediaType.IMAGE_JPEG_VALUE
    try (InputStream inputStream = file.getInputStream()) {
      // Check the first two bytes to see if they are FF D8 (JPEG header)
      byte[] header = new byte[2];
      if (inputStream.read(header) != 2) {
        return ResponseEntity.badRequest().body("Error occured please select JPEG file\n");

      }
      // JPEG starts with FF D8 and ends with FF D9
      is_jpeg = header[0] == (byte) 0xFF && header[1] == (byte) 0xD8;
    } catch (IOException e1) {
      is_jpeg = false;
    }
    if (!is_jpeg)
      return new ResponseEntity<>("bad file type", HttpStatus.UNSUPPORTED_MEDIA_TYPE);

    try {
      Image img = new Image(file.getOriginalFilename(), file.getBytes());
      imageDao.create(img);
      return ResponseEntity
          .ok("Image added\n");
    } catch (IOException e2) {
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
      nodes.add(img_json);
    }
    return nodes;
  }
  //route pour indexer les images avec son id 
  @RequestMapping(value = "/images/{id}/descriptors", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
  @ResponseBody
  public ResponseEntity<ObjectNode> getImageHistogram(@PathVariable("id") long id) {
    Optional<int[][]> histogramHS = imageService.getHistogramHS(id);
    Optional<int[][][]> histogramRGB = imageService.getHistogramRGB(id);

    if (histogramHS.isPresent() && histogramRGB.isPresent()) {
        ObjectNode response = mapper.createObjectNode();
        response.putPOJO("histogramHS", histogramHS.get());
        response.putPOJO("histogramRGB", histogramRGB.get());
        return ResponseEntity.ok(response);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }


}
