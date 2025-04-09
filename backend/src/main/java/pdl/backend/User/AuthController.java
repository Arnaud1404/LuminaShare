package pdl.backend.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class AuthController {
    @Autowired
    private UserDao userDao;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (user.getUserid() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().body("Userid and password are required");
        }
        
        boolean success = userDao.create(user);
        if (success) {
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User ID already exists");
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String userid = credentials.get("userid");
        String password = credentials.get("password");
        
        if (userid == null || password == null) {
            return ResponseEntity.badRequest().body("Userid and password are required");
        }
        
        Optional<User> user = userDao.authenticate(userid, password);
        if (user.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("userid", user.get().getUserid());
            response.put("name", user.get().getName());
            response.put("bio", user.get().getBio());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}