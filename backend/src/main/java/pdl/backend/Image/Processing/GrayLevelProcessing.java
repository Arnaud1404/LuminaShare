package pdl.backend.Image.Processing;

import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayU8;

/**
 * Provides methods for grayscale image processing and enhancement.
 * 
 * This class contains static utility methods to manipulate grayscale images
 * (GrayU8),
 * including thresholding, brightness adjustment, dynamic range stretching, and
 * histogram
 * equalization. It operates directly on the input image data, modifying it in
 * place.
 */
public class GrayLevelProcessing {
	/**
	 * Applies a binary threshold to a grayscale image.
	 * 
	 * Pixels with grayscale values below the threshold are set to 0 (black), and
	 * those
	 * above or equal to the threshold are set to 255 (white). The input image is
	 * modified
	 * in place.
	 * 
	 * @param input The grayscale image to threshold
	 * @param t     The threshold value (0-255)
	 */
	public static void threshold(GrayU8 input, int t) {
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				int gl = input.get(x, y);
				if (gl < t) {
					gl = 0;
				} else {
					gl = 255;
				}
				input.set(x, y, gl);
			}
		}
	}

	/**
	 * Adjusts the brightness of a grayscale image by a specified delta.
	 * 
	 * Adds the delta value to each pixel’s grayscale level, clamping the result to
	 * the
	 * valid range [0, 255]. Positive delta increases brightness, negative delta
	 * decreases it.
	 * The input image is modified in place.
	 * 
	 * @param input The grayscale image to adjust
	 * @param delta The brightness adjustment value (can be negative or positive)
	 */
	public static void modif_lum(GrayU8 input, int delta) {
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				int gl = input.get(x, y);
				gl = gl + delta;
				if (gl < 0)
					gl = 0;
				if (gl > 255) {
					gl = 255;
				}
				input.set(x, y, gl);
			}
		}
	}

	/**
	 * Stretches the dynamic range of a grayscale image using a lookup table (LUT).
	 * 
	 * Computes the minimum and maximum grayscale values in the image, then maps all
	 * pixel
	 * values to the full range [0, 255] using a LUT for efficiency. The input image
	 * is
	 * modified in place.
	 */
	public static void etend_dynamic(GrayU8 input) {
		int LUT[] = new int[256];

		int min = 256;
		int max = 0;
		int gl = 0;
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				gl = input.get(x, y);
				if (gl < min)
					min = gl;
				if (gl > max)
					max = gl;
			}
		}

		for (int i = 0; i < 256; i = i + 1) {
			LUT[i] = 255 * (i - min) / (max - min);
		}

		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				gl = input.get(x, y);
				gl = LUT[gl];
				input.set(x, y, gl);
			}
		}

	}

	/**
	 * Stretches the dynamic range of a grayscale image without optimization.
	 * 
	 * Similar to etend_dynamic, but recalculates the mapping for each pixel without
	 * using
	 * a LUT, making it less efficient. The input image is modified in place to span
	 * the
	 * full range [0, 255] based on its minimum and maximum grayscale values.
	 */
	public static void etend_dynamic_pas_opt(GrayU8 input) {
		int min = 256;
		int max = 0;
		int gl = 0;

		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				gl = input.get(x, y);
				if (gl < min)
					min = gl;
				if (gl > max)
					max = gl;
			}
		}

		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				gl = input.get(x, y);
				gl = 255 * (gl - min) / (max - min);
				input.set(x, y, gl);
			}
		}

	}

	/**
	 * Computes the histogram of grayscale levels in an image.
	 * 
	 * Returns an array where each index (0-255) represents a grayscale level, and
	 * the
	 * value at that index is the number of pixels with that level.
	 * 
	 * @param input The grayscale image to analyze
	 * @return An array of size 256 containing the histogram of grayscale levels
	 */
	public static int[] histogram(GrayU8 input) {
		int histogram[] = new int[256];
		int gl = 0;
		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				gl = input.get(x, y);
				histogram[gl] = histogram[gl] + 1;
			}
		}
		return histogram;
	}

	/**
	 * Applies histogram equalization to a grayscale image.
	 * 
	 * Enhances contrast by redistributing grayscale levels based on the cumulative
	 * histogram. The input image is modified in place to achieve a more uniform
	 * distribution of intensities.
	 */
	public static void egalisation_histo(GrayU8 input) {
		int histogram[] = histogram(input);
		int histogram_egalise[] = new int[256];
		int nb_pixel = 0;
		int nb_gl = 0;
		int min = 256;
		int max = 0;
		int gl;

		for (int i = 0; i < 256; i = i + 1) {
			if (min == 0 && histogram[i] != 0)
				min = histogram[i];
			nb_gl = histogram[i];
			nb_pixel = nb_pixel + nb_gl;
			histogram_egalise[i] = nb_pixel;
		}

		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				gl = input.get(x, y);
				if (gl < min)
					min = gl;
				if (gl > max)
					max = gl;
			}
		}

		for (int y = 0; y < input.height; ++y) {
			for (int x = 0; x < input.width; ++x) {
				gl = input.get(x, y);
				gl = histogram_egalise[gl] * 255 / nb_pixel;
				input.set(x, y, gl);
			}
		}

	}

	/**
	 * Main method for testing grayscale image processing operations.
	 * 
	 * Loads an input image from the command line, applies a selected processing
	 * method
	 * (currently set to etend_dynamic_pas_opt), and saves the result to an output
	 * file.
	 * 
	 * <p>
	 * Usage: java GrayLevelProcessing <input_image_path> <output_image_path>
	 * </p>
	 * 
	 * @param args Command-line arguments: [0] input image path, [1] output image
	 *             path
	 */
	public static void main(String[] args) {

		// load image
		if (args.length < 2) {
			System.out.println("missing input or output image filename");
			System.exit(-1);
		}
		final String inputPath = args[0];
		GrayU8 input = UtilImageIO.loadImage(inputPath, GrayU8.class);
		if (input == null) {
			System.err.println("Cannot read input file '" + inputPath);
			System.exit(-1);
		}

		// processing

		// threshold(input, 128);
		// modif_lum(input,-50);
		etend_dynamic_pas_opt(input);
		// egalisation_histo(input);

		// save output image
		final String outputPath = args[1];
		UtilImageIO.saveImage(input, outputPath);
		System.out.println("Image saved in: " + outputPath);
	}

}