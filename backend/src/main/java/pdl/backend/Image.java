package pdl.backend;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

public class Image {
  private static Long count = Long.valueOf(1);
  private Long id;
  private String name;
  private MediaType type;
  private String size;
  private String description;
  private byte[] data;

  public Image(final String name, final byte[] data,
      MediaType type, long width, long height,
      final String description) {
    id = count++;
    this.name = name;
    this.type = type;
    this.size = width + "*" + height;
    this.description = description;
    this.data = data;
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
}
