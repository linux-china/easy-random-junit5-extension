package org.mvnsearch;

import org.jeasy.random.EasyRandomExtension;
import org.jeasy.random.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.validation.constraints.Email;

@ExtendWith(EasyRandomExtension.class)
public class RecordTest {

    record Person(int id, @Email String name) {
    }

    @Test
    public void testRecord(@Random Person person) {
        System.out.println("person.id = " + person.id());
        System.out.println("person.name = " + person.name());
    }
}
