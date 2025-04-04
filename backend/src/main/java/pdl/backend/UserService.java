package pdl.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pdl.backend.Database.UserRepository;
import pdl.backend.Security.PasswordService;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordService passwordService;
    
    // Authentication and user management methods
}