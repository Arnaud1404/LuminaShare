package pdl.backend.Database;

import pdl.backend.Image;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pdl.backend.Image;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;

@Repository
public class ImageRepository implements InitializingBean {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    RowMapper<Image> rowMapper = (rs, rowNum) -> {
        Image img = new Image(); // doit pas incrémenter le compteur
        img.setId((long) rs.getInt("id"));
        img.setName(rs.getString("name"));
        img.setType(rs.getString("type"));
        img.setSize(rs.getString("size"));
        return img;
    };

    @Override
    @PostConstruct
    public void afterPropertiesSet() throws Exception {

        // Create table
        this.jdbcTemplate
                .execute(
                        "CREATE TABLE IF NOT EXISTS imageDatabase (id bigserial PRIMARY KEY, name character varying(255), type character varying(10), size character varying(255),descripteur vector(2))");
    }

    public void addDatabase(Image img) {
        jdbcTemplate.update(
                "INSERT INTO imageDatabase (name, type, size) VALUES (?, ?, ?)",
                img.getName(),
                img.getType().toString(),
                img.getSize());
    }

    public List<Image> list() {
        String sql = "SELECT id, name, type, size";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public void deleteDatabase(Image img) {
        jdbcTemplate.update("DELETE FROM imageDatabase WHERE id = (?)", img.getId());
    }
}