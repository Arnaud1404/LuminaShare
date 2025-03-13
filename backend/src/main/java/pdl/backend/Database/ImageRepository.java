package pdl.backend.Database;

import pdl.backend.Image;
import pdl.backend.imageProcessing.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.pgvector.PGvector;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import jakarta.annotation.PostConstruct;

import java.awt.image.BufferedImage;

/**
 * Handles database operations for images with vector similarity search.
 *
 * IMPORTANT: Only manages database records. Doesn't handle physical files or
 * in-memory records.
 * Synchronization with physical files and memory should be done by
 * ImageController.
 */
@Repository
public class ImageRepository implements InitializingBean {

    @Value("${DATABASE_TABLE:imageDatabase}")
    private String databaseTable;
    @Value("${DATABASE_RESET:false}")
    private boolean resetDatabase;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    RowMapper<Image> rowMapper = (rs, rowNum) -> {
        Image img = new Image(); // doit pas incrémenter le compteur
        img.setId((long) rs.getInt("id"));
        img.setName(rs.getString("name"));
        img.setType(MediaType.valueOf(rs.getString("type")));
        img.setSize(rs.getString("size"));
        return img;
    };

    @Override
    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        if (resetDatabase) {
            this.jdbcTemplate.execute("DROP TABLE IF EXISTS " + databaseTable);
        }
        this.jdbcTemplate
                .execute(
                        "CREATE TABLE IF NOT EXISTS " + databaseTable
                                + " (id bigserial PRIMARY KEY, name character varying(255) UNIQUE, type character varying(10), size character varying(255), rgbcube vector(512), hueSat vector(101))");
    }

    /**
     * Adds multiple images to the database with their descriptors
     * 
     * @param images Array of images to add to the database
     * @return Number of images successfully added
     */
    public int addDatabase(Image[] images) {
        if (images == null || images.length == 0) {
            return 0;
        }
        int success = 0;

        for (Image img : images) {
            try {
                int result = addDatabase(img);
                success += result;
            } catch (Exception e) {
                System.err.println("Failed to add image " + img.getName() + " - " + e.getMessage());
            }
        }

        return success;
    }

    /**
     * Adds an image to the database with its descriptors (3D histogram and 2D)
     * 
     * @param image image to add to the database
     * @return 1 if the image was added, else 0
     */
    public int addDatabase(Image img) {
        try {
            if (imageExists(img.getName())) {
                return 0;
            }

            PGvector rgbcube = createRgbHistogramFromImage(img);
            PGvector hueSat = createHueSaturationHistogramFromImage(img);

            if (rgbcube == null || hueSat == null) {
                return 0;
            }
            return insertImageRecord(img, rgbcube, hueSat);
        } catch (Exception e) {
            System.err.println("Error adding image to database: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Returns the list of images from the database
     * 
     * @return A List<Image> with the images from the database
     */
    public List<Image> list() {
        String sql = "SELECT id, name, type, size FROM " + databaseTable;
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * Gets an image from the database by its unique id
     * 
     * @return The image from the database
     */
    public Image getById(long id) {
        try {
            String sql = "SELECT id, name, type, size, rgbcube, hueSat FROM " + databaseTable + " WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, Image.class, id);
        } catch (Exception e) {
            return null; // Non trouvé
        }
    }

    /**
     * Deletes an image from the database by its unique id
     * This function does not delete an image server-side
     * 
     * @param id The ID of the image to delete
     * @return Number of rows affected (1 if successful)
     */
    public int deleteDatabase(long id) {
        return jdbcTemplate.update("DELETE FROM " + databaseTable + " WHERE id = ?", id);
    }

    /**
     * Deletes an image from the database
     * This function does not delete an image server-side
     * 
     * @param img The Image
     * @return Number of rows affected (1 if successful)
     */
    public int deleteDatabase(Image img) {
        return jdbcTemplate.update("DELETE FROM " + databaseTable + " WHERE id = ?", img.getId());
    }

    /**
     * Gets the number of images in the database
     * 
     * @return The number of images in the database
     */
    public long getImageCount() {
        try {
            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + databaseTable, Long.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return -1; // Non trouvé
        }
    }

    /**
     * Creates a 3D RGB histogram vector from an image
     *
     * @param img The image object containing metadata and path information
     * @return PGvector containing the hue-saturation histogram, or null if
     *         processing fails
     */
    private PGvector createRgbHistogramFromImage(Image img) {
        try {
            BufferedImage input = UtilImageIO.loadImage(img.getPath() + "/" + img.getName());
            if (input == null) {
                System.err.println("Could not load image: " + img.getPath() + "/" + img.getName());
                return null;
            }

            Planar<GrayU8> image = ConvertBufferedImage.convertFromPlanar(input, null, true, GrayU8.class);
            return ImagePGVector.createRgb(image, 8);
        } catch (Exception e) {
            System.err.println("Failed to create histogram: " + e.getMessage());
            return null;
        }
    }

    /**
     * Creates a hue-saturation histogram vector from an image
     *
     * @param img The image object containing metadata and path information
     * @return PGvector containing the hue-saturation histogram, or null if
     *         processing fails
     */
    private PGvector createHueSaturationHistogramFromImage(Image img) {
        try {
            BufferedImage input = UtilImageIO.loadImage(img.getPath() + "/" + img.getName());
            if (input == null) {
                System.err.println("Could not load image: " + img.getPath() + "/" + img.getName());
                return null;
            }

            Planar<GrayU8> image = ConvertBufferedImage.convertFromPlanar(input, null, true, GrayU8.class);
            return ImagePGVector.createHueSaturation(image);
        } catch (Exception e) {
            System.err.println("Failed to create histogram: " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if an image with the given name already exists in the database
     *
     * @param imageName The name of the image to check in the database
     * @return true if an image with this name exists, false otherwise
     */
    public boolean imageExists(String imageName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + databaseTable + " WHERE name = ?",
                Integer.class, imageName);
        return count != null && count > 0;
    }

    /**
     * Inserts an image record into the database
     *
     * @param img     The Image object containing metadata (name, type, size) to
     *                insert
     * @param rgbcube The PGvector containing the RGB histogram data for
     *                similarity search
     * @param hueSat  The PGvector containing the Hue-Saturation histogram
     *                data for similarity search
     * @return 1 if insertion was successful, 0 if it failed
     */
    private int insertImageRecord(Image img, PGvector rgbcube, PGvector hueSat) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO " + databaseTable + " (name, type, size, rgbcube, hueSat) VALUES (?, ?, ?, ?, ?)",
                    img.getName(),
                    img.getType().toString(),
                    img.getSize(),
                    rgbcube,
                    hueSat);
            return 1;
        } catch (Exception e) {
            System.err.println("Database insertion failed: " + e.getMessage());
            return 0;
        }
    }
}