package pdl.backend.Common;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Initiliazes the databases in the right order
 * Users table is created first and then the images table which references users
 */
@Component
public class DatabaseInitializer implements InitializingBean {

    @Value("${DATABASE_RESET:true}")
    private boolean resetDatabase;

    @Value("${DATABASE_TABLE:imageDatabase}")
    private String databaseTable;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Initializing database tables in correct order...");

        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        
        if (resetDatabase) {
            jdbcTemplate.execute("DROP TABLE IF EXISTS users CASCADE");
        }
        
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS users (" +
            "userid VARCHAR(50) PRIMARY KEY, " +
            "name VARCHAR(100) NOT NULL, " +
            "password VARCHAR(100) NOT NULL, " +
            "bio VARCHAR(500))"
        );
        
        System.out.println("Users table initialized");
        
        if (resetDatabase) {
            jdbcTemplate.execute("DROP TABLE IF EXISTS " + databaseTable);
        }
        
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS " + databaseTable + " (" +
            "id bigserial PRIMARY KEY, " +
            "name character varying(255) UNIQUE, " +
            "type character varying(10), " +
            "size character varying(255), " +
            "rgbcube vector(512), " +
            "hueSat vector(101), " +
            "userid VARCHAR(50) REFERENCES users(userid), " +
            "ispublic BOOLEAN DEFAULT false" +
            ")"
        );
        
        System.out.println("Image table initialized");
    }
}