package pdl.backend.Image.Processing;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class Traitement {
    /**
     * Resizes a given BufferedImage to the specified target width and height.
     *
     * @param originalImage The original BufferedImage to be resized.
     * @param targetWidth The desired width of the resized image.
     * @param targetHeight The desired height of the resized image.
     * @return A new BufferedImage object with the specified dimensions.
     */
    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth,
            int targetHeight) {
        BufferedImage resizedImage =
                new BufferedImage(targetWidth, targetHeight, originalImage.getType());
        Graphics2D graphics = resizedImage.createGraphics();
        // Activer les options de rendu de haute qualité
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        return resizedImage;
    }

    /**
     * Inverts the colors of a given BufferedImage.
     *
     * @param originalImage The original BufferedImage to be inverted.
     * @return A new BufferedImage object with inverted colors.
     */
    public static BufferedImage invertColors(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        BufferedImage invertedImage = new BufferedImage(width, height, originalImage.getType());
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgba = originalImage.getRGB(x, y);
                // Extraire les composantes alpha, rouge, vert et bleu
                int alpha = (rgba >> 24) & 0xff;// canal alpha représente la transparence des pixel
                                                // pour les formats ARGB (png)
                int red = (rgba >> 16) & 0xff;
                int green = (rgba >> 8) & 0xff;
                int blue = rgba & 0xff;

                // Inverser les couleurs
                int invertedRed = 255 - red;
                int invertedGreen = 255 - green;
                int invertedBlue = 255 - blue;

                // Recomposer la couleur inversée
                int invertedRGBA;
                // Always preserve alpha channel for all image types
                invertedRGBA =
                        (alpha << 24) | (invertedRed << 16) | (invertedGreen << 8) | invertedBlue;
                // Appliquer la couleur inversée au pixel
                invertedImage.setRGB(x, y, invertedRGBA);
            }

        }

        return invertedImage;
    }

    /**
     * Creates a mirrored version of the given BufferedImage.
     *
     * @param originalImage The original BufferedImage to be mirrored.
     * @param horizontal True for horizontal mirroring (left-right), false for vertical mirroring
     *        (top-bottom).
     * @return A new BufferedImage object with the mirrored image.
     * @throws IllegalArgumentException If the image cannot be processed.
     */
    public static BufferedImage mirrorImage(BufferedImage originalImage, boolean horizontal) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // Crée une nouvelle image avec le même type que l'image originale
        BufferedImage mirroredImage = new BufferedImage(width, height, originalImage.getType());

        try {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int pixel = originalImage.getRGB(x, y);

                    if (horizontal) {
                        // Miroir horizontal : inverser gauche-droite
                        mirroredImage.setRGB(width - 1 - x, y, pixel);
                    } else {
                        // Miroir vertical : inverser haut-bas
                        mirroredImage.setRGB(x, height - 1 - y, pixel);
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "L'image ne peut pas être traitée : " + e.getMessage());
        }

        return mirroredImage;
    }

    /**
     * Rotates the given image by the specified angle.
     *
     * @param originalImage The original BufferedImage to be rotated.
     * @param angle The angle of rotation (must be 90, 180, or 270 degrees).
     * @return A new BufferedImage object with the rotated image.
     * @throws IllegalArgumentException If the angle is invalid or the image cannot be processed.
     */
    public static BufferedImage rotateImage(BufferedImage originalImage, int angle) {
        if (angle != 90 && angle != 180 && angle != 270) {
            throw new IllegalArgumentException(
                    "Angle invalide. Seuls 90°, 180° et 270° sont pris en charge.");
        }

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage rotatedImage;

        if (angle == 90 || angle == 270) {
            // Inverser largeur et hauteur pour 90° ou 270°
            rotatedImage = new BufferedImage(height, width, originalImage.getType());
        } else {
            // Conserver largeur et hauteur pour 180°
            rotatedImage = new BufferedImage(width, height, originalImage.getType());
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = originalImage.getRGB(x, y);

                if (angle == 90) {
                    rotatedImage.setRGB(height - 1 - y, x, pixel);
                } else if (angle == 180) {
                    rotatedImage.setRGB(width - 1 - x, height - 1 - y, pixel);
                } else if (angle == 270) {
                    rotatedImage.setRGB(y, width - 1 - x, pixel);
                }
            }
        }

        return rotatedImage;
    }
}
