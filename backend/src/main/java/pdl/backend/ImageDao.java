package pdl.backend;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

@Repository
public class ImageDao implements Dao<Image> {

  private final Map<Long, Image> images = new HashMap<>();
  private long idCounter = 1L;// Compteur d'ID pour assurer des IDs uniques

  /**
    * Sauvegarde une image en lui attribuant un ID unique.
  */
  public void saveImage(String fileName, byte[] fileContent) {
    Image img = new Image(fileName, fileContent,"jpeg", 800, 600, "/images/");
    img.setId(idCounter++);
    images.put(img.getId(), img);
  }

  public ImageDao() {
    // placez une image test.jpg dans le dossier "src/main/resources" du projet
    final ClassPathResource imgFile = new ClassPathResource("images_test/test.jpg");
    byte[] fileContent;
    try {
      fileContent = Files.readAllBytes(imgFile.getFile().toPath());
      String type = "jpeg";
      BufferedImage buff_img = ImageIO.read(imgFile.getInputStream());
      Image img = new Image(imgFile.getFile().getName(), fileContent, type, buff_img.getWidth(),buff_img.getHeight(),
          imgFile.getDescription());

      images.put(img.getId(), img);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Optional<Image> retrieve(final long id) {
    Optional<Image> img = Optional.ofNullable(images.get(id));
    return img;
  }

  @Override
  public List<Image> retrieveAll() {
    return new ArrayList<Image>(images.values());
  }

  @Override
  public void create(final Image img) {
    img.setId(idCounter++);
    images.put(img.getId(), img);
  }

  @Override
  public void update(final Image img, final String[] params) {
    // Not used
  }

  @Override
  public void delete(final Image img) {
    images.remove(img.getId());
  }
}
