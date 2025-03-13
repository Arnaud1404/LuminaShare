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

@RestController
public class ImageController {

  @Autowired
  private ObjectMapper mapper;

  private final ImageDao imageDao;

  @Autowired
  private ImageRepository imageRepository;

  @Autowired
  private ImageService imageService; // Ajouté pour accéder à findSimilarImages

  @Autowired
  public ImageController(ImageDao imageDao, ImageService imageService) {
    this.imageDao = imageDao;
    this.imageService = imageService;
  }

  /**
   * Récupère une image à partir de son identifiant.
   * 
   * @param id L'identifiant de l'image à récupérer
   * @return Une réponse HTTP contenant l'image au format JPEG si elle existe,
   *         ou un statut 404 si l'image n'existe pas
   * @throws IOException En cas d'erreur lors de la lecture des données de l'image
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

  @RequestMapping(value = "/images/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteImage(@PathVariable("id") long id) {
    Optional<Image> img = imageDao.retrieve(id);
    if (!img.isPresent()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found");
    }
    if (img.isPresent()) {
      FileController.remove_from_directory(img.get().getName());
      imageDao.delete(img.get());
      imageRepository.deleteDatabase(img.get());
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
          .body("fichier sans type.");

   try {
        MediaType mediaType = ImageService.parseMediaTypeFromFile(file);
        if (mediaType == null) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body("Les formats acceptés sont JPEG et PNG, vérifiez que votre image soit conforme.");
        }

        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        if (bufferedImage == null) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Format d'image non valide.");
        }

        FileController.store(file);
        imageDao.saveImage(file.getOriginalFilename(), file.getBytes()); // Changed to saveImage
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
      img_json.put("type", img.getType().toString());
      img_json.put("size", img.getSize());
      img_json.put("description", img.getDesciption());

      img_json.put("url", "/images/" + img.getId());
      nodes.add(img_json);
    }
    return nodes;
  }
  /**Point de terminaison GET /images/{id}/similar?n={n} .
   Acceptez un paramètre id (ID de l'image cible) et n (nombre d'images similaires à retourner).
   Utilisez imageService.findSimilarImages pour obtenir la liste, puis la formater en JSON comme getImageList .
   Retourne 404 si l'image cible n'existe pas. 
  */
  @RequestMapping(value = "/images/{id}/similar", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
  @ResponseBody
  public ResponseEntity<?> getSimilarImages(@PathVariable("id") long id, @RequestParam("n") int n) {
    try {
      List<Image> similarImages = imageService.findSimilarImages(id, n);
      ArrayNode nodes = mapper.createArrayNode();
      for (Image img : similarImages) {
        ObjectNode img_json = mapper.createObjectNode();
        img_json.put("id", img.getId());
        img_json.put("name", img.getName());
        img_json.put("type", img.getType().toString());
        img_json.put("size", img.getSize());
        img_json.put("description", img.getDesciption());
        img_json.put("url", "/images/" + img.getId());
        nodes.add(img_json);
      }
      return ResponseEntity.ok(nodes);
    } catch (IllegalArgumentException e) {
        System.out.println("Not found: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
        System.out.println("Error in getSimilarImages: " + e.getMessage());
        e.printStackTrace();
        throw e;
    }
  }
}