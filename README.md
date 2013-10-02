
This guide walks you through the process of converting a runnable JAR application that was built with [Spring Boot](https://github.com/spring-projects/spring-boot) into a [WAR][u-war] file that you can run in any standard servlet container.

What you'll build
-----------------
You'll take a simple Spring MVC web app and build a WAR file using Spring Boot.

What you'll need
----------------

 - About 15 minutes
 - A favorite text editor or IDE
 - [JDK 6][jdk] or later
 - [Gradle 1.8+][gradle] or [Maven 3.0+][mvn]
 - You can also import the code from this guide as well as view the web page directly into [Spring Tool Suite (STS)][gs-sts] and work your way through it from there.

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[gradle]: http://www.gradle.org/
[mvn]: http://maven.apache.org/download.cgi
[gs-sts]: /guides/gs/sts


How to complete this guide
--------------------------

Like all Spring's [Getting Started guides](/guides/gs), you can start from scratch and complete each step, or you can bypass basic setup steps that are already familiar to you. Either way, you end up with working code.

To **start from scratch**, move on to [Set up the project](#scratch).

To **skip the basics**, do the following:

 - [Download][zip] and unzip the source repository for this guide, or clone it using [Git][u-git]:
`git clone https://github.com/spring-guides/gs-convert-jar-to-war.git`
 - cd into `gs-convert-jar-to-war/initial`.
 - Jump ahead to [Create a basic web application](#initial).

**When you're finished**, you can check your results against the code in `gs-convert-jar-to-war/complete`.
[zip]: https://github.com/spring-guides/gs-convert-jar-to-war/archive/master.zip
[u-git]: /understanding/Git


<a name="scratch"></a>
Set up the project
------------------

First you set up a basic build script. You can use any build system you like when building apps with Spring, but the code you need to work with [Gradle](http://gradle.org) and [Maven](https://maven.apache.org) is included here. If you're not familiar with either, refer to [Building Java Projects with Gradle](/guides/gs/gradle/) or [Building Java Projects with Maven](/guides/gs/maven).

### Create the directory structure

In a project directory of your choosing, create the following subdirectory structure; for example, with `mkdir -p src/main/java/hello` on *nix systems:

    └── src
        └── main
            └── java
                └── hello

### Create a Gradle build file

`build.gradle`
```gradle
buildscript {
    repositories {
        maven { url "http://repo.spring.io/libs-snapshot" }
        mavenLocal()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:0.5.0.M4")
    }
}

apply plugin: 'java'
apply plugin: 'eclipse'
apply plugin: 'idea'
apply plugin: 'spring-boot'

jar {
    baseName = 'gs-convert-jar-to-war'
    version =  '0.1.0'
}

repositories {
    mavenCentral()
    maven { url "http://repo.spring.io/libs-snapshot" }
}

dependencies {
    compile("org.springframework.boot:spring-boot-starter-web:0.5.0.M4")
    compile("org.thymeleaf:thymeleaf-spring3:2.0.16")
    testCompile("junit:junit:4.11")
}

task wrapper(type: Wrapper) {
    gradleVersion = '1.8'
}
```

> **Note:** This guide is using [Spring Boot](/guides/gs/spring-boot/).


<a name="initial"></a>
Create a basic web application
------------------------------
Now that you've set up the project, you can create a Spring MVC application. You'll get it to run using Spring Boot's embedded servlet container. Then, you'll modify things slightly to build a WAR file that can run in any servlet 3.0 container.


### Create a web controller

Spring MVC allows you to quickly build controllers for your web site.

`src/main/java/hello/HelloController.java`
```java
package hello;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HelloController {

    @RequestMapping(value="/", method=RequestMethod.GET)
    public String index() {
        return "index";
    }
}
```

This controller is concise and simple, but there's plenty going on under the hood. Let's break it down step by step.

The `@Controller` annotation signals that this class contains web application paths.

The `@RequestMapping` annotation ensures that HTTP requests to `/` are mapped to the `index()` method.

> **Note:** In this example, the application only handles `GET`.

The implementation of the method body returns the string `index`, signaling the name of the view that needs to be rendered.

### Create a web page template

The web controller returns the string `index` when someone does `GET /` on your web site. Spring Boot has automatically added Thymeleaf beans to the application context to convert this into a request for the Thymeleaf template located at `src/main/resources/templates/index.html`.

`src/main/resources/templates/index.html`
```html
<html>
	<body>
		Hello, world!
	</body>
</html>
```
    
This template has some very basic HTML elements and no actual Thymeleaf-specific code. You could [augment it as needed](http://www.thymeleaf.org/).

Make the application executable
-------------------------------

In this guide, you'll first make the application an executable JAR file. You package everything in a single, executable JAR, driven by a `main()` method. Along the way, you use Spring's support for embedding the [Tomcat][u-tomcat] servlet container as the HTTP runtime, instead of deploying to an external instance. Later on, you'll see how to build a [WAR][u-war] file and run it on a standard container.

### Create an application class

`src/main/java/hello/Application.java`
```java
package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

The `main()` method defers to the [`SpringApplication`][] helper class, providing `Application.class` as an argument to its `run()` method. This tells Spring to read the annotation metadata from `Application` and to manage it as a component in the [Spring application context][u-application-context].

The `@ComponentScan` annotation tells Spring to search recursively through the `hello` package and its children for classes marked directly or indirectly with Spring's [`@Component`][] annotation. This directive ensures that Spring finds and registers the `HelloController`, because it is marked with `@Controller`, which in turn is a kind of `@Component` annotation.

The [`@EnableAutoConfiguration`][] annotation switches on reasonable default behaviors based on the content of your classpath. For example, because the application depends on the embeddable version of Tomcat (tomcat-embed-core.jar), a Tomcat server is set up and configured with reasonable defaults on your behalf. And because the application also depends on Spring MVC (spring-webmvc.jar), a Spring MVC [`DispatcherServlet`][] is configured and registered for you — no `web.xml` necessary! Auto-configuration is a powerful, flexible mechanism. See the [API documentation][`@EnableAutoConfiguration`] for further details.

### Build an executable JAR

Now that your `Application` class is ready, you simply instruct the build system to create a single, executable jar containing everything. This makes it easy to ship, version, and deploy the service as an application throughout the development lifecycle, across different environments, and so forth.

```sh
$ ./gradlew clean build
```

Run the service
-------------------
Run your service at the command line:

```sh
$ ./gradlew clean build && java -jar build/libs/gs-convert-jar-to-war-0.1.0.jar
```


Logging output is displayed. The service should be up and running within a few seconds. With your browser, click on [http://localhost:8080](http://localhost:8080). You should see the "Hello, world!" text rendered by the template.

Create a WAR file
-------------------
The application you built up to this point is configured to generate a JAR artifact. To switch it to a WAR file, you need add another plugin to your `build.gradle`:

```groovy
apply plugin: 'war'
```

You also need to change the `jar` settings to `war` settings:

```groovy
war {
    baseName = 'gs-convert-jar-to-war'
    version =  '0.1.0'
}
```

This signals Gradle to build a WAR. Here is the new version of the build.gradle:

`build.gradle`
```gradle
buildscript {
    repositories {
        maven { url "http://repo.spring.io/libs-snapshot" }
        mavenLocal()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:0.5.0.M4")
    }
}

apply plugin: 'java'
apply plugin: 'eclipse'
apply plugin: 'idea'
apply plugin: 'spring-boot'
apply plugin: 'war'

war {
    baseName = 'gs-convert-jar-to-war'
    version =  '0.1.0'
}

repositories {
    mavenCentral()
    maven { url "http://repo.spring.io/libs-snapshot" }
}

dependencies {
    compile("org.springframework.boot:spring-boot-starter-web:0.5.0.M4")
    compile("org.thymeleaf:thymeleaf-spring3:2.0.16")
    testCompile("junit:junit:4.11")
}

task wrapper(type: Wrapper) {
    gradleVersion = '1.8'
}
```
    
Now when you run `./gradlew clean build` a WAR file is created, as you'll see below.

Initialize the servlet
------------------------
Previously, the application contained a `public static void main()` method which the **spring-boot-gradle-plugin** was configured to run when using the `java -jar` command.

By converting this into a WAR file with no XML files, you need a different signal to the servlet container on how to launch the application.

`src/main/java/hello/HelloWebXml.java`
```java
package hello;

import org.springframework.boot.web.SpringBootServletInitializer;

public class HelloWebXml extends SpringBootServletInitializer {
    
    @Override
    protected Class<?>[] getConfigClasses() {
        return new Class<?>[]{ Application.class };
    }

}
```
    
`HelloWebXml` is a pure Java class that provides an alternative to creating a `web.xml`. It extends the `SpringServletInitializer` class. This extension offers many configurable options by overriding methods. But one required method is `getConfigClasses()`.

`getConfigClasses()` returns an array of classes that are needed to launch the application. This is where you supply a handle to your `Application` configuration. Remember: `Application` has the `@ComponentScan`, so it will find the web controller automatically.

Even though `public static void main()` is no longer needed, you can leave that code in place.

> **Note:** If you didn't use `@ComponentScan`, you would either need to add all other components manually as `@Bean`s or include the other components inside `getConfigClasses()`.

Run the WAR file
--------------------

Once `Application` is loaded, it will trigger Spring Boot to automatically configure other beans. In this example, it adds the the Spring MVC beans and Thymeleaf beans. But Spring Boot adds other beans driven by a combination factors such as what's on your classpath as well as other settings in the application context.

At this stage, you are ready to build a WAR file.

```sh
$ ./gradlew clean build
```
    
This command creates **build/libs/gs-convert-jar-to-war-0.1.0.war**, a deployable artifact.
    
You can download [Tomcat 7.0.42](http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.42/bin/) (the same version Spring Boot currently uses), Jetty, or any other container, as long as it has servlet 3.0 support. Unpack it and drop the WAR file in the proper directory. Then start the server.

If you are using [Spring Tool Suite](http://spring.io/tools/sts/all) to develop your application, you can use its built-in support for **tc Server v2.9**. 
- Drag the entire application's root folder down to the server instance. 
- Click the `Start` button to start the app.
- You should see tc Server logging appear in one of the console windows.
- Right-click on the app, and select `Open Home Page`. A browser tab inside STS should open up and display the "Hello, world!" text.

That means you can then either navigate to:
- [http://localhost:8080/gs-convert-jar-to-war/](http://localhost:8080/gs-convert-jar-to-war/) from STS
- [http://localhost:8080/gs-convert-jar-to-war-0.1.0/](http://localhost:8080/gs-convert-jar-to-war-0.1.0/) if you deployed to a separate instance of Tomcat

From there you will see the "Hello, world!" text.


Summary
-------

Congratulations! You've just converted an executable JAR application into a WAR-file based application that can be run in any servlet 3.0+ container.


[u-war]: /understanding/WAR
[u-tomcat]: /understanding/Tomcat
[u-application-context]: /understanding/application-context
[`@Controller`]: http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/stereotype/Controller.html
[`SpringApplication`]: http://docs.spring.io/spring-boot/docs/0.5.0.M3/api/org/springframework/boot/SpringApplication.html
[`@EnableAutoConfiguration`]: http://docs.spring.io/spring-boot/docs/0.5.0.M3/api/org/springframework/boot/autoconfigure/EnableAutoConfiguration.html
[`@Component`]: http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/stereotype/Component.html
[`@ResponseBody`]: http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/ResponseBody.html
[`MappingJackson2HttpMessageConverter`]: http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/http/converter/json/MappingJackson2HttpMessageConverter.html
[`DispatcherServlet`]: http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/DispatcherServlet.html
