package pdl.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DatabaseTests {

    @Autowired
    private Database repository;

    @Test
    void testGetNbImages() {
        assertEquals(repository.getNbImages(), Database.names.length);
    }

    @Test
    void testGetImageName() {
        for (int i = 0; i < Database.names.length; ++i) {
            assertEquals(repository.getImageName(i + 1), Database.names[i]);
        }
    }
}
