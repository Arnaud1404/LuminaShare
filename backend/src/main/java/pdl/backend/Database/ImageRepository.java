package pdl.backend.Database;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;

@Repository
public class ImageRepository implements InitializingBean {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static String images[] = new String[] { "Toto", "Tata", "Bob", "Hello" };

    @Override
    @PostConstruct
    public void afterPropertiesSet() throws Exception {

        jdbcTemplate.execute("DROP TABLE IF EXISTS images");

        // Create table
        this.jdbcTemplate
                .execute("CREATE TABLE IF NOT EXISTS images (id bigserial PRIMARY KEY, name character varying(255))");

        // Insert rows
        jdbcTemplate.update("INSERT INTO images (name) VALUES (?), (?), (?), (?)", (Object[]) images);
    }

    @SuppressWarnings("null")
    public int getNbImages() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM images", Integer.class);
    }

    public String getImageId(long id) {
        return jdbcTemplate.queryForObject("SELECT name FROM images WHERE id = ?", String.class, id);
    }

}
