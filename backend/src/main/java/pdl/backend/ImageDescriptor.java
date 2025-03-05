package pdl.backend;
//cette classe c'est pour calculer et stockeras les  deux types  d'histogrammes et 
//fournira une méthode pour générer ces descripteurs à partir d’une image.
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class ImageDescriptor {

    private int[][] histogramHS; // Histogramme 2D Teinte/Saturation
    private int[][][] histogramRGB; // Histogramme 3D RGB

    public ImageDescriptor(byte[] imageData) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
        this.histogramHS = computeHistogramHS(image);
        this.histogramRGB = computeHistogramRGB(image);
    }

    // Calcul de l'histogramme 2D Teinte/Saturation
    private int[][] computeHistogramHS(BufferedImage image) {
        int[][] hist = new int[256][256]; // 256 niveaux pour H et S
        Arrays.stream(hist).forEach(row -> Arrays.fill(row, 0));
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y));
                float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
                int h = (int) (hsb[0] * 255);
                int s = (int) (hsb[1] * 255);
                hist[h][s]++;
            }
        }
        return hist;
    }

    // Calcul de l'histogramme 3D RGB
    private int[][][] computeHistogramRGB(BufferedImage image) {
        int[][][] hist = new int[256][256][256]; // 256 niveaux pour R, G et B
        for (int[][] plane : hist) {
            for (int[] row : plane) {
                Arrays.fill(row, 0);
            }
        }
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y));
                hist[color.getRed()][color.getGreen()][color.getBlue()]++;
            }
        }
        return hist;
    }

    public int[][] getHistogramHS() {
        return histogramHS;
    }

    public int[][][] getHistogramRGB() {
        return histogramRGB;
    }
}