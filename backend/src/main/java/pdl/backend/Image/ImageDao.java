package pdl.backend.Image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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


import pdl.backend.Image.ImageRepository;
import pdl.backend.Common.Dao;
import pdl.backend.FileHandler.FileController;

/**
 * Handles in-memory image collection.
 * 
 * IMPORTANT: Only manages in-memory image objects. Doesn't manage database records or filesystem.
 * Synchronization with database and physical files should be done by ImageController.
 */
@Repository
public class ImageDao implements Dao<Image> {

  private final Map<Long, Image> images = new HashMap<>();
  @Autowired
  private ImageRepository imageRepository;

  /**
   * Saves an image to the in-memory collection. Processes the raw file data to extract image
   * metadata.
   * 
   * @param fileName Name of the image file
   * @param fileContent Raw byte array of the image file
   * @throws RuntimeException if image processing fails
   */
  public Image saveImage(String fileName, byte[] fileContent) {
    try {
      BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(fileContent));

      int width = bufferedImage.getWidth();
      int height = bufferedImage.getHeight();

      MediaType type = ImageService.parseMediaTypeFromFilename(fileName);

      Image img = new Image(FileController.directory_location.toString(), fileName, fileContent,
          type, width, height);

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
    imageRepository.addDatabase(img);
  }

  public void create(final Image img, MultipartFile file) {
    images.put(img.getId(), img);
    FileController.store(file);
    imageRepository.addDatabase(img);

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

  /**
   * Gets the number of images in the in-memory collection
   *
   * @return The count of images in memory
   */
  public static long getImageCount() {
    return Image.getCount();
  }

  // Add these methods to the ImageDao class

  /**
   * Get all images belonging to a specific user
   * 
   * @param userid The user ID
   * @return List of images owned by the user
   */
  public List<Image> getByUserId(String userid) {
    return imageRepository.getByUserId(userid);
  }

  /**
   * Get only public images belonging to a specific user
   * 
   * @param userid The user ID
   * @return List of public images owned by the user
   */
  public List<Image> getPublicByUserId(String userid) {
    return imageRepository.getPublicByUserId(userid);
  }

  /**
   * Likes an image by incrementing its like count
   * 
   * @param imageId The ID of the image to like
   * @return The new like count, or -1 if operation failed
   */
  public int likeImage(long imageId) {
    int newLikeCount = imageRepository.likeImage(imageId);

    // Also update in-memory image if it exists
    Optional<Image> imgOpt = retrieve(imageId);
    if (imgOpt.isPresent() && newLikeCount >= 0) {
      Image img = imgOpt.get();
      img.setLikes(newLikeCount);
    }

    return newLikeCount;
  }

  /**
   * Unlikes an image by decrementing its like count
   * 
   * @param imageId The ID of the image to unlike
   * @return The new like count, or -1 if operation failed
   */
  public int unlikeImage(long imageId) {
    int newLikeCount = imageRepository.unlikeImage(imageId);

    // Also update in-memory image if it exists
    Optional<Image> imgOpt = retrieve(imageId);
    if (imgOpt.isPresent() && newLikeCount >= 0) {
      Image img = imgOpt.get();
      img.setLikes(newLikeCount);
    }

    return newLikeCount;
  }
}
