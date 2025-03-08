package pdl.backend.Database;

import pdl.backend.Image;

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

        // Create table
        this.jdbcTemplate
                .execute(
                        "CREATE TABLE IF NOT EXISTS images2 (id bigserial PRIMARY KEY, name character varying(255), type character varying(10), size character varying(255),descripteur vector(2))");
    }

}
