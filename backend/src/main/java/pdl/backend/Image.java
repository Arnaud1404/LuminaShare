package pdl.backend;

public class Image {
  private static Long count = Long.valueOf(1);
  private Long id;
  private String name;
  private String type;
  private String size;
  private String description;
  private byte[] data;

  public Image(final String name, final byte[] data, final String type, long width, long height,
      final String description) {
    id = count++;
    this.name = name;
    this.type = type;
    this.size = width + " x " + height + " pixels";
    this.description = description;
    this.data = data;
  }

  public Image() {
    count--;
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
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
}
