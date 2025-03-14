package pdl.backend;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import com.pgvector.PGvector;

public class Image {
  private static Long count = Long.valueOf(1);
  private String path;
  private Long id;
  private String name;
  private MediaType type;
  private String size;
  private String description;
  private PGvector rgbcube;
  private PGvector hueSat;
  private float similarityScore;
  private byte[] data;

  public Image(String path, String name, byte[] data,
      MediaType type, long width, long height,
      String description) {
    id = count++;
    this.path = path;
    this.name = name;
    this.type = type;
    this.size = width + "*" + height;
    this.description = description;
    this.data = data;
  }

  public Image() {
  }

  public long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getPath() {
    return path;
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

  public String getDesciption() {
    return description;
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
