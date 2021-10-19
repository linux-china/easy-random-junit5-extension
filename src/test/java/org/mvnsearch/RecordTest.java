package org.mvnsearch;

import org.jeasy.random.EasyRandomExtension;
import org.jeasy.random.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.validation.constraints.Email;
import javax.validation.constraints.Positive;

@ExtendWith(EasyRandomExtension.class)
public class RecordTest {

    record Person(@Positive int id, @Email String email) {
    }

    @Test
    public void testRecord(@Random Person person) {
        System.out.println("person.id = " + person.id());
        System.out.println("person.email = " + person.email());
    }
}
