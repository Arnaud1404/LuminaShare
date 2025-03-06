package pdl.backend;

import java.util.Arrays;

public class Image {
  private static Long count = Long.valueOf(0);
  private Long id;
  private String name;
  private byte[] data;

  private String histogram2D; // Ajout de l'attribut pour stocker l'histogramme 2D Teinte/Saturation
  private String histogram3D; // Ajout de l'attribut pour stocker l'histogramme 3D RGB

  public Image(String name, byte[] data, String histogram2D, String histogram3D) {
    this.id = count++;
    this.name = name;
    this.data = data;
    this.histogram2D = histogram2D; // Initialisation de l'histogramme 2D
    this.histogram3D = histogram3D; // Initialisation de l'histogramme 3D
    
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

  public void setName(final String name) {
    this.name = name;
  }

  public byte[] getData() {
    return data;
  }
  public String getHistogram2D() {
    return histogram2D; // Getter pour récupérer l'histogramme 2D
  }

  public void setHistogram2D(String histogram2D) {
    this.histogram2D = histogram2D; // Setter pour modifier l'histogramme 2D
  }

  public String getHistogram3D() {
    return histogram3D; // Getter pour récupérer l'histogramme 3D
  }

  public void setHistogram3D(String histogram3D) {
    this.histogram3D = histogram3D; // Setter pour modifier l'histogramme 3D
  }
}
