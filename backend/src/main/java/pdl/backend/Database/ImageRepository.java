package pdl.backend.Database;

import pdl.backend.Image;
import pdl.backend.imageProcessing.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
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

@Repository
public class ImageRepository implements InitializingBean {

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
                        "CREATE TABLE IF NOT EXISTS databasearnaud (id bigserial PRIMARY KEY, name character varying(255), type character varying(10), size character varying(255), rgbcube vector(512))");
    }

    public void addDatabase(Image img) {
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
                "INSERT INTO databasearnaud (name, type, size, rgbcube ) VALUES (?, ?, ?, ?)",
                img.getName(),
                img.getType().toString(),
                img.getSize(),
                histo3Drgb);
    }

    public List<Image> list() {
        String sql = "SELECT id, name, type, size FROM databasearnaud";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public void deleteDatabase(long id) {
        jdbcTemplate.update("DELETE FROM databasearnaud WHERE id = (?)", id);
    }
}