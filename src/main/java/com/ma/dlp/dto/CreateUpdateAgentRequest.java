package com.ma.dlp.dto;

import lombok.Data;

@Data
public class CreateUpdateAgentRequest {
    private String username;
    private String password;
    private String email;
    // private String role;


    public String getPassword() {
        return password;
    }

   public String getEmail() {   // ✅
        return email;
    }

    public void setEmail(String email) {   // ✅
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    // public String getRole() {
    //     return role;
    // }

    // public void setRole(String role) {
    //     this.role = role;
    // }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}