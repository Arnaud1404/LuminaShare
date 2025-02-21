package pdl.imageprocessing;

import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayS16;
import boofcv.struct.image.GrayU8;

public class Convolution {

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

  public static void gradientImageSobel(GrayU8 input, GrayU8 output) {
    int[][] kernelX = { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
    int[][] kernelY = { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };
    gradientImage(input, output, kernelX, kernelY);
  }

  public static void gradientImagePrewitt(GrayU8 input, GrayU8 output) {
    int[][] kernelX = { { -1, 0, 1 }, { -1, 0, 1 }, { -1, 0, 1 } };
    int[][] kernelY = { { -1, -1, -1 }, { 0, 0, 0 }, { 1, 1, 1 } };
    gradientImage(input, output, kernelX, kernelY);
  }

  public static void gradientImageLinearKernel(GrayU8 input, GrayU8 output) {
    int[][] kernelX = { { -1, 0, 1 } };
    int[][] kernelY = { { -1 }, { 0 }, { 1 } };
    gradientImage(input, output, kernelX, kernelY);
  }

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
