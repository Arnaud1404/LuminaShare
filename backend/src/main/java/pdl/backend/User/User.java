package pdl.backend.User;

public class User {
    private String userid;
    private String name;
    private String password;
    private String bio;
    
    /**
     * Default constructor
     */
    public User() {
    }
    
    /**
     * Full constructor
     * 
     * @param userid Unique user identifier
     * @param name User's display name
     * @param password User's password
     * @param bio User's bio/description
     */
    public User(String userid, String name, String password, String bio) {
        this.userid = userid;
        this.name = name;
        this.password = password;
        this.bio = bio;
    }
    
    /**
     * Gets user ID
     * 
     * @return User ID
     */
    public String getUserid() {
        return userid;
    }
    
    /**
     * Sets user ID
     * 
     * @param userid User ID to set
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }
    
    /**
     * Gets user display name
     * 
     * @return User's name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets user display name
     * 
     * @param name Name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets user password (hashed)
     * 
     * @return Password hash
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Sets user password
     * 
     * @param password Password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Gets user bio
     * 
     * @return User's bio
     */
    public String getBio() {
        return bio;
    }
    
    /**
     * Sets user bio
     * 
     * @param bio Bio to set
     */
    public void setBio(String bio) {
        this.bio = bio;
    }
}