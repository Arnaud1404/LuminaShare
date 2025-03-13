package pdl.backend.Database;

import pdl.backend.Image;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import com.pgvector.PGvector; 


@Repository
public class ImageRepository implements InitializingBean {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @PostConstruct
    public void afterPropertiesSet() throws Exception {

        // Create table
        this.jdbcTemplate
                .execute(
                        "CREATE TABLE IF NOT EXISTS imageDatabase (id bigserial PRIMARY KEY, name character varying(255), type character varying(10), size character varying(255),descripteur vector(2))");
    }
    public void addDatabase(Image img) {
        // Utiliser un tableau float[] au lieu de PGvector directement
        jdbcTemplate.update(
                "INSERT INTO imageDatabase (name, type, size, descripteur) VALUES (?, ?, ?, ?)",
                img.getName(),
                img.getType().toString(),
                img.getSize(),
                img.getDescriptor() != null ? new PGvector(img.getDescriptor().toArray()) : new PGvector(new float[]{0.0f, 0.0f})        
        );
    }

    public void deleteDatabase(Image img) {
        jdbcTemplate.update("DELETE FROM imageDatabase WHERE id = (?)", img.getId());
    }
   
}