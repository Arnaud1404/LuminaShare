package pdl.backend.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import pdl.backend.Security.PasswordService;

@Repository
public class UserDao {
    private final Map<String, User> users = new HashMap<>();
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordService passwordService;
    
    /**
     * Creates a new user in memory and database
     * 
     * @param user The user to create
     * @return true if successful, false if userid already exists
     */
    public boolean create(final User user) {
        // Check if user already exists
        if (users.containsKey(user.getUserid())) {
            return false;
        }
        
        // Encrypt the password before storing
        String encryptedPassword = passwordService.encryptPassword(user.getPassword());
        user.setPassword(encryptedPassword);
        
        // Add to in-memory map
        users.put(user.getUserid(), user);
        
        // Add to database
        return userRepository.addUser(user) > 0;
    }
    
    /**
     * Retrieves a user from in-memory collection
     * 
     * @param userid The unique user ID
     * @return Optional containing the user if found, else empty
     */
    public Optional<User> retrieve(final String userid) {
        return Optional.ofNullable(users.get(userid));
    }
    
    /**
     * Retrieves all users from in-memory collection
     * 
     * @return List of all users
     */
    public List<User> retrieveAll() {
        return new ArrayList<>(users.values());
    }
    
    /**
     * Updates a user's information
     * 
     * @param user The updated user object
     * @param updatePassword Whether to update the password (if true, will encrypt the new password)
     * @return true if successful
     */
    public boolean update(final User user, boolean updatePassword) {
        if (!users.containsKey(user.getUserid())) {
            return false;
        }
        
        if (updatePassword) {
            String encryptedPassword = passwordService.encryptPassword(user.getPassword());
            user.setPassword(encryptedPassword);
        } else {
            // Keep existing password
            User existingUser = users.get(user.getUserid());
            user.setPassword(existingUser.getPassword());
        }
        
        users.put(user.getUserid(), user);
        return userRepository.updateUser(user) > 0;
    }
    
    /**
     * Deletes a user
     * 
     * @param userid The ID of the user to delete
     * @return true if successful
     */
    public boolean delete(final String userid) {
        if (!users.containsKey(userid)) {
            return false;
        }
        
        users.remove(userid);
        return userRepository.deleteUser(userid) > 0;
    }
    
    /**
     * Validates login credentials
     * 
     * @param userid User ID
     * @param password Plain text password to check
     * @return Optional user if credentials valid, empty otherwise
     */
    public Optional<User> authenticate(String userid, String password) {
        // Get user from database (passwords stored in DB)
        Optional<User> userOpt = userRepository.getUserById(userid);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordService.matchPassword(password, user.getPassword())) {
                // Add to in-memory cache if not present
                users.putIfAbsent(userid, user);
                return Optional.of(user);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Loads all users from database into memory
     */
    public void loadAllUsers() {
        List<User> userList = userRepository.getAllUsers();
        users.clear();
        for (User user : userList) {
            users.put(user.getUserid(), user);
        }
    }
}