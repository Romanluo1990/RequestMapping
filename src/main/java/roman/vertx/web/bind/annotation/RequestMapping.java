package roman.vertx.web.bind.annotation;

import io.vertx.core.http.HttpMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for mapping web requests onto specific handler classes and/or
 * handler methods. Provides a consistent style between Servlet and Portlet
 * environments, with the semantics adapting to the concrete environment.
 * 
 * @author RomanLuo
 * @email 530827804@qq.com
 * @date 2016年3月17日 下午2:05:50
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {

	String value() default "";

	HttpMethod[] method() default {};

	String[] consumes() default {};

	String[] produces() default {};

}
