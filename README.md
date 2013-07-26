
What you'll build
-----------------
This guide will walk you through the process of converting a runnable JAR application into a WAR file you can run on any standard servlet container.

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
        <groupId>org.springframework.zero</groupId>
        <artifactId>spring-starter-parent</artifactId>
        <version>0.5.0.BUILD-SNAPSHOT</version>
    </parent>

	<properties>
		<start-class>hello.Application</start-class>
	</properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.zero</groupId>
            <artifactId>spring-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.thymeleaf</groupId>
            <artifactId>thymeleaf-spring3</artifactId>
        </dependency>
    </dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.zero</groupId>
				<artifactId>spring-package-maven-plugin</artifactId>
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

TODO: mention that we're using Spring Bootstrap's [_starter POMs_](../gs-bootstrap-starter) here.

Note to experienced Maven users who are unaccustomed to using an external parent project: you can take it out later, it's just there to reduce the amount of code you have to write to get started.


<a name="initial"></a>
Create a basic web application
------------------------------
Now that you've set up the basic project, you can create a Spring MVC application. You'll get it to run using Spring Zero's embedded servlet container. Then, you'll modify things slightly to build a WAR file that can run in any servlet 3.0 container.


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

The `@Controller` annotation signals that application that this class contains methods to map onto web links.

The `@RequestMapping` annotation ensures that HTTP requests to `/` are mapped to the `index()` method.

> **Note:** In this example, the application only handles `GET`.

The implementation of the method body returns the string `index`, signaling the name of the view that needs to be rendered.

### Creating a web page template

Yuur web controller up above wants to render `index` when someone does `GET /` on your web site. A simple HTML5 Thymeleaf template is located in `src/main/resources/templates/`.

`src/main/resources/templates/index.html`
```html
<html>
	<body>
		Hello, world!
	</body>
</html>
```
    
This template has some very basic HTML elements and no actual Thymeleaf-specific code. But if you wanted to, you could augment it as needed.

Make the application executable
-------------------------------

In this guide, you'll first make the application an executable JAR file with the following steps:

### Create an Application class

`src/main/java/hello/Application.java`
```java
package hello;

import org.springframework.autoconfigure.EnableAutoConfiguration;
import org.springframework.bootstrap.SpringApplication;
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

Add the following configuration to your existing Maven POM:

`pom.xml`
```xml
    <properties>
        <start-class>hello.Application</start-class>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.zero</groupId>
                <artifactId>spring-package-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
```

The `start-class` property tells Maven to create a `META-INF/MANIFEST.MF` file with a `Main-Class: hello.Application` entry. This entry enables you to run the jar with `java -jar`.

The [Spring Package maven plugin][spring-package-maven-plugin] collects all the jars on the classpath and builds a single "über-jar", which makes it more convenient to execute and transport your service.

Now run the following to produce a single executable JAR file containing all necessary dependency classes and resources:

```sh
$ mvn package
```

[spring-package-maven-plugin]: https://github.com/SpringSource/spring-zero/tree/master/spring-package-maven-plugin

> **Note:** The procedure above will create a runnable JAR. You can also opt to [build a classic WAR file](/guides/gs/convert-jar-to-war/content) instead.

Run the application
-------------------
Run your application with `java -jar` at the command line:

```sh
$ java -jar target/gs-convert-jar-to-war-0.1.0.jar
```


Logging output is displayed. The service should be up and running within a few seconds. With your browser, click on [http://localhost:8080](http://localhost:8080). You should see the "Hello, world!" text rendered by the template.

Creating a WAR file
-------------------
The application you built up to this point is configured to generate a JAR artifact. To switch it to a WAR file, you must add the artifact type at the top of your `pom.xml`:

```xml
    <packaging>war</packaging>
```

To support the fact that you are using servlet 3.0's web.xml-free version, you must add some extra settings through the `maven-war-plugin`:

```xml
    <build>
    	<plugins>
    		<plugin>
    			<groupId>org.apache.maven.plugins</groupId>
    			<artifactId>maven-war-plugin</artifactId>
    			<configuration>
    				<failOnMissingWebXml>false</failOnMissingWebXml>
    			</configuration>
    		</plugin>
    	</plugins>
    </build>
```

This signals maven to proceed even though there is no web.xml anywhere in the project. You no longer need the `maven-shade-plugin` nor the `<properties></properties>` settings you had earlier. Here is the new version of the pom.xml:

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
        <groupId>org.springframework.zero</groupId>
        <artifactId>spring-starter-parent</artifactId>
        <version>0.5.0.BUILD-SNAPSHOT</version>
    </parent>

	<properties>
		<start-class>hello.Application</start-class>
	</properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.zero</groupId>
            <artifactId>spring-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.thymeleaf</groupId>
            <artifactId>thymeleaf-spring3</artifactId>
        </dependency>
    </dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.zero</groupId>
				<artifactId>spring-package-maven-plugin</artifactId>
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

Initializing the servlet
------------------------
Previously, the application contained a `public static void main()` method which the maven-shade-plugin was configured to run when using the `java -jar` command.

By converting this into a WAR file with no XML files, you need a different signal to the servlet container on how to launch the application.

`src/main/java/hello/HelloWebXml.java`
```java
package hello;

import org.springframework.bootstrap.web.SpringServletInitializer;

public class HelloWebXml extends SpringServletInitializer {
	
	@Override
	protected Class<?>[] getConfigClasses() {
		return new Class<?>[]{ Application.class };
	}

}
```
    
`HelloWebXml` is a pure Java class that provides an alternative to creating a `web.xml`. It extends the `SpringServletInitializer`. This offers many configurable options by overriding methods. But one required method is `getConfigClasses()`.

`getConfigClasses()` returns an array of classes that are needed to launch the application. This is where you supply a handle to your `Application` configuration. Remember: `Application` has the `@ComponentScan`, so it will find the web controller automatically.

Even though `public static void main()` is no longer needed, you can leave that code in place.

> **Note:** If you didn't use `@ComponentScan`, you would either need to manually add all other components as `@Bean`s or include the other components inside `getConfigClasses()`.

Running the WAR file
--------------------

There is nothing else to do. Once `Application` gets loaded up, it will trigger Spring Zero to automatically configure the other beans, such as the Spring MVC ones, the Thymeleaf ones, and anything else you have added to your application.

At this stage, you are ready to build a WAR file.

    mvn package
    
That will create **target/gs-jar-to-war-complete-0.1.0.war**, a deployable artifact.
    
You can download [Tomcat 7.0.39](http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.39/bin/) (the same version Spring Zero currently uses), Jetty, or any other container, as long as it has servlet 3.0 support. Unpack it and drop the WAR file in the proper directory. Then start the server.

If you are using [Spring Tool Suite](http://www.springsource.org/sts) to develop your application, you can take advantage of it's built in support for **tc Server v2.9**. 
- Drag the entire application's root folder down to the server instance. 
- Click on the `Start` button, and it should fire up right away. 
- Then right click on the app, and select `Open Home Page`. It should open a browser tab and display the "Hello, world!" text.

Either way, you can then navigate to [http://localhost:8080/gs-jar-to-war-complete/](http://localhost:8080/gs-jar-to-war-complete/) to see the results.


Summary
-------

Congratulations! You've just converted an executable JAR application into a WAR-file based application that can be run on any servlet 3.0+ container.



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
