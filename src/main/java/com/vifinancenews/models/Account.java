package com.vifinancenews.models;

public class Account {
    private String id; // Now stores hashed UUID
    private String userName;
    private String avatarLink;
    private String bio;

    public Account(String id, String userName, String avatarLink, String bio) {
        this.id = id;
        this.userName = userName;
        this.avatarLink = avatarLink;
        this.bio = bio;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatarLink() {
        return avatarLink;
    }

    public void setAvatarLink(String avatarLink) {
        this.avatarLink = avatarLink;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", avatarLink='" + avatarLink + '\'' +
                ", bio='" + bio + '\'' +
                '}';
    }
}
