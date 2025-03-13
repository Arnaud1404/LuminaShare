package pdl.backend;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import pdl.backend.Database.ImageRepository;
import pdl.backend.FileHandler.*;
import pdl.backend.Database.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles HTTP requests for image operations.
 * 
 * IMPORTANT: Coordinates synchronization between ImageDao (memory storage),
 * ImageRepository (database storage), and FileController (filesystem storage).
 * All image operations should go through this controller to maintain
 * consistency across the three storage systems.
 */
@RestController
public class ImageController {

  @Autowired
  private ObjectMapper mapper;

  private final ImageDao imageDao;

  @Autowired
  private ImageRepository imageRepository;

  @Autowired
  public ImageController(ImageDao imageDao) {
    this.imageDao = imageDao;
  }

  /**
   * Gets an image from its id
   * 
   * @param id The ID of the image
   * @return An HTTP Response with the image bytes, or NOT_FOUND if image doesn't
   *         exist
   */
  @RequestMapping(value = "/images/{id}", method = RequestMethod.GET, produces = { MediaType.IMAGE_JPEG_VALUE,
      MediaType.IMAGE_PNG_VALUE })
  public ResponseEntity<?> getImage(@PathVariable("id") long id) throws IOException {
    Optional<Image> img = imageDao.retrieve(id);
    if (img.isPresent()) {
      byte[] bytes = img.get().getData();
      MediaType mediaType = img.get().getType();
      return ResponseEntity
          .ok()
          .contentType(mediaType)
          .body(bytes);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  /**
   * Deletes an image by its id from all three storage systems
   * 
   * @param id The ID of the image to delete
   * @return OK if deletion succeeded, NOT_FOUND if image doesn't exist
   */
  @RequestMapping(value = "/images/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteImage(@PathVariable("id") long id) {
    Optional<Image> img = imageDao.retrieve(id);
    if (!img.isPresent()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found");
    }
    if (img.isPresent()) {
      FileController.remove_from_directory(img.get().getName());
      imageDao.delete(img.get());
      imageRepository.deleteDatabase(id);
      return ResponseEntity
          .ok("Image deleted successfully\n");
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found");
  }

  /**
   * Adds a new image from an uploaded file
   * Stores the image in memory, database, and filesystem
   * 
   * @param file               The uploaded image file
   * @param redirectAttributes Spring redirect attributes
   * @return OK if successful, BAD_REQUEST if file is invalid
   */
  @RequestMapping(value = "/images", method = RequestMethod.POST)
  public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
    // Vérification des erreurs dans un seul bloc
    if (file == null || file.isEmpty()) {
      return ResponseEntity.badRequest().body("Veuillez sélectionner un fichier.");
    }

    String contentType = file.getContentType();
    String filename = file.getOriginalFilename();

    if (filename == null || contentType == null || !contentType.startsWith("image/")) {
      return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
          .body("Format de fichier non supporté ou nom de fichier invalide.");
    }

    String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    if (!List.of("jpg", "jpeg", "png").contains(extension)) {
      return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
          .body("Seuls les fichiers JPG, JPEG et PNG sont autorisés.");
    }
    String type_file = file.getContentType();
    if (type_file == null)
      return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
          .body("fichier sans type.");

    try {
      MediaType mediaType = ImageService.parseMediaTypeFromFile(file);
      if (mediaType == null) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body("Les formats acceptés sont JPEG et PNG, vérifiez que votre image soit conforme.");
      }

      // Lecture de l’image
      BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
      if (bufferedImage == null) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Format d'image non valide.");
      }

      MediaType type = ImageService.parseMediaTypeFromFile(file);

      FileController.store(file);
      Image img = new Image(
          FileController.directory_location.toString(),
          file.getOriginalFilename(),
          file.getBytes(),
          type,
          bufferedImage.getWidth(),
          bufferedImage.getHeight(),
          "TODO");
      imageDao.create(img);

      imageRepository.addDatabase(img);

      return ResponseEntity.ok("Image ajoutée avec succès.");
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Erreur lors de l'enregistrement de l'image.");
    }
  }

  /**
   * Lists all images in memory
   * 
   * @return JSON array with image metadata
   */
  @RequestMapping(value = "/images", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
  @ResponseBody
  public ArrayNode getImageList() { // format attendu
    ArrayNode nodes = mapper.createArrayNode();
    List<Image> imgs = imageDao.retrieveAll();
    for (Image img : imgs) {
      ObjectNode img_json = mapper.createObjectNode();
      img_json.put("id", img.getId());
      img_json.put("name", img.getName());
      img_json.put("type", img.getType().toString());
      img_json.put("size", img.getSize());
      img_json.put("description", img.getDesciption());

      img_json.put("url", "/images/" + img.getId());
      nodes.add(img_json);
    }
    return nodes;
  }

}