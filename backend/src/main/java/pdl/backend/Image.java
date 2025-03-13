package pdl.backend;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.pgvector.PGvector; 
/**
 * Represents an image entity with metadata and binary data.
 * 
 * This class encapsulates the properties of an image, including its identifier,
 * name, media type, dimensions, description, binary content, and descriptor for
 * similarity analysis. It uses a static counter to assign unique IDs to instances.
 */
public class Image {
  private static Long count = Long.valueOf(1);
  private Long id;
  private String name;
  private MediaType type;
  private String size;
  private String description;
  private byte[] data;
  private PGvector descriptor; // Vector descriptor for similarity comparisons

   /**
    * Constructs an Image instance with the specified properties.
    * 
    * Initializes the image with a unique ID (incremented from a static counter),
    * name, binary data, media type, dimensions, and description. The descriptor
    * is initially set to null and can be computed or set later.
    * 
    * @param name The name of the image file
    * @param data The binary content of the image
    * @param type The MIME type of the image (e.g., IMAGE_PNG, IMAGE_JPEG)
    * @param width The width of the image in pixels
    * @param height The height of the image in pixels
    * @param description A description or additional metadata for the image
    */
  public Image(final String name, final byte[] data,
      MediaType type, long width, long height,
      final String description) {
    id = count++;
    this.name = name;
    this.type = type;
    this.size = width + "*" + height;
    this.description = description;
    this.data = data;
    this.descriptor = null; // Initialisé à null, sera calculé plus tard
  }

  public long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public MediaType getType() {
    return type;
  }

  public String getDesciption() {
    return description;
  }

  public String getSize() {
    return size;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public byte[] getData() {
    return data;
  }
  public PGvector getDescriptor() { 
    return descriptor;
  }
  public void setDescriptor(PGvector descriptor) { 
    this.descriptor = descriptor; 
  }
}
