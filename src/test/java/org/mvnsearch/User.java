package org.mvnsearch;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;

import javax.validation.constraints.Pattern;

public class User {
    @Positive
    private Integer id;
    @Pattern(regexp = "\\w{6}")
    private String name;
    @Email
    private String email;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
