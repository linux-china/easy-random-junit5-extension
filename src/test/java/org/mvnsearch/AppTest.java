package org.mvnsearch;


import org.jeasy.random.EasyRandomExtension;
import org.jeasy.random.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(EasyRandomExtension.class)
public class AppTest {
    @Random
    @Email
    private String email;

    @Test
    public void shouldAnswerWithTrue(@Random @Positive int value) {
        assertThat(value).isGreaterThan(0);
        assertThat(email).contains("@");
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
