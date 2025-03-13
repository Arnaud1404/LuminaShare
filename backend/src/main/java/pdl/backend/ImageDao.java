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

import boofcv.io.image.ConvertBufferedImage; // Ajouté pour conversion
import boofcv.struct.image.GrayU8; // Ajouté pour GrayU8
import pdl.backend.imageProcessing.ImageVectorConversion; // Ajouté pour calcul du vecteur
import com.pgvector.PGvector; // Ajouté

@Repository
public class ImageDao implements Dao<Image> {

  private final Map<Long, Image> images = new HashMap<>();

  public void saveImage(String fileName, byte[] fileContent) {
    BufferedImage bufferedImage = null;
    try {
        bufferedImage = ImageIO.read(new ByteArrayInputStream(fileContent));
        if (bufferedImage == null) {
            throw new RuntimeException("Failed to read image: " + fileName);
        }

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        MediaType type = ImageService.parseMediaTypeFromFilename(fileName);

        GrayU8 grayImage = ConvertBufferedImage.convertFrom(bufferedImage, (GrayU8) null);
        PGvector descriptor = ImageVectorConversion.convertGrayU8ToVector(grayImage);

        Image img = new Image(fileName, fileContent, type, width, height, "TODO");
        img.setDescriptor(descriptor);
        this.create(img);
    } catch (IOException e) {
        throw new RuntimeException("Failed to process image: " + fileName, e);
    } finally {
        if (bufferedImage != null) {
            bufferedImage.flush(); // Libère la mémoire explicitement
        }
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
