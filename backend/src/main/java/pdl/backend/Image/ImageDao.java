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

import pdl.backend.Common.Dao;
import pdl.backend.FileHandler.FileController;

/**
 * Handles in-memory image collection.
 * 
 * IMPORTANT: Only manages in-memory image objects. Doesn't manage database
 * records or filesystem.
 * Synchronization with database and physical files should be done by
 * ImageController.
 */
@Repository
public class ImageDao implements Dao<Image> {

  private final Map<Long, Image> images = new HashMap<>();

  @Autowired
  private ImageRepository imageRepository;

  /**
   * Saves an image to the in-memory collection. Processes the raw file data to
   * extract image
   * metadata.
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
   * Checks if a user has liked an image
   * 
   * @param imageId The ID of the image
   * @param userid  The ID of the user
   * @return true if the user has liked the image, false otherwise
   */
  public boolean hasUserLikedImage(long imageId, String userid) {
    return imageRepository.hasUserLikedImage(userid, imageId);
  }

  /**
   * Updates the privacy status of an image
   * 
   * @param imageId  The ID of the image to update
   * @param isPublic The new privacy status
   * @return true if update was successful, false otherwise
   */
  public boolean updatePrivacy(long imageId, boolean isPublic) {
    boolean dbUpdateSuccess = imageRepository.updateImagePrivacy(imageId, isPublic);

    Optional<Image> imgOpt = retrieve(imageId);
    if (imgOpt.isPresent() && dbUpdateSuccess) {
      Image img = imgOpt.get();
      img.setPublic(isPublic);
      return true;
    }

    return dbUpdateSuccess;
  }

  /**
   * Updates the like count for an image (for testing purposes)
   * 
   * @param imageId The ID of the image
   * @param likes   The new number of likes
   * @return true if update was successful, false otherwise
   */
  public boolean updateLikeCount(long imageId, int likes) {
    boolean success = imageRepository.updateLikeCount(imageId, likes);

    if (success) {
      Optional<Image> imgOpt = retrieve(imageId);
      if (imgOpt.isPresent()) {
        Image img = imgOpt.get();
        img.setLikes(likes);
      }
    }

    return success;
  }

  /**
   * Toggles a user's like on an image
   * 
   * @param id     The ID of the image
   * @param userid The ID of the user
   * @return true if the image is now liked, false if unliked
   */
  public boolean toggleLike(long id, String userid) {
    boolean isLiked = imageRepository.toggleLike(userid, id);

    Optional<Image> imgOpt = retrieve(id);
    if (imgOpt.isPresent()) {
      Image img = imgOpt.get();
      int currentLikes = imageRepository.getLikeCount(id);
      img.setLikes(currentLikes);
    }

    return isLiked;
  }
}
