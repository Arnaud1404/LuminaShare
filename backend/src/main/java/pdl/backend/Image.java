package pdl.backend;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import com.pgvector.PGvector;

/**
 * Represents an image with its metadata and binary data.
 * This class allows storing and managing image information,
 * including its path, name, media type, and dimensions.
 */
public class Image {
  private String path;
  private Long id;
  private String name;
  private MediaType type;
  private String size;
  private PGvector rgbcube;
  private PGvector hueSat;
  private float similarityScore;
  private byte[] data;

  /**
   * Constructs a new Image instance with the specified parameters.
   * The identifier is automatically generated from the static counter.
   *
   * @param path        the path of the image in the file system
   * @param name        the name of the image
   * @param data        the binary data of the image
   * @param type        the media type of the image
   * @param width       the width of the image in pixels
   * @param height      the height of the image in pixels
   * @param description the description of the image
   */
  public Image(String path, String name, byte[] data,
      MediaType type, long width, long height) {
    this.path = path;
    this.name = name;
    this.type = type;
    this.size = width + "*" + height;
    this.data = data;
  }

  public Image() {
  }

  public Long getId() {
    return id;
  }

  public long getIdAsLong() {
    return id != null ? id : 0L;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getName() {
    return name;
  }

  public MediaType getType() {
    return type;
  }

  public void setType(MediaType type) {
    this.type = type;
  }

  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public byte[] getData() {
    return data;
  }

  public void setHueSat(PGvector huesat) {
    this.hueSat = huesat;
  }

  public PGvector getHueSat() {
    return this.hueSat;
  }

  public void setRgbCube(PGvector rgbcube) {
    this.rgbcube = rgbcube;
  }

  public PGvector getRgbCube() {
    return this.rgbcube;
  }

  public void setSimilarityScore(float similarityScore) {
    this.similarityScore = similarityScore;
  }

  public float getSimilarityScore() {
    return this.similarityScore;
  }
}