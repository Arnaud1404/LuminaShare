package pdl.backend.User;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository implements InitializingBean {

    @Value("${DATABASE_RESET:true}")
    private boolean resetDatabase;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<User> rowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setUserid(rs.getString("userid"));
        user.setName(rs.getString("name"));
        user.setPassword(rs.getString("password"));
        user.setBio(rs.getString("bio"));
        return user;
    };

    @Override
    public void afterPropertiesSet() throws Exception {
        // Empty - initialization moved to DatabaseInitializer
    }

    /**
     * Adds a new user to the database
     * 
     * @param user The user to add
     * @return Number of rows affected (1 if successful)
     */
    public int addUser(User user) {
        return jdbcTemplate.update(
                "INSERT INTO users (userid, name, password, bio) VALUES (?, ?, ?, ?)",
                user.getUserid(), user.getName(), user.getPassword(), user.getBio());
    }

    /**
     * Gets a user from the database by userid
     * 
     * @param userid The user's ID
     * @return Optional containing the user if found, empty otherwise
     */
    public Optional<User> getUserById(String userid) {
        try {
            User user = jdbcTemplate.queryForObject(
                    "SELECT userid, name, password, bio FROM users WHERE userid = ?", rowMapper,
                    userid);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets all users from the database
     * 
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return jdbcTemplate.query("SELECT userid, name, password, bio FROM users", rowMapper);
    }

    /**
     * Updates an existing user's information
     * 
     * @param user The user with updated information
     * @return Number of rows affected (1 if successful)
     */
    public int updateUser(User user) {
        return jdbcTemplate.update(
                "UPDATE users SET name = ?, password = ?, bio = ? WHERE userid = ?", user.getName(),
                user.getPassword(), user.getBio(), user.getUserid());
    }

    /**
     * Deletes a user by userid
     * 
     * @param userid The ID of the user to delete
     * @return Number of rows affected (1 if successful)
     */
    public int deleteUser(String userid) {
        return jdbcTemplate.update("DELETE FROM users WHERE userid = ?", userid);
    }

    /**
     * Checks if a user with the given ID exists
     * 
     * @param userid The user ID to check
     * @return true if the user exists
     */
    public boolean userExists(String userid) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE userid = ?",
                Integer.class, userid);
        return count != null && count > 0;
    }

    /**
     * Gets the count of all users
     * 
     * @return Number of users in the database
     */
    public long getUserCount() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        return count != null ? count : 0;
    }
}
