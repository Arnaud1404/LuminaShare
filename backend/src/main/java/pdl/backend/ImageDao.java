package pdl.backend;

import java.util.Arrays;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    Image img = new Image(fileName, fileContent);
    img.setId(idCounter++);
    images.put(img.getId(), img);
  }

  public ImageDao() {
    // placez une image test.jpg dans le dossier "src/main/resources" du projet
    final ClassPathResource imgFile = new ClassPathResource("images/test.jpg");
    byte[] fileContent;
    try {
      fileContent = Files.readAllBytes(imgFile.getFile().toPath());
      Image img = new Image("default.jpg", fileContent);
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
    System.out.println(" Image enregistrée avec ID : " + img.getId());
    System.out.println(" Histogramme HS : " + Arrays.deepToString(img.getHistogramHS()));
    System.out.println(" Histogramme RGB : " + Arrays.deepToString(img.getHistogramRGB()));
  }

  @Override
  public void update(final Image img, final String[] params) {
    // Not used
  }

  @Override
  public void delete(final Image img) {
    images.remove(img.getId());
  }
  /**
   * Récupère l'histogramme HS d'une image donnée par son ID.
   */
  public int[][] getHistogramHS(long id) {
    return images.containsKey(id) ? images.get(id).getHistogramHS() : null;
  }

  /**
   * Récupère l'histogramme RGB d'une image donnée par son ID.
   */
  public int[][][] getHistogramRGB(long id) {
    return images.containsKey(id) ? images.get(id).getHistogramRGB() : null;
  }
}
