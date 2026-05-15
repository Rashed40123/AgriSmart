package com.agrisoft.models;

public class User {
    private int id;
    private String username;
    private String password; // This will be hashed
    private String nid;
    private String name;
    private String phone;
    private String profilePic;

    public User() {}

    public User(String username, String password, String nid, String name, String phone) {
        this.username = username;
        this.password = password;
        this.nid = nid;
        this.name = name;
        this.phone = phone;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNid() { return nid; }
    public void setNid(String nid) { this.nid = nid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProfilePic() { return profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }
}