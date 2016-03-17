/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package roman.vertx.web.handler;

import io.vertx.ext.web.Router;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.PathMatcher;
import org.springframework.util.ReflectionUtils.MethodFilter;

import roman.vertx.web.method.HandlerMethod;
import roman.vertx.web.method.HandlerMethodSelector;

public abstract class AbstractHandlerMapping<T> extends ApplicationObjectSupport implements Ordered, InitializingBean {

	private int order = Integer.MAX_VALUE; // default: same as non-Ordered

	private static final String SCOPED_TARGET_NAME_PREFIX = "scopedTarget.";

	private boolean detectHandlerMethodsInAncestorContexts = false;

	private HandlerMethodMappingNamingStrategy<T> namingStrategy;

	private final Map<T, HandlerMethod> handlerMethods = new LinkedHashMap<T, HandlerMethod>();

	private final MultiValueMap<String, HandlerMethod> nameMap = new LinkedMultiValueMap<String, HandlerMethod>();

	private Object defaultHandler;

	private PathMatcher pathMatcher = new AntPathMatcher();

	private Router router;

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
	 * Configure the naming strategy to use for assigning a default name to
	 * every mapped handler method.
	 *
	 * @param namingStrategy
	 *            strategy to use.
	 */
	public void setHandlerMethodMappingNamingStrategy(HandlerMethodMappingNamingStrategy<T> namingStrategy) {
		this.namingStrategy = namingStrategy;
	}

	/**
	 * Return a map with all handler methods and their mappings.
	 */
	public Map<T, HandlerMethod> getHandlerMethods() {
		return Collections.unmodifiableMap(this.handlerMethods);
	}

	/**
	 * Return the handler methods mapped to the mapping with the given name.
	 * 
	 * @param mappingName
	 *            the mapping name
	 */
	public List<HandlerMethod> getHandlerMethodsForMappingName(String mappingName) {
		return this.nameMap.get(mappingName);
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
		router = getApplicationContext().getBean(Router.class);
		if (router == null) {
			throw new IllegalStateException("The Router bean does not exist!");
		}
		for (String beanName : beanNames) {
			if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX) && isHandler(getApplicationContext().getType(beanName))) {
				detectHandlerMethods(beanName);
			}
		}
		handlerMethodsInitialized(getHandlerMethods());
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
		final Map<Method, T> mappings = new IdentityHashMap<Method, T>();
		final Class<?> userType = ClassUtils.getUserClass(handlerType);

		Set<Method> methods = HandlerMethodSelector.selectMethods(userType, new MethodFilter() {
			@Override
			public boolean matches(Method method) {
				T mapping = getMappingForMethod(method, userType);
				if (mapping != null) {
					mappings.put(method, mapping);
					return true;
				} else {
					return false;
				}
			}
		});

		for (Method method : methods) {
			registerHandlerMethod(handler, method, mappings.get(method));
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
	protected abstract T getMappingForMethod(Method method, Class<?> handlerType);

	/**
	 * Register a handler method and its unique mapping.
	 * 
	 * @param handler
	 *            the bean name of the handler or the handler instance
	 * @param method
	 *            the method to register
	 * @param mapping
	 *            the mapping conditions associated with the handler method
	 * @throws IllegalStateException
	 *             if another method was already registered under the same
	 *             mapping
	 */
	protected void registerHandlerMethod(Object handler, Method method, T mapping) {
		HandlerMethod newHandlerMethod = createHandlerMethod(handler, method);
		HandlerMethod oldHandlerMethod = this.handlerMethods.get(mapping);
		if (oldHandlerMethod != null && !oldHandlerMethod.equals(newHandlerMethod)) {
			throw new IllegalStateException("Ambiguous mapping found. Cannot map '" + newHandlerMethod.getBean() + "' bean method \n" + newHandlerMethod + "\nto " + mapping + ": There is already '"
					+ oldHandlerMethod.getBean() + "' bean method\n" + oldHandlerMethod + " mapped.");
		}

		this.handlerMethods.put(mapping, newHandlerMethod);
		if (logger.isInfoEnabled()) {
			logger.info("Mapped \"" + mapping + "\" onto " + newHandlerMethod);
		}

		Set<String> patterns = getMappingPathPatterns(mapping);
		for (String pattern : patterns) {
			if (getPathMatcher().isPattern(pattern)) {
				router.routeWithRegex(pattern);
			} else {
				router.route(pattern).handler(r -> {
					try {
						method.invoke(getApplicationContext().getBean(handler.toString()), r.request(), r.response());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
		}

		if (this.namingStrategy != null) {
			String name = this.namingStrategy.getName(newHandlerMethod, mapping);
			updateNameMap(name, newHandlerMethod);
		}
	}

	private void updateNameMap(String name, HandlerMethod newHandlerMethod) {

		List<HandlerMethod> handlerMethods = this.nameMap.get(name);
		if (handlerMethods != null) {
			for (HandlerMethod handlerMethod : handlerMethods) {
				if (handlerMethod.getMethod().equals(newHandlerMethod.getMethod())) {
					logger.trace("Mapping name already registered. Multiple controller instances perhaps?");
					return;
				}
			}
		}

		logger.trace("Mapping name=" + name);
		this.nameMap.add(name, newHandlerMethod);

		if (this.nameMap.get(name).size() > 1) {
			if (logger.isDebugEnabled()) {
				logger.debug("Mapping name clash for handlerMethods=" + this.nameMap.get(name) + ". Consider assigning explicit names.");
			}
		}
	}

	/**
	 * Create the HandlerMethod instance.
	 * 
	 * @param handler
	 *            either a bean name or an actual handler instance
	 * @param method
	 *            the target method
	 * @return the created HandlerMethod
	 */
	protected HandlerMethod createHandlerMethod(Object handler, Method method) {
		HandlerMethod handlerMethod;
		if (handler instanceof String) {
			String beanName = (String) handler;
			handlerMethod = new HandlerMethod(beanName, getApplicationContext(), method);
		} else {
			handlerMethod = new HandlerMethod(handler, method);
		}
		return handlerMethod;
	}

	/**
	 * Extract and return the URL paths contained in a mapping.
	 */
	protected abstract Set<String> getMappingPathPatterns(T mapping);

	/**
	 * Invoked after all handler methods have been detected.
	 * 
	 * @param handlerMethods
	 *            a read-only map with handler methods and mappings.
	 */
	protected void handlerMethodsInitialized(Map<T, HandlerMethod> handlerMethods) {
	}

	/**
	 * Set the default handler for this handler mapping. This handler will be
	 * returned if no specific mapping was found.
	 * <p>
	 * Default is {@code null}, indicating no default handler.
	 */
	public void setDefaultHandler(Object defaultHandler) {
		this.defaultHandler = defaultHandler;
	}

	/**
	 * Return the default handler for this handler mapping, or {@code null} if
	 * none.
	 */
	public Object getDefaultHandler() {
		return this.defaultHandler;
	}

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
