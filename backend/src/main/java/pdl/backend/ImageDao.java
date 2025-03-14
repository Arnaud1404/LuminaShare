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

/**
 * Handles in-memory image collection.
 * 
 * IMPORTANT: Only manages in-memory image objects. Doesn't manage database
 * records or filesystem. Synchronization with database and physical files
 * should be done by ImageController.
 */
@Repository
public class ImageDao implements Dao<Image> {

  private final Map<Long, Image> images = new HashMap<>();

  /**
   * Saves an image to the in-memory collection.
   * Processes the raw file data to extract image metadata.
   * 
   * @param fileName    Name of the image file
   * @param fileContent Raw byte array of the image file
   * @throws RuntimeException if image processing fails
   */
  public Image saveImage(String fileName, byte[] fileContent) {
    try {
      BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(fileContent));

      int width = bufferedImage.getWidth();
      int height = bufferedImage.getHeight();

      MediaType type = ImageService.parseMediaTypeFromFilename(fileName);

      Image img = new Image(FileController.directory_location.toString(), fileName, fileContent, type, width, height);

      this.create(img);
      return img;

    } catch (IOException e) {
      throw new RuntimeException("Failed to process image");
    }
  }

  /**
   * Retrieves a single image from memory by its id
   * 
   * @param id The unique id of the image to retrieve
   * @return Optional containing the image if found, else empty Optional
   */
  @Override
  public Optional<Image> retrieve(final long id) {
    Optional<Image> img = Optional.ofNullable(images.get(id));
    return img;
  }

  /**
   * Retrieves all images from the in-memory collection
   * 
   * @return List of all images in memory
   */
  @Override
  public List<Image> retrieveAll() {
    return new ArrayList<Image>(images.values());
  }

  /**
   * Adds a new image to the in-memory collection
   * 
   * @param img The image to add to the collection
   */
  @Override
  public void create(final Image img) {
    images.put(img.getId(), img);
  }

  /**
   * Deletes an image from the in-memory collection
   * 
   * @param img The image to remove
   */
  @Override
  public void update(final Image img, final String[] params) {
    // Not used
  }

  /**
   * Deletes an image from the in-memory collection
   * 
   * @param img The image to remove
   */
  @Override
  public void delete(final Image img) {
    images.remove(img.getId());
  }

  /**
   * Gets the number of images in the in-memory collection
   * 
   * @return The count of images in memory
   */
  public int getImageCount() {
    return images.size();
  }
}
