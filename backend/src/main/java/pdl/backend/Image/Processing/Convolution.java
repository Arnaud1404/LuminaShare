package pdl.backend.Image.Processing;

import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayS16;
import boofcv.struct.image.GrayU8;

/**
 * Provides methods for applying convolution-based filters to grayscale images.
 * 
 * This class contains static utility methods for performing convolution operations
 * on grayscale images (GrayU8), such as mean filtering, generic convolution, and
 * gradient computation using various kernels (Sobel, Prewitt, Linear). All methods
 * modify an output image rather than the input, except where specified.
 */
public class Convolution {
  /**
   * Applies a mean filter to a grayscale image.
   * 
   * Computes the average grayscale value within a square window of the specified size
   * around each pixel, ignoring border pixels where the window doesn’t fully fit.
   * The result is stored in the output image.
   * 
   * @param input The input grayscale image (GrayU8)
   * @param output The output grayscale image to store the filtered result
   * @param size The size of the square filter window (must be odd, e.g., 3, 5)
   */

  public static void meanFilter(GrayU8 input, GrayU8 output, int size) {
    int moy = 0;

    for (int y = size / 2; y < input.height - (size / 2); y = y + 1) {
      for (int x = size / 2; x < input.width - (size / 2); x = x + 1) {

        for (int j = y - (size / 2); j <= y + (size / 2); j = j + 1) {
          for (int i = x - (size / 2); i <= x + (size / 2); i = i + 1) {
            moy = moy + input.get(i, j);
          }
        }

        output.set(x, y, moy / (size * size));
        moy = 0;
      }
    }
  }
  /**
   * Applies a convolution operation to a grayscale image using a custom kernel.
   * 
   * Performs a 2D convolution by sliding the kernel over the input image and computing
   * the weighted sum of pixel values. Results are stored in a signed 16-bit output image
   * (GrayS16) to handle negative values. Border pixels are skipped.
   * 
   * @param input The input grayscale image (GrayU8)
   * @param output The output signed 16-bit image (GrayS16) to store the result
   * @param kernel The 2D convolution kernel (e.g., 3x3 matrix)
   */
  public static void convolution(GrayU8 input, GrayS16 output, int[][] kernel) {
    int moy = 0;
    int rayon_x = kernel[0].length / 2;
    int rayon_y = kernel.length / 2;

    for (int y = rayon_x; y < input.height - rayon_x; y = y + 1) {
      for (int x = rayon_y; x < input.width - rayon_y; x = x + 1) {

        for (int j = -rayon_x; j <= rayon_x; j = j + 1) {
          for (int i = -rayon_y; i <= rayon_y; i = i + 1) {
            moy = moy + input.get(x + i, y + j) * kernel[i + rayon_y][j + rayon_x];
          }
        }

        output.set(x, y, moy);
        moy = 0;
      }
    }
  }
  /**
   * Computes the gradient magnitude of a grayscale image using two kernels.
   * 
   * Applies convolution with horizontal (kernelX) and vertical (kernelY) kernels to
   * detect edges, then calculates the gradient magnitude as the square root of the
   * sum of squared gradients. The result is clamped to [0, 255] and stored in the output image.
   * 
   * @param input The input grayscale image (GrayU8)
   * @param output The output grayscale image (GrayU8) to store the gradient magnitude
   * @param kernelX The horizontal gradient kernel (e.g., Sobel X)
   * @param kernelY The vertical gradient kernel (e.g., Sobel Y)
   */
  public static void gradientImage(GrayU8 input, GrayU8 output, int[][] kernelX, int[][] kernelY) {

    GrayS16 matrice_gradient_x = new GrayS16(input.width, input.height);
    GrayS16 matrice_gradient_y = new GrayS16(input.width, input.height);
    int norme_gradient = 0;
    int gradient_x = 0;
    int gradient_y = 0;

    convolution(input, matrice_gradient_y, kernelY);
    convolution(input, matrice_gradient_x, kernelX);

    for (int y = 0; y < input.height; y = y + 1) {
      for (int x = 0; x < input.width; x = x + 1) {
        gradient_x = matrice_gradient_x.get(x, y);
        gradient_y = matrice_gradient_y.get(x, y);
        norme_gradient = (int) Math.sqrt((gradient_x * gradient_x) + (gradient_y * gradient_y));
        norme_gradient = Math.min(255, norme_gradient);

        output.set(x, y, norme_gradient);
      }
    }
  }
  /**
   * Computes the gradient magnitude of a grayscale image using Sobel kernels.
   * 
   * Applies the Sobel operator to detect edges by calling gradientImage with predefined
   * Sobel kernels for horizontal and vertical gradients. The result is stored in the output image.
   * 
   * @param input The input grayscale image (GrayU8)
   * @param output The output grayscale image (GrayU8) to store the gradient magnitude
   */
  public static void gradientImageSobel(GrayU8 input, GrayU8 output) {
    int[][] kernelX = { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
    int[][] kernelY = { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };
    gradientImage(input, output, kernelX, kernelY);
  }
  /**
   * Computes the gradient magnitude of a grayscale image using Prewitt kernels.
   * 
   * Applies the Prewitt operator to detect edges by calling gradientImage with predefined
   * Prewitt kernels for horizontal and vertical gradients. The result is stored in the output image.
   * 
   * @param input The input grayscale image (GrayU8)
   * @param output The output grayscale image (GrayU8) to store the gradient magnitude
   */
  public static void gradientImagePrewitt(GrayU8 input, GrayU8 output) {
    int[][] kernelX = { { -1, 0, 1 }, { -1, 0, 1 }, { -1, 0, 1 } };
    int[][] kernelY = { { -1, -1, -1 }, { 0, 0, 0 }, { 1, 1, 1 } };
    gradientImage(input, output, kernelX, kernelY);
  }
  /**
   * Computes the gradient magnitude of a grayscale image using linear kernels.
   * 
   * Applies a simple linear gradient operator by calling gradientImage with predefined
   * 1D kernels for horizontal and vertical gradients. The result is stored in the output image.
   * 
   * @param input The input grayscale image (GrayU8)
   * @param output The output grayscale image (GrayU8) to store the gradient magnitude
   */
  public static void gradientImageLinearKernel(GrayU8 input, GrayU8 output) {
    int[][] kernelX = { { -1, 0, 1 } };
    int[][] kernelY = { { -1 }, { 0 }, { 1 } };
    gradientImage(input, output, kernelX, kernelY);
  }
  /**
   * Main method for testing convolution-based image processing operations.
   * 
   * Loads an input image from the command line, applies Sobel and Linear gradient filters,
   * measures execution time, and saves two output images. 
   * 
   * <p>Usage: java Convolution <input_image_path> <output_image_path_sobel> <output_image_path_linear></p>
   * 
   * @param args Command-line arguments: [0] input image path, [1] Sobel output path, [2] Linear output path
   */
  public static void main(final String[] args) {
    // load image
    if (args.length < 3) {
      System.out.println("missing input or output image filename");
      System.exit(-1);
    }
    final String inputPath = args[0];
    GrayU8 input = UtilImageIO.loadImage(inputPath, GrayU8.class);
    GrayU8 output = input.createSameShape();
    GrayU8 output2 = input.createSameShape();

    // processing
    long time = System.currentTimeMillis();
    // meanFilter(input, output, 32);
    gradientImageSobel(input, output);
    gradientImageLinearKernel(input, output2);

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
