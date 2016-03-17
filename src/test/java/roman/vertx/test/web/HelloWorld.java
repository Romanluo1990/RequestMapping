package roman.vertx.test.web;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import org.springframework.stereotype.Controller;

import roman.vertx.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/hello")
public class HelloWorld {

	@RequestMapping("test")
	public void hello(HttpServerRequest request, HttpServerResponse httpServerResponse) {
		httpServerResponse.end("hello world");
	}

}
