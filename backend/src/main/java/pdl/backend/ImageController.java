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
    if (!img.isPresent()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found");
    }
    if (img.isPresent()) {
      FileController.remove_from_directory(img.get().getName());
      imageDao.delete(img.get());
      return ResponseEntity
          .ok("Image deleted successfully\n");
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found");
  }

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
          .body("fichier sans type .");

    try {
      // Vérification du format JPEG
      switch (type_file) {
        case MediaType.IMAGE_JPEG_VALUE:
          break;
        case MediaType.IMAGE_PNG_VALUE:
          break;

        default:
          return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
              .body("Les format accepter sont JPEG et PNG, vérifié que votre image soit conforme .");
      }

      // Lecture de l’image
      BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
      if (bufferedImage == null) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Format d'image non valide.");
      }
      boolean duplicateExists = imageDao.retrieveAll().stream()
          .anyMatch(img -> img.getName().equals(file.getOriginalFilename()));

      if (duplicateExists) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body("Une image avec ce nom existe déjà.");
      }
      // Création et stockage de l’image
      Image img = new Image(file.getOriginalFilename(), file.getBytes(), file.getContentType(),
          bufferedImage.getWidth(), bufferedImage.getHeight(), "/images/");
      imageDao.create(img);
      FileController.store(file);

      return ResponseEntity.ok("Image ajoutée avec succès.");
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Erreur lors de l'enregistrement de l'image.");
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

      img_json.put("url", "/images/" + img.getId());
      nodes.add(img_json);
    }
    return nodes;
  }

}