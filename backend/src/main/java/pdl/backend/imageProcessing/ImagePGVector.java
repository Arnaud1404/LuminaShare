package pdl.backend.imageProcessing;

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
     * Converts a GrayU8 image to a PGVector
     * 
     * @param input GrayU8 image
     * @return PGvector representing the input image
     */
    public static PGvector convertGrayU8ToVector(GrayU8 image) {
        float[] vector = new float[image.width * image.height];
        int i = 0;
        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                vector[i] = image.get(x, y) / 255.0f;
                i++;
            }
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
    public static PGvector createRgbHistogram(Planar<GrayU8> input, int bins) {
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
