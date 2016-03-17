package roman.vertx.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import roman.vertx.web.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class WebMvcConfiguration {

	@Bean
	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
		RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
		return handlerMapping;
	}

}
