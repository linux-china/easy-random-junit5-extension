package org.mvnsearch;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.CreditCardNumber;

import javax.validation.constraints.Pattern;

public class User {
    @Positive
    private Integer id;
    @Pattern(regexp = "\\w{6}")
    private String name;
    @Email
    private String email;
    @CreditCardNumber
    private String creditCardNumber;

    @Pattern(regexp = "(https?)://example.com/avatars/[a-f0-9]{10,20}")
    private String avatar;

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

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
