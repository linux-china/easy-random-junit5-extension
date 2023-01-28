package org.jeasy.random.validation;

import net.bytebuddy.ByteBuddy;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.api.Randomizer;
import org.jeasy.random.api.RandomizerRegistry;

import javax.validation.constraints.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;


/**
 * Java Validation annotations handlers for parameter and field
 *
 * @author Random Beans
 * @author linux_china
 */
public class BeanValidationRandomizerHandlers implements RandomizerRegistry {
    protected Map<Class<? extends Annotation>, BeanValidationAnnotationHandler> annotationHandlers = new HashMap<>();

    public void init(EasyRandomParameters parameters) {
        long seed = parameters.getSeed();
        //javax validation
        annotationHandlers.put(AssertFalse.class, new AssertFalseAnnotationHandler());
        annotationHandlers.put(AssertTrue.class, new AssertTrueAnnotationHandler());
        annotationHandlers.put(Null.class, new NullAnnotationHandler());
        annotationHandlers.put(Future.class, new FutureAnnotationHandler(parameters));
        annotationHandlers.put(FutureOrPresent.class, new FutureOrPresentAnnotationHandler(parameters));
        annotationHandlers.put(Past.class, new PastAnnotationHandler(parameters));
        annotationHandlers.put(PastOrPresent.class, new PastOrPresentAnnotationHandler(parameters));
        annotationHandlers.put(Min.class, new MinMaxAnnotationHandler(seed));
        annotationHandlers.put(Max.class, new MinMaxAnnotationHandler(seed));
        annotationHandlers.put(DecimalMin.class, new DecimalMinMaxAnnotationHandler(seed));
        annotationHandlers.put(DecimalMax.class, new DecimalMinMaxAnnotationHandler(seed));
        annotationHandlers.put(Pattern.class, new PatternAnnotationHandler(seed));
        annotationHandlers.put(Size.class, new SizeAnnotationHandler(parameters));
        annotationHandlers.put(Positive.class, new PositiveAnnotationHandler(seed));
        annotationHandlers.put(PositiveOrZero.class, new PositiveOrZeroAnnotationHandler(seed));
        annotationHandlers.put(Negative.class, new NegativeAnnotationHandler(seed));
        annotationHandlers.put(NegativeOrZero.class, new NegativeOrZeroAnnotationHandler(seed));
        annotationHandlers.put(NotBlank.class, new NotBlankAnnotationHandler(seed));
        annotationHandlers.put(Email.class, new EmailAnnotationHandler(seed));
        //jakarta validation
        try {
            Class.forName("jakarta.validation.constraints.Pattern");
            annotationHandlers.put(jakarta.validation.constraints.AssertFalse.class, new AssertFalseAnnotationHandler());
            annotationHandlers.put(jakarta.validation.constraints.AssertTrue.class, new AssertTrueAnnotationHandler());
            annotationHandlers.put(jakarta.validation.constraints.Null.class, new NullAnnotationHandler());
            annotationHandlers.put(jakarta.validation.constraints.Future.class, new FutureAnnotationHandler(parameters));
            annotationHandlers.put(jakarta.validation.constraints.FutureOrPresent.class, new FutureOrPresentAnnotationHandler(parameters));
            annotationHandlers.put(jakarta.validation.constraints.Past.class, new PastAnnotationHandler(parameters));
            annotationHandlers.put(jakarta.validation.constraints.PastOrPresent.class, new PastOrPresentAnnotationHandler(parameters));
            annotationHandlers.put(jakarta.validation.constraints.Min.class, new JakartaMinMaxAnnotationHandler(seed));
            annotationHandlers.put(jakarta.validation.constraints.Max.class, new JakartaMinMaxAnnotationHandler(seed));
            annotationHandlers.put(jakarta.validation.constraints.DecimalMin.class, new JakartaDecimalMinMaxAnnotationHandler(seed));
            annotationHandlers.put(jakarta.validation.constraints.DecimalMax.class, new JakartaDecimalMinMaxAnnotationHandler(seed));
            annotationHandlers.put(jakarta.validation.constraints.Pattern.class, new JakartaPatternAnnotationHandler(seed));
            annotationHandlers.put(jakarta.validation.constraints.Size.class, new JakartaSizeAnnotationHandler(parameters));
            annotationHandlers.put(jakarta.validation.constraints.Positive.class, new PositiveAnnotationHandler(seed));
            annotationHandlers.put(jakarta.validation.constraints.PositiveOrZero.class, new PositiveOrZeroAnnotationHandler(seed));
            annotationHandlers.put(jakarta.validation.constraints.Negative.class, new NegativeAnnotationHandler(seed));
            annotationHandlers.put(jakarta.validation.constraints.NegativeOrZero.class, new NegativeOrZeroAnnotationHandler(seed));
            annotationHandlers.put(jakarta.validation.constraints.NotBlank.class, new NotBlankAnnotationHandler(seed));
            annotationHandlers.put(jakarta.validation.constraints.Email.class, new EmailAnnotationHandler(seed));
        } catch (Exception ignore) {
        }
        //hibernate validator
        try {
            Class.forName("org.hibernate.validator.constraints.URL");
            annotationHandlers.put(org.hibernate.validator.constraints.URL.class, new URLAnnotationHandler());
            annotationHandlers.put(org.hibernate.validator.constraints.ISBN.class, new IsbnAnnotationHandler(seed));
            annotationHandlers.put(org.hibernate.validator.constraints.CreditCardNumber.class, new CreditCardNumberAnnotationHandler(seed));
            annotationHandlers.put(org.hibernate.validator.constraints.EAN.class, new EANAnnotationHandler(seed));
            annotationHandlers.put(org.hibernate.validator.constraints.UUID.class, new UUIDAnnotationHandler());
            annotationHandlers.put(org.hibernate.validator.constraints.Length.class, new LengthAnnotationHandler(parameters));
            annotationHandlers.put(org.hibernate.validator.constraints.Range.class, new RangeAnnotationHandler());
        } catch (Exception ignore) {
        }
    }

