package pdl.backend.Database;

import pdl.backend.Image;
import pdl.backend.imageProcessing.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbcTemplate.execute("DROP TABLE IF EXISTS imageDatabase2");

        jdbcTemplate.execute(
                "CREATE TABLE imageDatabase2 (id bigserial PRIMARY KEY, name character varying(255),type character varying(255),size character varying(255), embedding vector(3))");

    }

    public void addDatabase(Image img) {
        BufferedImage input = UtilImageIO.loadImage(img.getPath() + "/" + img.getName());
        // GrayU8 img_final = new GrayU8();
        // PGvector vector_img;

        Planar<GrayU8> image = ConvertBufferedImage.convertFromPlanar(input, null, true, GrayU8.class);
        PGvector histo3Drgb = ImagePGVector.createRgbHistogram(image, 8);

        // GrayU8 img_final = new GrayU8();
        // PGvector hueSaturation;
        // ConvertBufferedImage.convertFrom(input, img_final);
        // hueSaturation = ImagePGVector.convertGrayU8ToVector(img_final);
        // Object[] vector = new Object[] { hueSaturation };

        Object[] insertParams = new Object[] {
                new PGvector(new float[] { 1, 1, 1 }),
                new PGvector(new float[] { 2, 2, 2 }),
                new PGvector(new float[] { 1, 1, 2 }),
                null
        };

        jdbcTemplate.update("INSERT INTO imageDatabase2 (name,type,size,embedding) VALUES (?), (?), (?), (?))", "test",
                "png", "251x532",
                insertParams[0]);

    }

    public List<Image> list() {
        String sql = "SELECT id, name, type, size FROM databasearnaud";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Image> GetImage(String name) {
        String sql = "SELECT name, type, size FROM imageDatabase WHERE name = ?";
        Image img = new Image();
        try {
            jdbcTemplate.queryForObject(sql, rowMapper, name);
            img = (Image) rowMapper;

        } catch (DataAccessException ex) {
            return Optional.ofNullable(img);
        }

        return Optional.ofNullable(img);
    }

    public void deleteDatabase(Image img) {
        jdbcTemplate.update("DELETE FROM databasearnaud WHERE id = (?)", img.getId());
    }
}