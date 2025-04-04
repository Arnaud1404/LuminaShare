package pdl.backend;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import pdl.backend.Database.UserRepository;

@Repository
public class UserDao {
    private final Map<String, User> users = new HashMap<>();
    
    @Autowired
    private UserRepository userRepository;
    
    // Methods for user management
}