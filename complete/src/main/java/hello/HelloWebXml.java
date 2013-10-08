package hello;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.SpringBootServletInitializer;

public class HelloWebXml extends SpringBootServletInitializer {
    
	@Override
	protected void configure(SpringApplicationBuilder application) {
        application.sources(Application.class);
    }

}
