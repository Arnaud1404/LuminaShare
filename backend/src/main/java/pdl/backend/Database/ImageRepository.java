package pdl.backend.Database;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ImageRepository implements InitializingBean {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static String names[] = new String[] { "Toto", "Titi", "Tata", "Bob" };

    @Override
    public void afterPropertiesSet() throws Exception {
        // Drop table
        jdbcTemplate.execute("DROP TABLE IF EXISTS test");

        // Create table
        this.jdbcTemplate
                .execute("CREATE TABLE IF NOT EXISTS test (id bigserial PRIMARY KEY, name character varying(255))");

        // Insert rows
        jdbcTemplate.update("INSERT INTO test (name) VALUES (?), (?), (?), (?)", (Object[]) names);
    }

    public int getNbEmployees() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test", Integer.class);
    }

    public String getEmployeeName(long id) {
        return jdbcTemplate.queryForObject("SELECT name FROM test WHERE id = ?", String.class, id);
    }

}
