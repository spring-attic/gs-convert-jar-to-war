package hello;

import org.springframework.bootstrap.web.SpringServletInitializer;

public class HelloWebXml extends SpringServletInitializer {
	
	@Override
	protected Class<?>[] getConfigClasses() {
		return new Class<?>[]{ Application.class };
	}

}
