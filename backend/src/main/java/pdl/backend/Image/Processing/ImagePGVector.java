package pdl.backend.Image.Processing;

import com.pgvector.PGvector;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;

/**
 * This class converts images to PGVector objects for database storage and
 * similarity search.
 * 
 * PGVector requires float arrays because the pgvector PostgreSQL extension
 * expects floating point vectors for similarity search
 */
public class ImagePGVector {
    /**
     * Creates a Hue-Saturation histogram from an image
     * 
     * @param input RGB image of type Planar<GrayU8>
     * @return PGvector representing the sum of each line (101 lines in total)
     */
    public static PGvector createHueSaturation(Planar<GrayU8> input) {
        GrayU8 image = ColorProcessing.histo_2d_hue_saturation(input);
        float[] vector = new float[image.height];

        int i = 0;
        for (int y = 0; y < image.height; y++) {
            int rowSum = 0;
            for (int x = 0; x < image.width; x++) {
                rowSum += image.get(x, y);
            }
            vector[i++] = (float) rowSum / image.height;
        }
        return new PGvector(vector);
    }

    /**
     * Creates an RGB color histogram from an image
     * 
     * @param input RGB image of type Planar<GrayU8>
     * @param bins  Number of bins for each color band (e.g. 4x4x4 = 64 bins in
     *              total)
     * @return PGvector representing the normalized RGB histogram
     */
    public static PGvector createRgb(Planar<GrayU8> input, int bins) {
        int[][][] colorCube = new int[bins][bins][bins];
        int binSize = 256 / bins;

        for (int y = 0; y < input.height; y++) {
            for (int x = 0; x < input.width; x++) {
                int rBin = Math.min(input.getBand(0).get(x, y) / binSize, bins - 1);
                int gBin = Math.min(input.getBand(1).get(x, y) / binSize, bins - 1);
                int bBin = Math.min(input.getBand(2).get(x, y) / binSize, bins - 1);

                colorCube[rBin][gBin][bBin]++;
            }
        }

        // cube to 1d array
        float[] vector = new float[bins * bins * bins];
        int totalPixels = input.width * input.height;

        int index = 0;
        for (int r = 0; r < bins; r++) {
            for (int g = 0; g < bins; g++) {
                for (int b = 0; b < bins; b++) {
                    vector[index++] = (float) colorCube[r][g][b] / totalPixels;
                }
            }
        }

        return new PGvector(vector);
    }
}
