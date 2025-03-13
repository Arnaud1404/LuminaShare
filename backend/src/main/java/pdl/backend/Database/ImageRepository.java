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
 * Repository for image storage and similarity search using pgvector.
 * 
 * IMPORTANT: This repository only manages database records, not physical image
 * files.
 * The application must manually maintain synchronization between:
 * 1. Images stored in the filesystem
 * 2. Corresponding entries in this database
 * 
 * This class uses pgvector to store histogram file descriptors for efficient
 * similarity search.
 */
@Repository
public class ImageRepository implements InitializingBean {

    @Value("${DATABASE_TABLE:imageDatabase}")
    private String databaseTable;

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

        // Create table
        this.jdbcTemplate
                .execute(
                        "CREATE TABLE IF NOT EXISTS " + databaseTable
                                + " (id bigserial PRIMARY KEY, name character varying(255), type character varying(10), size character varying(255), rgbcube vector(512))");
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
     * Adds one image to the database with their descriptors
     * 
     * @param image image to add to the database
     * @return 1 if the image was added, else 0
     */

    public int addDatabase(Image img) {
        try {
            BufferedImage input = UtilImageIO.loadImage(img.getPath() + "/" +
                    img.getName());

            Planar<GrayU8> image = ConvertBufferedImage.convertFromPlanar(input, null, true, GrayU8.class);
            PGvector histo3Drgb = ImagePGVector.createRgbHistogram(image, 8);

            // GrayU8 img_final = new GrayU8();
            // PGvector hueSaturation;
            // ConvertBufferedImage.convertFrom(input, img_final);
            // hueSaturation = ImagePGVector.convertGrayU8ToVector(img_final);
            // Object[] vector = new Object[] { hueSaturation };

            jdbcTemplate.update(
                    "INSERT INTO " + databaseTable + " (name, type, size, rgbcube) VALUES (?, ?, ?, ?)",
                    img.getName(),
                    img.getType().toString(),
                    img.getSize(),
                    histo3Drgb);
            return 1;
        } catch (Exception e) {
            System.err.println("Failed to add image " + img.getName() + " - " + e.getMessage());
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
            String sql = "SELECT id, name, type, size, rgbcube FROM " + databaseTable + " WHERE id = ?";
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
}