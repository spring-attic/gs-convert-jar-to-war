package hello;

import org.springframework.boot.web.SpringBootServletInitializer;

public class HelloWebXml extends SpringBootServletInitializer {
    
    @Override
    protected Class<?>[] getConfigClasses() {
        return new Class<?>[]{ Application.class };
    }

}
