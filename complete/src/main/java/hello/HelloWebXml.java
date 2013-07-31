package hello;

import org.springframework.boot.web.SpringServletInitializer;

public class HelloWebXml extends SpringServletInitializer {
	
	@Override
	protected Class<?>[] getConfigClasses() {
		return new Class<?>[]{ Application.class };
	}

}
