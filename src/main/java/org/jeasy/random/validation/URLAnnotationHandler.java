package org.jeasy.random.validation;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.URL;
import org.jeasy.random.api.Randomizer;

import java.lang.reflect.Field;

public class URLAnnotationHandler implements BeanValidationAnnotationHandler {
    @Override
    public Randomizer<?> getRandomizer(Field field) {
        final URL urlAnnotation = field.getAnnotation(URL.class);
        return (Randomizer<Object>) () -> {
            String protocol = StringUtils.defaultIfEmpty(urlAnnotation.protocol(), "http");
            String host = StringUtils.defaultIfEmpty(urlAnnotation.host(), "www.example.com");
            String portString = urlAnnotation.port() == -1 ? "" : ":" + urlAnnotation.port();
            String path = "/" + RandomStringUtils.randomAlphabetic(10);
            return protocol + "://" + host + portString + path;
        };
    }
}
