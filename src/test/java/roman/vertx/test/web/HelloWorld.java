package roman.vertx.test.web;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import org.springframework.stereotype.Controller;

import roman.vertx.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test")
public class HelloWorld {

	@RequestMapping(value = "hello/:value", method = HttpMethod.GET)
	public void hello(HttpServerRequest request, HttpServerResponse httpServerResponse) {
		httpServerResponse.end("hello " + request.getParam("value"));
	}

}
