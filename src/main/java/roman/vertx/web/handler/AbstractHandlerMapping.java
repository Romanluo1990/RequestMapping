package roman.vertx.web.handler;

import io.vertx.ext.web.Router;

import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.ReflectionUtils.MethodFilter;

import roman.vertx.web.method.HandlerMethodSelector;
import roman.vertx.web.method.RequestMappingInfo;

/**
 * @author RomanLuo
 * @email 530827804@qq.com
 * @date 2016年3月17日 下午2:01:28
 */
public abstract class AbstractHandlerMapping extends ApplicationObjectSupport implements Ordered, InitializingBean {

	private int order = Integer.MAX_VALUE; // default: same as non-Ordered

	private static final String SCOPED_TARGET_NAME_PREFIX = "scopedTarget.";

	private boolean detectHandlerMethodsInAncestorContexts = false;

	private PathMatcher pathMatcher = new AntPathMatcher();

	/**
	 * Specify the order value for this HandlerMapping bean.
	 * <p>
	 * Default value is {@code Integer.MAX_VALUE}, meaning that it's
	 * non-ordered.
	 * 
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	public final void setOrder(int order) {
		this.order = order;
	}

	@Override
	public final int getOrder() {
		return this.order;
	}

	/**
	 * Whether to detect handler methods in beans in ancestor
	 * ApplicationContexts.
	 * <p>
	 * Default is "false": Only beans in the current ApplicationContext are
	 * considered, i.e. only in the context that this HandlerMapping itself is
	 * defined in (typically the current DispatcherServlet's context).
	 * <p>
	 * Switch this flag on to detect handler beans in ancestor contexts
	 * (typically the Spring root WebApplicationContext) as well.
	 */
	public void setDetectHandlerMethodsInAncestorContexts(boolean detectHandlerMethodsInAncestorContexts) {
		this.detectHandlerMethodsInAncestorContexts = detectHandlerMethodsInAncestorContexts;
	}

	/**
	 * Detects handler methods at initialization.
	 */
	@Override
	public void afterPropertiesSet() {
		initHandlerMethods();
	}

	/**
	 * Scan beans in the ApplicationContext, detect and register handler
	 * methods.
	 * 
	 * @see #isHandler(Class)
	 * @see #getMappingForMethod(Method, Class)
	 * @see #handlerMethodsInitialized(Map)
	 */
	protected void initHandlerMethods() {
		if (logger.isDebugEnabled()) {
			logger.debug("Looking for request mappings in application context: " + getApplicationContext());
		}

		String[] beanNames = (this.detectHandlerMethodsInAncestorContexts ? BeanFactoryUtils.beanNamesForTypeIncludingAncestors(getApplicationContext(), Object.class) : getApplicationContext()
				.getBeanNamesForType(Object.class));
		for (String beanName : beanNames) {
			if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX) && isHandler(getApplicationContext().getType(beanName))) {
				detectHandlerMethods(beanName);
			}
		}
	}

	/**
	 * Whether the given type is a handler with handler methods.
	 * 
	 * @param beanType
	 *            the type of the bean being checked
	 * @return "true" if this a handler type, "false" otherwise.
	 */
	protected abstract boolean isHandler(Class<?> beanType);

	/**
	 * Look for handler methods in a handler.
	 * 
	 * @param handler
	 *            the bean name of a handler or a handler instance
	 */
	protected void detectHandlerMethods(final Object handler) {
		Class<?> handlerType = (handler instanceof String ? getApplicationContext().getType((String) handler) : handler.getClass());

		// Avoid repeated calls to getMappingForMethod which would rebuild
		// RequestMappingInfo instances
		final Map<Method, RequestMappingInfo> mappings = new IdentityHashMap<Method, RequestMappingInfo>();
		final Class<?> userType = ClassUtils.getUserClass(handlerType);

		Set<Method> methods = HandlerMethodSelector.selectMethods(userType, new MethodFilter() {
			@Override
			public boolean matches(Method method) {
				RequestMappingInfo mapping = getMappingForMethod(method, userType);
				if (mapping != null) {
					mappings.put(method, mapping);
					return true;
				} else {
					return false;
				}
			}
		});

		for (Method method : methods) {
			registerHandlerMethod(mappings.get(method));
		}
	}

	/**
	 * Provide the mapping for a handler method. A method for which no mapping
	 * can be provided is not a handler method.
	 * 
	 * @param method
	 *            the method to provide a mapping for
	 * @param handlerType
	 *            the handler type, possibly a sub-type of the method's
	 *            declaring class
	 * @return the mapping, or {@code null} if the method is not mapped
	 */
	protected abstract RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType);

	protected void registerHandlerMethod(RequestMappingInfo handler) {
		handler.Router(getApplicationContext().getBean(Router.class).route());
	};

	/**
	 * Set the PathMatcher implementation to use for matching URL paths against
	 * registered URL patterns. Default is AntPathMatcher.
	 * 
	 * @see org.springframework.util.AntPathMatcher
	 */
	public void setPathMatcher(PathMatcher pathMatcher) {
		Assert.notNull(pathMatcher, "PathMatcher must not be null");
		this.pathMatcher = pathMatcher;
	}

	/**
	 * Return the PathMatcher implementation to use for matching URL paths
	 * against registered URL patterns.
	 */
	public PathMatcher getPathMatcher() {
		return this.pathMatcher;
	}

}
