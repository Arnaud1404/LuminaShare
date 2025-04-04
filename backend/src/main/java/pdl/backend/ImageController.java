package pdl.backend;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.util.Optional;
import javax.imageio.ImageIO;
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
import org.springframework.http.HttpStatusCode;
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

@RestController
public class ImageController {

  @Autowired
  private ObjectMapper mapper;

  private final ImageDao imageDao;

  @Autowired
  private ImageRepository imageRepository;

  @Autowired
  private ImageService imageService;

  @Autowired
  public ImageController(ImageDao imageDao) {
    this.imageDao = imageDao;
    this.imageService = imageService;
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
      imageDao.delete(img.get());
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
      return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Veuillez sélectionner un fichier.");
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
    try {

      // Lecture de l’image
      BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
      if (bufferedImage == null) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Format d'image non valide.");
      }

      MediaType type = ImageService.parseMediaTypeFromFile(file);

      Image img = new Image(
          FileController.directory_location.toString(),
          file.getOriginalFilename(),
          file.getBytes(),
          type,
          bufferedImage.getWidth(),
          bufferedImage.getHeight());
      imageDao.create(img, file);

      return ResponseEntity.status(HttpStatus.CREATED).body("Image ajoutée avec succès.");
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
  @RequestMapping(value = "/images", method = RequestMethod.GET, produces = "application/json")
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
      img_json.put("similarity", img.getSimilarityScore());
      img_json.put("url", "/images/" + img.getId());
      nodes.add(img_json);
    }
    return nodes;
  }

  /**
   * Gets a list of similar images to the one with the given id
   * 
   * @param id         The ID of the image to compare
   * @param n          The number of similar images to return
   * @param descriptor The descriptor to use for comparison (e.g. "rgbcube")
   * @return JSON array with similar image metadata
   */
  @RequestMapping(value = "/images/{id}/similar", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
  @ResponseBody
  public ResponseEntity<?> getSimilarImages(@PathVariable("id") long id, @RequestParam("number") int n,
      @RequestParam("descriptor") String descriptor) {
    try {
      if (n <= 0) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le paramètre 'number' doit être supérieur à 0");
      }

      Image image = imageDao.retrieve(id).get();
      List<Image> similarImages = imageRepository.imageSimilar(image, descriptor, n);

      ArrayNode nodes = mapper.createArrayNode();
      for (Image img : similarImages) {
        ObjectNode img_json = mapper.createObjectNode();
        img_json.put("id", img.getId());
        img_json.put("name", img.getName());
        img_json.put("type", img.getType().toString());
        img_json.put("size", img.getSize());
        img_json.put("similarity", img.getSimilarityScore());

        nodes.add(img_json);
      }
      return ResponseEntity.ok(nodes);
    } catch (IllegalArgumentException e) {
      if (e.getMessage().contains("non trouvée")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
      } else {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
      }
    }
  }
  /**
   * Handles the resizing of an image with the specified ID.
   *
   * @param id The ID of the image to be resized.
   * @return A ResponseEntity containing the result of the operation:
   *         - HTTP 200 (OK) with a success message if the image is resized successfully.
   *         - HTTP 404 (NOT FOUND) if the image with the specified ID is not found.
   *         - HTTP 400 (BAD REQUEST) if the image data is invalid or corrupted.
   *         - HTTP 500 (INTERNAL SERVER ERROR) if an error occurs during the resizing process.
   */
  @RequestMapping(value = "/images/{id}/resize", method = RequestMethod.POST, produces = MediaType.IMAGE_JPEG_VALUE)
  public ResponseEntity<?> resizeImage(@PathVariable("id") long id, 
                                       @RequestParam("width") int width,
                                       @RequestParam("height") int height) {
    try {
        // Validation des dimensions
        if (width <= 0 || height <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Les dimensions doivent être positives.");
        }
        Optional<Image> optionalImage = imageDao.retrieve(id);
        if (optionalImage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image introuvable.");
        }

        Image image = optionalImage.get();
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(image.getData()));

        if (originalImage == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("L'image est corrompue ou invalide.");
        }

        BufferedImage resizedImage = imageService.resizeImage(originalImage, width, height);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpeg", outputStream);

        image.setData(outputStream.toByteArray());
        image.setWidth(width);
        image.setHeight(height);
        imageDao.create(image);

        return ResponseEntity.ok().body("Image redimensionnée avec succès.");
    } catch (IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors du redimensionnement de l'image.");
    }
  }
  /**
   * Handles the inversion of colors for an image identified by its ID.
   *
   * @param id The ID of the image to invert colors for.
   * @return A ResponseEntity containing:
   *         - HTTP 200 (OK) with a success message if the colors were inverted successfully.
   *         - HTTP 404 (NOT FOUND) if the image with the given ID does not exist.
   *         - HTTP 400 (BAD REQUEST) if the image data is corrupted or invalid.
   *         - HTTP 500 (INTERNAL SERVER ERROR) if an error occurs during processing.
   */
  @RequestMapping(value = "/images/{id}/invert", method = RequestMethod.POST, produces = MediaType.IMAGE_JPEG_VALUE)
  public ResponseEntity<?> invertImageColors(@PathVariable("id") long id) {
    try {
        Optional<Image> optionalImage = imageDao.retrieve(id);
        if (optionalImage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image introuvable.");
        }

        Image image = optionalImage.get();
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(image.getData()));

        if (originalImage == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("L'image est corrompue ou invalide.");
        }

        BufferedImage invertedImage = imageService.invertColors(originalImage);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(invertedImage, "jpeg", outputStream);

        image.setData(outputStream.toByteArray());
        imageDao.create(image);

        return ResponseEntity.ok().body("Couleurs inversées avec succès.");
    } catch (IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'inversion des couleurs.");
    }
  }
  
}