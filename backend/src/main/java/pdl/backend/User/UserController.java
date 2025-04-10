package pdl.backend.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pdl.backend.Security.PasswordService;

import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserDao userDao;

    // @Autowired
    // private PasswordService passwordService;

    /**
     * Get all users
     * 
     * @return List of all users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userDao.retrieveAll();
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(users);
    }

    /**
     * Get a specific user by ID
     * 
     * @param userid User ID to retrieve
     * @return User information if found
     */
    @GetMapping("/{userid}")
    public ResponseEntity<?> getUserById(@PathVariable String userid) {
        Optional<User> user = userDao.retrieve(userid);
        if (user.isPresent()) {
            User userObj = user.get();
            // Don't return password
            userObj.setPassword(null);
            return ResponseEntity.ok(userObj);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    /**
     * Update user information
     * 
     * @param userid User ID to update
     * @param userData Updated user data
     * @return Updated user or error
     */
    @PutMapping("/{userid}")
    public ResponseEntity<?> updateUser(@PathVariable String userid, @RequestBody User userData) {

        Optional<User> existingUser = userDao.retrieve(userid);
        if (!existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        if (!userid.equals(userData.getUserid())) {
            return ResponseEntity.badRequest().body("User ID in path must match user ID in body");
        }

        boolean updatePassword =
                (userData.getPassword() != null && !userData.getPassword().isEmpty());

        boolean success = userDao.update(userData, updatePassword);

        if (success) {
            User updatedUser = userDao.retrieve(userid).get();
            updatedUser.setPassword(null);
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update user");
        }
    }

    /**
     * Delete a user
     * 
     * @param userid User ID to delete
     * @return Success or error message
     */
    @DeleteMapping("/{userid}")
    public ResponseEntity<?> deleteUser(@PathVariable String userid) {
        boolean success = userDao.delete(userid);
        if (success) {
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    /**
     * Check if a user exists
     * 
     * @param userid User ID to check
     * @return Status indicating if user exists
     */
    @GetMapping("/{userid}/exists")
    public ResponseEntity<?> checkUserExists(@PathVariable String userid) {
        boolean exists = userDao.retrieve(userid).isPresent();
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
}
