package pdl.backend;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import pdl.backend.Database.ImageRepository;
import pdl.backend.FileHandler.FileController;
import pdl.backend.Image;

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
  @Autowired
  private ImageRepository imageRepository;

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

  @PostConstruct
  public void syncWithDatabase() {
    images.clear();

    try {
      List<Image> dbImages = imageRepository.list();
      for (Image img : dbImages) {
        if (img.getId() != null) {
          images.put(img.getId(), img);
          System.out.println("Loaded from DB: Image ID " + img.getId() + ", name: " + img.getName());
        }
      }

      System.out.println("Synchronized " + images.size() + " images from database");
    } catch (Exception e) {
      System.err.println("Error synchronizing with database: " + e.getMessage());
    }
  }

  /**
   * Retri
   * eves a single image from memory by its id
   * 
   * @param id The unique id of the image to retrieve
   * @return Optional containing the image if found, else empty Optional
   */
  @Override
  public Optional<Image> retrieve(final long id) {
    Image image = images.get(id);

    if (image != null && image.getData() == null) {
      try {
        Path filePath = Paths.get(image.getPath(), image.getName());
        if (Files.exists(filePath)) {
          image.setData(Files.readAllBytes(filePath));
        }
      } catch (Exception e) {
        System.err.println("Failed to load image data: " + e.getMessage());
      }
    }

    return Optional.ofNullable(image);
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
    int result = imageRepository.addDatabase(img);

    if (result > 0 && img.getId() != null) {
      images.put(img.getId(), img);
      System.out.println("Created image with DB-assigned ID: " + img.getId());
    } else {
      System.err.println("Failed to create image in database");
    }
  }

  public void create(final Image img, MultipartFile file) {
    FileController.store(file);
    create(img);

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
    imageRepository.deleteDatabase(img.getId());
    FileController.remove_from_directory(img.getName());

  }
}
