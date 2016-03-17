package roman.vertx.test;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.impl.RouterImpl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import roman.vertx.web.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
public class App {

	@Bean
	public Vertx createVertx() {
		return Vertx.factory.vertx();
	}

	@Bean
	public Router createRouter(Vertx vertx) {
		return new RouterImpl(vertx);
	}

	@Bean
	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
		return new RequestMappingHandlerMapping();
	}

	public static void main(String[] args) {
		ApplicationContext applicationContext = SpringApplication.run(App.class, args);
		Vertx vertx = applicationContext.getBean(Vertx.class);
		Router router = applicationContext.getBean(Router.class);
		router.route("/test2").method(HttpMethod.GET).handler(r -> {
			r.response().end("222");
		});
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	}
}
