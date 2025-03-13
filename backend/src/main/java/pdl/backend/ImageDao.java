package pdl.backend;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;

import pdl.backend.FileHandler.FileController;

@Repository
public class ImageDao implements Dao<Image> {

  private final Map<Long, Image> images = new HashMap<>();

  public long getImageCount() {
    return images.size();
  }

  public void saveImage(String fileName, byte[] fileContent) {
    try {
      BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(fileContent));

      int width = bufferedImage.getWidth();
      int height = bufferedImage.getHeight();

      MediaType type = ImageService.parseMediaTypeFromFilename(fileName);

      Image img = new Image(FileController.directory_location.toString(), fileName, fileContent, type, width, height,
          "TODO");

      this.create(img);

    } catch (IOException e) {
      throw new RuntimeException("Failed to process image");
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
