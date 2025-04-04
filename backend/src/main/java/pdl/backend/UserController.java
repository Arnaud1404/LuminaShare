package pdl.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pdl.backend.Security.PasswordService;

@RestController
public class UserController {
    @Autowired
    private UserDao userDao;
    
    @Autowired
    private PasswordService passwordService;
    
    // Endpoints for user registration, login, etc.
}