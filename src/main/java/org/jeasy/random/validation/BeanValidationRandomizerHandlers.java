package org.jeasy.random.validation;

import net.bytebuddy.ByteBuddy;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.api.Randomizer;
import org.jeasy.random.util.ReflectionUtils;

import javax.validation.constraints.*;
import java.lang.annotation.Annotation;
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
public class BeanValidationRandomizerHandlers {
    protected Map<Class<? extends Annotation>, BeanValidationAnnotationHandler> annotationHandlers = new HashMap<>();

    public void init(EasyRandomParameters parameters) {
        long seed = parameters.getSeed();
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
    }

    public Randomizer<?> getRandomizer(final Parameter param) {
        for (Map.Entry<Class<? extends Annotation>, BeanValidationAnnotationHandler> entry : annotationHandlers.entrySet()) {
            Class<? extends Annotation> annotation = entry.getKey();
            BeanValidationAnnotationHandler annotationHandler = entry.getValue();
            if (param.isAnnotationPresent(annotation) && annotationHandler != null) {
                final Field field = mockField(param.getName(), param.getType(), param.getAnnotations());
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
            if (ReflectionUtils.isAnnotationPresent(field, annotation) && annotationHandler != null) {
                return annotationHandler.getRandomizer(field);
            }
        }
        return null;
    }

    private Field mockField(String name, Class<?> type, Annotation[] annotations) {
        try {
            final Class<?> clazz = new ByteBuddy()
                    .subclass(Object.class)
                    .defineField(name, type)
                    .annotateField(annotations)
                    .make()
                    .load(this.getClass().getClassLoader())
                    .getLoaded();
            return clazz.getDeclaredField(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
