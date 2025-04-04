package pdl.backend.Database;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import pdl.backend.User;

@Repository
public class UserRepository implements InitializingBean{
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS users (" +
            "userid VARCHAR(50) PRIMARY KEY, " +
            "name VARCHAR(100) NOT NULL, " +
            "password VARCHAR(100) NOT NULL, " +
            "bio VARCHAR(500))"
        );
    }
}