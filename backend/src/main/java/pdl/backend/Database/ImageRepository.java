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
                        "CREATE TABLE IF NOT EXISTS imageDatabase (id bigserial PRIMARY KEY, name character varying(255), type character varying(10), size character varying(255),descripteur vector(2) published_on timestamp not null)");
    }

    public void addDatabase(Image img) {
        // BufferedImage input = UtilImageIO.loadImage(img.getPath() + "/" +
        // img.getName());
        // GrayU8 img_final = new GrayU8();
        // PGvector vector_img;

        // ConvertBufferedImage.convertFrom(input, img_final);
        // vector_img = ImageVectorConversion.convertGrayU8ToVector(img_final);
        // Object[] vector = new Object[] { vector_img };

        jdbcTemplate.update(
                "INSERT INTO imageDatabase (name, type, size ) VALUES (?, ?, ?)",
                img.getName(),
                img.getType().toString(),
                img.getSize());
    }

    public List<Image> list() {
        String sql = "SELECT id, name, type, size";
        return jdbcTemplate.query(sql, rowMapper);
    }

    // public Optional<Image> GetImage(String name) {
    // String sql = "SELECT name, type, size FROM imageDatabase WHERE name = ?";
    // Image img = new Image();
    // try {
    // jdbcTemplate.queryForObject(sql, rowMapper, name);
    // img = (Image) rowMapper;

    // } catch (DataAccessException ex) {
    // return Optional.ofNullable(img);
    // }

    // return Optional.ofNullable(img);
    // }

    public void deleteDatabase(Image img) {
        jdbcTemplate.update("DELETE FROM imageDatabase WHERE id = (?)", img.getId());
    }
}