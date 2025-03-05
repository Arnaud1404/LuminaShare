package pdl.backend;

import java.util.Arrays;

public class Image {
  private static Long count = Long.valueOf(0);
  private Long id;
  private String name;
  private byte[] data;

  private int[][] histogramHS;
  private int[][][] histogramRGB;

  public Image(final String name, final byte[] data) {
    id = count++;
    this.name = name;
    this.data = data;
    //descripteur
    try {
      ImageDescriptor descriptor = new ImageDescriptor(data);
      this.histogramHS = descriptor.getHistogramHS();
      this.histogramRGB = descriptor.getHistogramRGB();

      System.out.println(" Image indexée : " + name);
      System.out.println(" Histogramme HS : " + Arrays.deepToString(histogramHS));
      System.out.println(" Histogramme RGB : " + Arrays.deepToString(histogramRGB));
    } catch (Exception e) {
      e.printStackTrace();
    }
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
  public int[][] getHistogramHS() {
    return histogramHS;
  }

  public int[][][] getHistogramRGB() {
    return histogramRGB;
  }
}
