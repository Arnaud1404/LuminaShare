package pdl.backend.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserDao userDao;

    // @Autowired
    // private PasswordService passwordService;

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
}
