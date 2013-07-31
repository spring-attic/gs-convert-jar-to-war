
What you'll build
-----------------
This guide walks you through the process of converting a runnable JAR application that was built with [Spring Boot](https://github.com/SpringSource/spring-boot) into a WAR file that you can run in any standard servlet container.

What you'll need
----------------

 - About 15 minutes
 - A favorite text editor or IDE
 - [JDK 6][jdk] or later
 - [Maven 3.0][mvn] or later

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[mvn]: http://maven.apache.org/download.cgi


How to complete this guide
--------------------------

Like all Spring's [Getting Started guides](/guides/gs), you can start from scratch and complete each step, or you can bypass basic setup steps that are already familiar to you. Either way, you end up with working code.

To **start from scratch**, move on to [Set up the project](#scratch).

To **skip the basics**, do the following:

 - [Download][zip] and unzip the source repository for this guide, or clone it using [git](/understanding/git):
`git clone https://github.com/springframework-meta/gs-convert-jar-to-war.git`
 - cd into `gs-convert-jar-to-war/initial`.
 - Jump ahead to [Create a basic web application](#initial).

**When you're finished**, you can check your results against the code in `gs-convert-jar-to-war/complete`.
[zip]: https://github.com/springframework-meta/gs-convert-jar-to-war/archive/master.zip


<a name="scratch"></a>
Set up the project
------------------

First you set up a basic build script. You can use any build system you like when building apps with Spring, but the code you need to work with [Maven](https://maven.apache.org) and [Gradle](http://gradle.org) is included here. If you're not familiar with either, refer to [Building Java Projects with Maven](/guides/gs/maven/content) or [Building Java Projects with Gradle](/guides/gs/gradle/content).

### Create the directory structure

In a project directory of your choosing, create the following subdirectory structure; for example, with `mkdir -p src/main/java/hello` on *nix systems:

    └── src
        └── main
            └── java
                └── hello

### Create a Maven POM

`pom.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.springframework</groupId>
    <artifactId>gs-convert-jar-to-war</artifactId>
    <version>0.1.0</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>0.5.0.BUILD-SNAPSHOT</version>
    </parent>

	<properties>
		<start-class>hello.Application</start-class>
	</properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.thymeleaf</groupId>
            <artifactId>thymeleaf-spring3</artifactId>
        </dependency>
    </dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

    <!-- TODO: remove once bootstrap goes GA -->
    <repositories>
        <repository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>http://repo.springsource.org/milestone</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
        <repository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>http://repo.springsource.org/snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
    <pluginRepositories>
        <pluginRepository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>http://repo.springsource.org/milestone</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
        <pluginRepository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>http://repo.springsource.org/snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
</project>
```

This guide is using [Spring Boot's starter POMs](/guides/gs/spring-boot/content).

Note to experienced Maven users who are unaccustomed to using an external parent project: you can take it out later, it's just there to reduce the amount of code you have to write to get started.


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
    
This template has some very basic HTML elements and no actual Thymeleaf-specific code. You could [augment it as needed][gs-thymeleaf].

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
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
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

The [Spring Package maven plugin][spring-package-maven-plugin] collects all the jars on the classpath and builds a single "über-jar", which makes it more convenient to execute and transport your service.

Now run the following to produce a single executable JAR file containing all necessary dependency classes and resources:

```sh
$ mvn package
```

[spring-package-maven-plugin]: https://github.com/SpringSource/spring-zero/tree/master/spring-package-maven-plugin

Run the application
-------------------
Run your application with `java -jar` at the command line:

```sh
$ java -jar target/gs-convert-jar-to-war-0.1.0.jar
```


Logging output is displayed. The service should be up and running within a few seconds. With your browser, click on [http://localhost:8080](http://localhost:8080). You should see the "Hello, world!" text rendered by the template.

Create a WAR file
-------------------
The application you built up to this point is configured to generate a JAR artifact. To switch it to a WAR file, you add the artifact type at the top of your `pom.xml`:

```xml
    <packaging>war</packaging>
```

This signals Maven to proceed even though there is no web.xml anywhere in the project. You no longer need the `maven-shade-plugin` nor the `<properties></properties>` settings you had earlier. Here is the new version of the pom.xml:

`pom.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.springframework</groupId>
    <artifactId>gs-convert-jar-to-war</artifactId>
    <version>0.1.0</version>
    <packaging>war</packaging>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>0.5.0.BUILD-SNAPSHOT</version>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.thymeleaf</groupId>
            <artifactId>thymeleaf-spring3</artifactId>
        </dependency>
    </dependencies>

	<properties>
		<start-class>hello.Application</start-class>
	</properties>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

    <!-- TODO: remove once bootstrap goes GA -->
    <repositories>
        <repository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>http://repo.springsource.org/milestone</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
        <repository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>http://repo.springsource.org/snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
    <pluginRepositories>
        <pluginRepository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>http://repo.springsource.org/milestone</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
        <pluginRepository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>http://repo.springsource.org/snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
</project>
```

Initialize the servlet
------------------------
Previously, the application contained a `public static void main()` method which the maven-shade-plugin was configured to run when using the `java -jar` command.

By converting this into a WAR file with no XML files, you need a different signal to the servlet container on how to launch the application.

`src/main/java/hello/HelloWebXml.java`
```java
package hello;

import org.springframework.boot.web.SpringServletInitializer;

public class HelloWebXml extends SpringServletInitializer {
	
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
	$ mvn package
```
    
This command creates **target/gs-convert-jar-to-war-0.1.0.war**, a deployable artifact.
    
You can download [Tomcat 7.0.39](http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.39/bin/) (the same version Spring Boot currently uses), Jetty, or any other container, as long as it has servlet 3.0 support. Unpack it and drop the WAR file in the proper directory. Then start the server.

If you are using [Spring Tool Suite](http://www.springsource.org/sts) to develop your application, you can use its built-in support for **tc Server v2.9**. 
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


[gs-thymeleaf]: /gs/thymeleaf/content
[u-war]: /understanding/war
[u-tomcat]: /understanding/tomcat
[u-application-context]: /understanding/application-context
[`@Controller`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/stereotype/Controller.html
[`SpringApplication`]: http://static.springsource.org/spring-bootstrap/docs/0.5.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/bootstrap/SpringApplication.html
[`@EnableAutoConfiguration`]: http://static.springsource.org/spring-bootstrap/docs/0.5.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/bootstrap/context/annotation/SpringApplication.html
[`@Component`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/stereotype/Component.html
[`@ResponseBody`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/ResponseBody.html
[`MappingJackson2HttpMessageConverter`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/http/converter/json/MappingJackson2HttpMessageConverter.html
[`DispatcherServlet`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/web/servlet/DispatcherServlet.html
