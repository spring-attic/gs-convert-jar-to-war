<#assign project_id="gs-convert-jar-to-war">

This guide walks you through the process of converting a runnable JAR application that was built with [Spring Boot](https://github.com/SpringSource/spring-boot) into a [WAR][u-war] file that you can run in any standard servlet container.

What you'll build
-----------------
You'll take a simple Spring MVC web app and build a WAR file using Spring Boot.

What you'll need
----------------

 - About 15 minutes
 - <@prereq_editor_jdk_buildtools/>


## <@how_to_complete_this_guide jump_ahead="Create a basic web application"/>


<a name="scratch"></a>
Set up the project
------------------

<@build_system_intro/>

<@create_directory_structure_hello/>

### Create a Maven POM

    <@snippet path="pom.xml" prefix="initial"/>

<@bootstrap_starter_pom_disclaimer/>


<a name="initial"></a>
Create a basic web application
------------------------------
Now that you've set up the project, you can create a Spring MVC application. You'll get it to run using Spring Boot's embedded servlet container. Then, you'll modify things slightly to build a WAR file that can run in any servlet 3.0 container.


### Create a web controller

Spring MVC allows you to quickly build controllers for your web site.

    <@snippet path="src/main/java/hello/HelloController.java" prefix="initial"/>

This controller is concise and simple, but there's plenty going on under the hood. Let's break it down step by step.

The `@Controller` annotation signals that this class contains web application paths.

The `@RequestMapping` annotation ensures that HTTP requests to `/` are mapped to the `index()` method.

> **Note:** In this example, the application only handles `GET`.

The implementation of the method body returns the string `index`, signaling the name of the view that needs to be rendered.

### Create a web page template

The web controller returns the string `index` when someone does `GET /` on your web site. Spring Boot has automatically added Thymeleaf beans to the application context to convert this into a request for the Thymeleaf template located at `src/main/resources/templates/index.html`.

    <@snippet path="src/main/resources/templates/index.html" prefix="initial"/>
    
This template has some very basic HTML elements and no actual Thymeleaf-specific code. You could [augment it as needed](http://www.thymeleaf.org/).

Make the application executable
-------------------------------

In this guide, you'll first make the application an executable JAR file. You package everything in a single, executable JAR, driven by a `main()` method. Along the way, you use Spring's support for embedding the [Tomcat][u-tomcat] servlet container as the HTTP runtime, instead of deploying to an external instance. Later on, you'll see how to build a [WAR][u-war] file and run it on a standard container.

### Create an application class

    <@snippet path="src/main/java/hello/Application.java" prefix="initial"/>

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
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
```

The `start-class` property tells Maven to create a `META-INF/MANIFEST.MF` file with a `Main-Class: hello.Application` entry. This entry enables you to run it with `mvn spring-boot:run` (or simply run the jar itself with `java -jar`).

The [Spring Boot maven plugin][spring-boot-maven-plugin] collects all the jars on the classpath and builds a single "über-jar", which makes it more convenient to execute and transport your service.

Now run the following command to produce a single executable JAR file containing all necessary dependency classes and resources:

```sh
$ mvn package
```

[spring-boot-maven-plugin]: https://github.com/SpringSource/spring-boot/tree/master/spring-boot-tools/spring-boot-maven-plugin

<@run_the_application/>

Logging output is displayed. The service should be up and running within a few seconds. With your browser, click on [http://localhost:8080](http://localhost:8080). You should see the "Hello, world!" text rendered by the template.

Create a WAR file
-------------------
The application you built up to this point is configured to generate a JAR artifact. To switch it to a WAR file, you add the artifact type at the top of your `pom.xml`:

```xml
    <packaging>war</packaging>
```

This signals Maven to proceed even though there is no web.xml anywhere in the project. You no longer need the `maven-shade-plugin` nor the `<properties></properties>` settings you had earlier. Here is the new version of the pom.xml:

    <@snippet path="pom.xml" prefix="complete"/>

Initialize the servlet
------------------------
Previously, the application contained a `public static void main()` method which the **spring-boot-maven-plugin** was configured to run when using the `java -jar` command.

By converting this into a WAR file with no XML files, you need a different signal to the servlet container on how to launch the application.

    <@snippet path="src/main/java/hello/HelloWebXml.java" prefix="complete"/>
    
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
    
This command creates **target/${project_id}-0.1.0.war**, a deployable artifact.
    
You can download [Tomcat 7.0.42](http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.42/bin/) (the same version Spring Boot currently uses), Jetty, or any other container, as long as it has servlet 3.0 support. Unpack it and drop the WAR file in the proper directory. Then start the server.

If you are using [Spring Tool Suite](http://www.springsource.org/sts) to develop your application, you can use its built-in support for **tc Server v2.9**. 
- Drag the entire application's root folder down to the server instance. 
- Click the `Start` button to start the app.
- You should see tc Server logging appear in one of the console windows.
- Right-click on the app, and select `Open Home Page`. A browser tab inside STS should open up and display the "Hello, world!" text.

That means you can then either navigate to:
- [http://localhost:8080/${project_id}/](http://localhost:8080/${project_id}/) from STS
- [http://localhost:8080/${project_id}-0.1.0/](http://localhost:8080/${project_id}-0.1.0/) if you deployed to a separate instance of Tomcat

From there you will see the "Hello, world!" text.


Summary
-------

Congratulations! You've just converted an executable JAR application into a WAR-file based application that can be run in any servlet 3.0+ container.


[u-war]: /understanding/WAR
[u-tomcat]: /understanding/Tomcat
[u-application-context]: /understanding/application-context
[`@Controller`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/stereotype/Controller.html
[`SpringApplication`]: http://static.springsource.org/spring-bootstrap/docs/0.5.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/bootstrap/SpringApplication.html
[`@EnableAutoConfiguration`]: http://static.springsource.org/spring-bootstrap/docs/0.5.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/bootstrap/context/annotation/SpringApplication.html
[`@Component`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/stereotype/Component.html
[`@ResponseBody`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/ResponseBody.html
[`MappingJackson2HttpMessageConverter`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/http/converter/json/MappingJackson2HttpMessageConverter.html
[`DispatcherServlet`]: http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/web/servlet/DispatcherServlet.html
