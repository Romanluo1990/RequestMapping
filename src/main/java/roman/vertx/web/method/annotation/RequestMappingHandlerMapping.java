package roman.vertx.web.method.annotation;

import io.vertx.ext.web.Router;

import java.lang.reflect.Method;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;

import roman.vertx.web.bind.annotation.RequestMapping;
import roman.vertx.web.condition.ConsumesRequestCondition;
import roman.vertx.web.condition.PatternRequestCondition;
import roman.vertx.web.condition.ProducesRequestCondition;
import roman.vertx.web.condition.RequestMethodsRequestCondition;
import roman.vertx.web.handler.AbstractHandlerMapping;
import roman.vertx.web.method.RequestMappingInfo;

/**
 * Creates {@link RequestMappingInfo} instances from type and method-level
 * {@link RequestMapping @RequestMapping} annotations in {@link Controller
 * @Controller} classes.
 *
 * @author RomanLuo
 * @email  530827804@qq.com  
 * @date   2016年3月17日 下午1:59:57 
 */
public class RequestMappingHandlerMapping extends AbstractHandlerMapping {

	/**
	 * {@inheritDoc} Expects a handler to have a type-level @{@link Controller}
	 * annotation.
	 */
	@Override
	protected boolean isHandler(Class<?> beanType) {
		return ((AnnotationUtils.findAnnotation(beanType, Controller.class) != null) || (AnnotationUtils.findAnnotation(beanType, RequestMapping.class) != null));
	}

	/**
	 * Uses method and type-level @{@link RequestMapping} annotations to create
	 * the RequestMappingInfo.
	 * 
	 * @return the created RequestMappingInfo, or {@code null} if the method
	 *         does not have a {@code @RequestMapping} annotation.
	 * @see #getCustomMethodCondition(Method)
	 * @see #getCustomTypeCondition(Class)
	 */
	@Override
	protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
		RequestMappingInfo info = null;
		RequestMapping methodAnnotation = AnnotationUtils.findAnnotation(method, RequestMapping.class);
		Object object = getApplicationContext().getBean(handlerType);
		if (methodAnnotation != null) {
			info = createRequestMappingInfo(object, method, methodAnnotation);
			RequestMapping typeAnnotation = AnnotationUtils.findAnnotation(handlerType, RequestMapping.class);
			if (typeAnnotation != null) {
				info = createRequestMappingInfo(object, method, typeAnnotation).combine(info);
			}
		}
		return info;
	}

	/**
	 * Created a RequestMappingInfo from a RequestMapping annotation.
	 */
	protected RequestMappingInfo createRequestMappingInfo(Object object, Method method, RequestMapping annotation) {
		String pattern = annotation.value();
		Router router = getApplicationContext().getBean(Router.class);
		if (router == null) {
			throw new IllegalStateException("The Router bean does not exist!");
		}
		return new RequestMappingInfo(object, method, new PatternRequestCondition(pattern, getPathMatcher()), new RequestMethodsRequestCondition(annotation.method()), new ConsumesRequestCondition(
				annotation.consumes()), new ProducesRequestCondition(annotation.produces()));
	}

}
