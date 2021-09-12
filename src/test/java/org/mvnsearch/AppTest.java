package org.mvnsearch;


import com.github.javafaker.Address;
import org.jeasy.random.EasyRandomExtension;
import org.jeasy.random.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(EasyRandomExtension.class)
public class AppTest {
    @Random
    private List<User> users;
    @Random
    @Phone
    private String phone2;


    @Test
    public void testRandomEmail(@Random @Email String email) {
        System.out.println(email);
    }

    @Test
    public void testPhone(@Random @Phone String phone) {

    }

    @Test
    public void testAddress(@Random(locale = "zh_CN") Address address){
        System.out.println(address.cityName());
    }

    @Test
    public void testRandomUser(@Random User user) {
        assertThat(user.getEmail()).contains("@");
    }

    @Test
    public void testRandomUserList(@Random(size = 5) List<User> users) {
        assertThat(users).hasSize(5);
    }

    @Test
    public void testRandomFullName(@Random @Pattern(regexp = "[A-Z][a-z]{6} [A-Z][a-z]{4}") String fullName) {
        assertThat(fullName).hasSize(13).contains(" ");
    }
}
