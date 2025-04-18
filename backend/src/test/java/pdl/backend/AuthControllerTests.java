package pdl.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import pdl.backend.User.User;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class AuthControllerTests {

        @Autowired
        private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper();

        private final String testUserId = "toto";
        private final String testPassword = "toto123";
        private final String testName = "Toto";
        private final String testBio = "Toto le testeur";

        @Test
        @Order(1)
        public void registerUserShouldReturnSuccess() throws Exception {
                User user = new User();
                user.setUserid(testUserId);
                user.setPassword(testPassword);
                user.setName(testName);
                user.setBio(testBio);

                this.mockMvc
                                .perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(user)))
                                .andDo(print()).andExpect(status().isCreated())
                                .andExpect(content().string("User registered successfully"));
        }

        @Test
        @Order(2)
        public void registerExistingUserShouldReturnConflict() throws Exception {
                User user = new User();
                user.setUserid(testUserId);
                user.setPassword(testPassword);
                user.setName(testName);
                user.setBio(testBio);

                this.mockMvc
                                .perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(user)))
                                .andDo(print()).andExpect(status().isConflict());
        }

        @Test
        @Order(3)
        public void loginUserShouldReturnSuccess() throws Exception {
                Map<String, String> credentials = new HashMap<>();
                credentials.put("userid", testUserId);
                credentials.put("password", testPassword);

                this.mockMvc
                                .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(credentials)))
                                .andDo(print()).andExpect(status().isOk())
                                .andExpect(jsonPath("$.userid").value(testUserId))
                                .andExpect(jsonPath("$.name").value(testName))
                                .andExpect(jsonPath("$.bio").value(testBio));
        }

        @Test
        @Order(4)
        public void loginWithInvalidPasswordShouldReturnUnauthorized() throws Exception {
                Map<String, String> credentials = new HashMap<>();
                credentials.put("userid", testUserId);
                credentials.put("password", "wrongpassword");

                this.mockMvc
                                .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(credentials)))
                                .andDo(print()).andExpect(status().isUnauthorized());
        }

        @Test
        @Order(5)
        public void loginWithNonExistentUserShouldReturnUnauthorized() throws Exception {
                Map<String, String> credentials = new HashMap<>();
                credentials.put("userid", "nonexistentuser");
                credentials.put("password", "toto123");

                this.mockMvc
                                .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(credentials)))
                                .andDo(print()).andExpect(status().isUnauthorized());
        }

        @Test
        @Order(6)
        public void loginWithMissingCredentialsShouldReturnBadRequest() throws Exception {
                Map<String, String> credentials = new HashMap<>();
                credentials.put("userid", testUserId);

                this.mockMvc
                                .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(credentials)))
                                .andDo(print()).andExpect(status().isBadRequest());
        }
}
