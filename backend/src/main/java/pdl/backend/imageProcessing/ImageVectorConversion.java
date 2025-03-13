package pdl.backend.imageProcessing;

import com.pgvector.PGvector;
import boofcv.alg.color.ColorHsv;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import java.awt.image.BufferedImage;

public class ImageVectorConversion {
    public static PGvector convertGrayU8ToVector(GrayU8 image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int sampleSize = 100; // Réduire la résolution (1 valeur tous les 10 pixels)
        int reducedWidth = Math.max(1, width / sampleSize);
        int reducedHeight = Math.max(1, height / sampleSize);
        float[] vector = new float[reducedWidth * reducedHeight];
        int index = 0;

        for (int y = 0; y < height && index < vector.length; y += sampleSize) {
            for (int x = 0; x < width && index < vector.length; x += sampleSize) {
                vector[index++] = image.get(x, y);
            }
        }
      return new PGvector(vector);
    }
}
