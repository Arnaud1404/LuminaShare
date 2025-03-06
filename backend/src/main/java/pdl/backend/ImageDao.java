package pdl.backend;

import java.util.Arrays;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.beans.factory.annotation.Autowired;

@Repository
public class ImageDao implements Dao<Image> {

  private final Map<Long, Image> images = new HashMap<>();
  private long idCounter = 1L;// Compteur d'ID pour assurer des IDs uniques

  @Autowired
  private JdbcTemplate jdbcTemplate; // Utilisation de JdbcTemplate pour PostgreSQL
  


  
  public void afterPropertiesSet() throws Exception {
    // Création automatique de la table si elle n'existe pas
    jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS images (
                id bigserial PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                data BYTEA NOT NULL,
                histogram2D TEXT NOT NULL,
                histogram3D TEXT NOT NULL
            )
        """);
    }
  /**
    * Sauvegarde une image en lui attribuant un ID unique.
  */
  public void saveImage(String fileName, byte[] fileContent, String histogram2D, String histogram3D) {
    Image img = new Image(fileName, fileContent, histogram2D, histogram3D);
    img.setId(idCounter++);
    images.put(img.getId(), img);

    // Enregistrement en base de données
   jdbcTemplate.update("INSERT INTO images (name, data, histogram2D, histogram3D) VALUES (?, ?, ?, ?)",
            fileName, fileContent, histogram2D, histogram3D);
  }
  

  public ImageDao() {
    // placez une image test.jpg dans le dossier "src/main/resources" du projet
    final ClassPathResource imgFile = new ClassPathResource("images/test.jpg");
    byte[] fileContent;
    try {
      fileContent = Files.readAllBytes(imgFile.getFile().toPath());
      Image img = new Image("default.jpg", fileContent, "default_2D", "default_3D");
      images.put(img.getId(), img);
    } catch (final IOException e) {
      e.printStackTrace();
    }

  }

  @Override
  public Optional<Image> retrieve(final long id) {
    if (images.containsKey(id)) {
      Optional<Image> img = Optional.ofNullable(images.get(id));
      return img;
    }
    String sql = "SELECT * FROM images WHERE id = ?";
    List<Image> result = jdbcTemplate.query(sql, new ImageRowMapper(), id);
    return result.stream().findFirst();
  }

  @Override
  public List<Image> retrieveAll() {
    // Fusionne les données en mémoire et celles de la base de données
    List<Image> allImages = new ArrayList<>(images.values());
    String sql = "SELECT * FROM images";
    allImages.addAll(jdbcTemplate.query(sql, new ImageRowMapper()));
    return allImages;
  }

  @Override
  public void create(final Image img) {
    saveImage(img.getName(), img.getData(), img.getHistogram2D(), img.getHistogram3D());

    
  }

  @Override
  public void update(final Image img, final String[] params) {
    String sql = "UPDATE images SET name = ?, histogram2D = ?, histogram3D = ? WHERE id = ?";
    jdbcTemplate.update(sql, img.getName(), img.getHistogram2D(), img.getHistogram3D(), img.getId());
  }

  @Override
  public void delete(final Image img) {
    //suppression en mémoire
    images.remove(img.getId());

    // Suppression en base de données
    String sql = "DELETE FROM images WHERE id = ?";
    jdbcTemplate.update(sql, img.getId());
  }

  private static class ImageRowMapper implements RowMapper<Image> {
    @Override
    public Image mapRow(ResultSet rs, int rowNum) throws SQLException {
      Image img = new Image(
            rs.getString("name"),
            rs.getBytes("data"),
            rs.getString("histogram2D"),
            rs.getString("histogram3D")
      );
      img.setId(rs.getLong("id"));
       return img;
    }
  }  
  
}
