package pdl.backend;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class Database implements InitializingBean {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static String[] names = new String[] { "Toto", "Titi", "Tata" };

    @Override
    public void afterPropertiesSet() throws Exception {
        // Drop table
        jdbcTemplate.execute("DROP TABLE IF EXISTS images");

        // Create table
        this.jdbcTemplate
                .execute("CREATE TABLE IF NOT EXISTS images" +
                        "id bigserial PRIMARY KEY," +
                        "name character varying(255))," +
                        "s");

        // Insert rows
        jdbcTemplate.update("INSERT INTO images (name) VALUES (?), (?), (?)", (Object[]) names);
    }

    public int getNbImages() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM images", Integer.class);
    }

    public String getImageName(long id) {
        return jdbcTemplate.queryForObject("SELECT name FROM images WHERE id = ?", String.class, id);
    }

}
