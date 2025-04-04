package pdl.backend.Security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    public String encryptPassword(String plainPassword) {
        return encoder.encode(plainPassword);
    }
    
    public boolean matchPassword(String plainPassword, String hashedPassword) {
        return encoder.matches(plainPassword, hashedPassword);
    }
}