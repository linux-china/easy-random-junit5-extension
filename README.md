<div align="center">
    <b><em>Easy Random/Faker JUnit 5 extension</em></b><br>
    The simple, stupid random Java&trade; beans generator for JUnit 5
</div>

<div align="center">

[![MIT license](https://img.shields.io/badge/license-Apache-brightgreen.svg?style=flat)](http://opensource.org/licenses/Apache-2.0)
[![Build Status](https://github.com/linux-china/easy-random-junit5-extension/actions/workflows/main.yml/badge.svg)](https://github.com/linux-china/easy-random-junit5-extension/actions)
[![Maven Central](https://img.shields.io/maven-central/v/org.mvnsearch/easy-random-junit5-extension)](https://repo1.maven.org/maven2/org/mvnsearch/easy-random-junit5-extension/)
[![Project status](https://img.shields.io/badge/Project%20status-Maintenance-orange.svg)](https://img.shields.io/badge/Project%20status-Maintenance-orange.svg)

</div>

The easy random extension provides a test with randomly generated objects, including:

* JDK types: int/double/BigDecimal/boolean/String etc
* Custom types: POJO, [except for records support](https://github.com/j-easy/easy-random/issues/397)
* Generic collections: List/Set/Stream/Array
* Java Faker support: Name, Internet, Address etc
* Java Validation annotations: @Email, @Pattern etc
* Custom Annotation with Validation annotation, such as @Phone. For more https://any86.github.io/any-rule/

```java
@Documented
@Constraint(validatedBy = {})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Pattern(regexp = "^1[3-9]\\d{9}$")
public @interface Phone {
}
```

* i18n friendly to Faker's types

```groovy
import com.github.javafaker.Address  
  
@Test
public void testAddress(@Random(locale = "zh_CN") Address address) {
  System.out.println(address.cityName());
}
```

# How to use?

Include following dependency in your pom.xml

```xml
  <dependency>
    <groupId>org.mvnsearch</groupId>
    <artifactId>easy-random-junit5-extension</artifactId>
    <version>0.3.0</version>
    <!--<version>0.2.0</version> for Java 8 & 11-->
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
