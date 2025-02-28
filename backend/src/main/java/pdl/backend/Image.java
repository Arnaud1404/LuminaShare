package pdl.backend;

public class Image {
  private static Long count = Long.valueOf(0);
  private Long id;
  private String name;
  private String type;
  private double length;
  private String description;
  private byte[] data;

  public Image(final String name, final byte[] data, final String type, double length,
      final String desciption) {
    id = count++;
    this.name = name;
    this.type = type;
    this.length = length;
    this.description = desciption;
    this.data = data;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getDesciption() {
    return description;
  }

  public double getLength() {
    return length;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public byte[] getData() {
    return data;
  }
}
