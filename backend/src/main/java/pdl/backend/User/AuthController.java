package pdl.backend.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pdl.backend.Security.PasswordService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordService passwordService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userDao.retrieve(user.getUserid()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }

        String hashedPassword = passwordService.encryptPassword(user.getPassword());
        user.setPassword(hashedPassword);

        userDao.create(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String userid = credentials.get("userid");
        String password = credentials.get("password");

        if (userid == null || password == null) {
            return ResponseEntity.badRequest().body("User ID and password are required");
        }

        Optional<User> userOpt = userDao.retrieve(userid);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordService.matchPassword(password, user.getPassword())) {
                Map<String, Object> response = new HashMap<>();
                response.put("userid", user.getUserid());
                response.put("name", user.getName());
                response.put("bio", user.getBio() != null ? user.getBio() : "");

                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
}
