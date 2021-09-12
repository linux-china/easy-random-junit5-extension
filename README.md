Easy Random JUnit 5 Extension
=============================

The easy random extension provides a test with randomly generated objects, including:

* JDK types: int/double/BigDecimal/boolean/String etc
* Custom types: POJO, [except for records support](https://github.com/j-easy/easy-random/issues/397)
* Generic collections: List/Set/Stream/Array
* Java Validation annotations: @Email, @Pattern etc
* Custom Annotation with Validation annotation, such as @Phone

```java
@Documented
@Constraint(validatedBy = {})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Pattern(regexp = "(136|137|186)\\d{8}")
public @interface Phone {
}
```

# How to use?

Include following dependency in your pom.xml

```xml
  <dependency>
    <groupId>org.mvnsearch</groupId>
    <artifactId>easy-random-junit5-extension</artifactId>
    <version>0.1.1</version>
    <scope>test</scope>
  </dependency>
```

# Usage examples:

* Injecting random values as fields:

```java
   import org.jeasy.random.EasyRandomExtension;
   import org.jeasy.random.Random;

   @ExtendWith(EasyRandomExtension.class)
   public class MyTest {
  
       @Random
       private String anyString;
  
       @Random
       private List<DomainObject> domainObjectList;
       
       @Test
       public void testUsingRandomString() {
           // use the injected anyString
           // ...
       }
  
       @Test
       public void testUsingRandomDomainObjects() {
           // use the injected anyDomainObjects
           // the anyDomainObjects will contain _N_ fully populated random instances of DomainObject
           // ...
       }
  
       @Test
       public void testUsingPartiallyPopulatedDomainObject() {
           // use the injected anyPartiallyPopulatedDomainObject
           // this object's "name" and "value" members will not be populated since this has been declared with
           //     excluded = {"name", "value"}
           // ...
       }
   }

```

* Injecting random values as parameters:

```java
   @ExtendWith(EasyRandomExtension.class)
   public class MyTest {
  
       @Test
       public void testUsingRandomString(@Random @Email String anyString) {
           // use the provided anyString
           // ...
       }
  
       @Test
       public void testUsingRandomDomainObjects(@Random List<DomainObject> anyDomainObjects) {
           // use the injected anyDomainObjects
           // the anyDomainObjects will contain _N_ fully populated random instances of DomainObject
           // ...
       }
   }
```

# JUnit 5 Automatic Extension Registration

With Automatic Extension Registration, and you can remove `@ExtendWith(EasyRandomExtension.class)` on test class.

* Create `src/test/resources/junit-platform.properties` with following code:

```properties
junit.jupiter.extensions.autodetection.enabled=true
```

* Remove `@ExtendWith(EasyRandomExtension.class)` on test class

# References and Thanks

* Easy Random: https://github.com/j-easy/easy-random
* RandomBeansExtension: https://glytching.github.io/junit-extensions/randomBeans
