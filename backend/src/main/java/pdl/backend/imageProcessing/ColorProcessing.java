package pdl.backend.imageProcessing;

import boofcv.alg.color.ColorHsv;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import java.awt.image.BufferedImage;

/**
 * Provides methods for processing and enhancing color images.
 * 
 * This class contains static utility methods to manipulate planar RGB images (Planar<GrayU8>),
 * including brightness adjustment, mean filtering, grayscale conversion, hue modification,
 * and histogram generation in HSV space. Methods either modify the input image in place
 * or produce an output image as specified.
 */
public class ColorProcessing {
 /**
  * Adjusts the brightness of a color image by a specified delta.
  * 
  * Adds the delta value to each RGB band of the image, clamping the result to the valid
  * range [0, 255]. Positive delta increases brightness, negative delta decreases it.
  * The input image is modified in place.
  * 
  * @param input The planar RGB image to adjust (3 bands: R, G, B)
  * @param delta The brightness adjustment value (can be negative or positive)
  */
  public static void modif_lum(Planar<GrayU8> input, int delta) {
    for (int band = 0; band < 3; band = band + 1) {
      for (int y = 0; y < input.height; ++y) {
        for (int x = 0; x < input.width; ++x) {
          int gl = input.getBand(band).get(x, y);
          gl = gl + delta;
          if (gl < 0)
            gl = 0;
          if (gl > 255) {
            gl = 255;
          }
          input.getBand(band).set(x, y, gl);
        }
      }
    }
  }
 /**
  * Applies a mean filter to a color image.
  * 
  * Computes the average value within a square window of the specified size around each
  * pixel for each RGB band, ignoring border pixels where the window doesn’t fully fit.
  * The result is stored in the output image.
  * 
  * @param input The input planar RGB image (3 bands: R, G, B)
  * @param output The output planar RGB image to store the filtered result
  * @param size The size of the square filter window (must be odd, e.g., 3, 5)
  */
  public static void meanFilter(Planar<GrayU8> input, Planar<GrayU8> output, int size) {
    int moy = 0;

    for (int band = 0; band < 3; band = band + 1) {

      for (int y = size / 2; y < input.height - (size / 2); y = y + 1) {
        for (int x = size / 2; x < input.width - (size / 2); x = x + 1) {

          for (int j = y - (size / 2); j <= y + (size / 2); j = j + 1) {
            for (int i = x - (size / 2); i <= x + (size / 2); i = i + 1) {
              moy = moy + input.getBand(band).get(i, j);
            }
          }

          output.getBand(band).set(x, y, moy / (size * size));
          moy = 0;
        }
      }
    }
  }
 /**
  * Converts a color image to grayscale.
  * 
  * Computes the grayscale value for each pixel using the weighted formula
  * (0.3*R + 0.59*G + 0.11*B) and sets all three bands of the output image to this value.
  * 
  * @param input The input planar RGB image (3 bands: R, G, B)
  * @param output The output planar RGB image with identical grayscale values in all bands
  */
  public static void griser(Planar<GrayU8> input, Planar<GrayU8> output) {
    int r = 0;
    int g = 0;
    int b = 0;
    int gris = 0;

    for (int y = 0; y < input.height; y = y + 1) {
      for (int x = 0; x < input.width; x = x + 1) {
        r = input.getBand(0).get(x, y);
        g = input.getBand(1).get(x, y);
        b = input.getBand(2).get(x, y);

        gris = (int) (0.3 * r + 0.59 * g + 0.11 * b);

        output.getBand(0).set(x, y, gris);
        output.getBand(1).set(x, y, gris);
        output.getBand(2).set(x, y, gris);

      }
    }
  }
  /**
   * Changes the hue of a color image to a specified value.
   * 
   * Converts each pixel from RGB to HSV, sets the hue to the specified value (in degrees),
   * and converts back to RGB. The input image is modified in place.
   * 
   * @param input The planar RGB image to modify (3 bands: R, G, B)
   * @param hue The new hue value in degrees (0-360)
   */
  public static void change_hue(Planar<GrayU8> input, int hue) {

    double[] hsv = new double[3];
    double[] rgb = new double[3];
    double r, g, b;

    for (int y = 0; y < input.height; y++) {
      for (int x = 0; x < input.width; x++) {
        r = input.getBand(0).get(x, y);
        g = input.getBand(1).get(x, y);
        b = input.getBand(2).get(x, y);

        ColorHsv.rgbToHsv(r, g, b, hsv);

        hsv[0] = hue * Math.PI / 180.0;

        ColorHsv.hsvToRgb(hsv[0], hsv[1], hsv[2], rgb);

        input.getBand(0).set(x, y, (int) (rgb[0]));
        input.getBand(1).set(x, y, (int) (rgb[1]));
        input.getBand(2).set(x, y, (int) (rgb[2]));
      }
    }
  }
  /**
   * Generates a hue histogram of a color image.
   * 
   * Computes the frequency of hue values (0-360 degrees) across the image, normalizes it
   * to a height of 300 pixels, and returns a grayscale image representing the histogram.
   * 
   * @param input The input planar RGB image (3 bands: R, G, B)
   * @return A GrayU8 image (360x300) representing the hue histogram
   */
  public static GrayU8 histogramme_hue(Planar<GrayU8> input) {
    int height = 300;
    int width = 360;
    int max = 0;
    int histogram[] = new int[360];
    GrayU8 histo = new GrayU8(width, height);
    double r, g, b;
    double[] hsv = new double[3];

    for (int y = 0; y < input.height; y++) {
      for (int x = 0; x < input.width; x++) {
        r = input.getBand(0).get(x, y);
        g = input.getBand(1).get(x, y);
        b = input.getBand(2).get(x, y);

        ColorHsv.rgbToHsv(r, g, b, hsv);
        histogram[(int) (hsv[0] * (180.0 / Math.PI))] += 1;
      }
    }

    for (int i = 0; i < 360; i = i + 1) {
      if (max < histogram[i])
        max = histogram[i];
    }

    for (int i = 0; i < 360; i = i + 1) {
      histogram[i] = (int) (histogram[i] * height / max);
    }

    for (int x = 0; x < 360; x = x + 1) {
      for (int y = 0; y < histogram[x]; y = y + 1) {
        for (int width_colonne = 0; width_colonne < 1; width_colonne = width_colonne + 1)
          histo.set(x + width_colonne, height - y - 1, 255);
      }
    }
    return histo;
  }
  /**
   * Generates a 2D hue-saturation histogram of a color image.
   * 
   * Computes a 2D histogram of hue (0-360 degrees) and saturation (0-100%) values,
   * mapping frequencies to grayscale intensities (0-255). Returns a 360x101 grayscale image.
   * 
   * @param input The input planar RGB image (3 bands: R, G, B)
   * @return A GrayU8 image (360x101) representing the 2D hue-saturation histogram
   */
  public static GrayU8 histo_2d_hue_saturation(Planar<GrayU8> input) {
    int height = 101;
    int width = 360;
    int col = 0;

    int histogram_2d[][] = new int[width][height];

    GrayU8 histo = new GrayU8(width, height);

    double r, g, b;
    double[] hsv = new double[3];

    for (int y = 0; y < input.height; y++) {
      for (int x = 0; x < input.width; x++) {
        r = input.getBand(0).get(x, y);
        g = input.getBand(1).get(x, y);
        b = input.getBand(2).get(x, y);

        ColorHsv.rgbToHsv(r, g, b, hsv);
        hsv[0] = (hsv[0] * (180.0 / Math.PI));
        hsv[1] = hsv[1] * 100;

        histogram_2d[(int) (hsv[0])][(int) (hsv[1])] += 1;

      }
    }

    // for (int y = 0; y < input.height; y++) {
    // for (int x = 0; x < input.width; x++) {
    // if (max_histo < histogram_2d[x][y])
    // max_histo = histogram_2d[x][y];
    // }
    // }

    // for (int y = 0; y < input.height; y++) {
    // for (int x = 0; x < input.width; x++) {
    // histogram_2d[x][y] =
    // }
    // }

    for (int x = 0; x < width; x = x + 1) {
      for (int y = 0; y < height; y = y + 1) {
        col = Math.min(255, histogram_2d[x][y]);
        histo.set(x, y, col);
      }
    }
    return histo;
  }
  /**
   * Main method for testing color image processing operations.
   * 
   * Loads an input RGB image from the command line, applies mean filtering with two
   * different window sizes (11 and 33), measures execution time, and saves two output images.
   * 
   * <p>Usage: java ColorProcessing <input_image_path> <output_image_path_11> <output_image_path_33></p>
   * 
   * @param args Command-line arguments: [0] input image path, [1] output path (size 11), [2] output path (size 33)
   */
  public static void main(String[] args) {
    // load image
    if (args.length < 3) {
      System.out.println("missing input or output image filename");
      System.exit(-1);
    }
    final String inputPath = args[0];

    // GrayU8 input2 = UtilImageIO.loadImage(inputPath, GrayU8.class);

    BufferedImage input = UtilImageIO.loadImage(inputPath);
    Planar<GrayU8> image = ConvertBufferedImage.convertFromPlanar(input, null, true, GrayU8.class);

    Planar<GrayU8> output = image.createSameShape();
    Planar<GrayU8> output2 = image.createSameShape();
    // Planar<GrayU8> hsv = image.createSameShape();
    // GrayU8 histo = histogramme_hue(image);
    // GrayU8 histo2d = histo_2d_hue_saturation(image);

    // processing
    long time = System.currentTimeMillis();
    output = image;
    // modif_lum(output, 50);
    meanFilter(image, output, 11);
    meanFilter(image, output2, 33);
    // griser(image, output);
    // ColorHsv.rgbToHsv(output, hsv);
    // System.out.println("h = " + hsv.getBand(0).get(0, 0));
    // change_hue(image, 270);
    // change_hue(image, 0);

    long end_time = System.currentTimeMillis();
    System.out.println("temps d'exc est de " + (end_time - time) + " ms");

    // save output image
    final String outputPath = args[1];
    final String outputPath2 = args[2];

    UtilImageIO.saveImage(output, outputPath);
    System.out.println("Image saved in: " + outputPath);

    UtilImageIO.saveImage(output2, outputPath2);
    System.out.println("Image saved in: " + outputPath2);

  }
}