package pdl.imageprocessing;

import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayU8;

public class GrayLevelProcessing {

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

	public static void egalisation_histo(GrayU8 input) {
		int histogram[] = histogram(input);
		int histogram_egalise[] = new int[256];
		int LUT[] = new int[256];
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