    public Randomizer<?> getRandomizer(final Parameter param) {
        for (Map.Entry<Class<? extends Annotation>, BeanValidationAnnotationHandler> entry : annotationHandlers.entrySet()) {
            Class<? extends Annotation> annotation = entry.getKey();
            BeanValidationAnnotationHandler annotationHandler = entry.getValue();
            final Annotation validationAnnotation = getValidationAnnotation(param, annotation);
            if (validationAnnotation != null && annotationHandler != null) {
                final Field field = mockField(param.getName(), param.getType(), validationAnnotation);
                if (field != null) {
                    return annotationHandler.getRandomizer(field);
                }
            }
        }
        return null;
    }

    public Randomizer<?> getRandomizer(final Field field) {
        for (Map.Entry<Class<? extends Annotation>, BeanValidationAnnotationHandler> entry : annotationHandlers.entrySet()) {
            Class<? extends Annotation> annotation = entry.getKey();
            BeanValidationAnnotationHandler annotationHandler = entry.getValue();
            final Annotation validationAnnotation = getValidationAnnotation(field, annotation);
            if (validationAnnotation != null && annotationHandler != null) {
                final Field field2 = mockField(field.getName(), field.getType(), validationAnnotation);
                return annotationHandler.getRandomizer(field2);
            }
        }
        return null;
    }

    @Override
    public Randomizer<?> getRandomizer(Class<?> type) {
        return null;
    }

    private Field mockField(String name, Class<?> type, Annotation validationAnnotation) {
        try {
            final Class<?> clazz = new ByteBuddy()
                    .subclass(Object.class)
                    .defineField(name, type)
                    .annotateField(validationAnnotation)
                    .make()
                    .load(this.getClass().getClassLoader())
                    .getLoaded();
            return clazz.getDeclaredField(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private Annotation getValidationAnnotation(AnnotatedElement element, Class<? extends Annotation> annotationClass) {
        for (Annotation elementAnnotation : element.getAnnotations()) {
            final Class<? extends Annotation> annotationType = elementAnnotation.annotationType();
            if (annotationType == annotationClass) {
                return elementAnnotation;
            } else if (annotationType.isAnnotationPresent(annotationClass)) {
                return annotationType.getAnnotation(annotationClass);
            }
        }
        return null;
    }

}
