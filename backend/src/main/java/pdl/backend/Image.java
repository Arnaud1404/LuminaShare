package pdl.backend;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.pgvector.PGvector; // Ajouté explicitement

public class Image {
  private static Long count = Long.valueOf(1);
  private Long id;
  private String name;
  private MediaType type;
  private String size;
  private String description;
  private byte[] data;
  private PGvector descriptor; // champ pour stocker le descripteur

  public Image(final String name, final byte[] data,
      MediaType type, long width, long height,
      final String description) {
    id = count++;
    this.name = name;
    this.type = type;
    this.size = width + "*" + height;
    this.description = description;
    this.data = data;
    this.descriptor = null; // Initialisé à null, sera calculé plus tard
  }

  public long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public MediaType getType() {
    return type;
  }

  public String getDesciption() {
    return description;
  }

  public String getSize() {
    return size;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public byte[] getData() {
    return data;
  }
  // Nouveau getter et setter pour le descripteur
  public PGvector getDescriptor() { 
    return descriptor;
  }
  public void setDescriptor(PGvector descriptor) { 
    this.descriptor = descriptor; 
  }
}
