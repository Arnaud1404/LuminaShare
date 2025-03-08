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
}
