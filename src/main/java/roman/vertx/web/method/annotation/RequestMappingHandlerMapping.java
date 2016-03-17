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

package roman.vertx.web.method.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringValueResolver;

import roman.vertx.web.bind.annotation.RequestMapping;
import roman.vertx.web.condition.ConsumesRequestCondition;
import roman.vertx.web.condition.HeadersRequestCondition;
import roman.vertx.web.condition.ParamsRequestCondition;
import roman.vertx.web.condition.PatternsRequestCondition;
import roman.vertx.web.condition.ProducesRequestCondition;
import roman.vertx.web.condition.RequestMethodsRequestCondition;
import roman.vertx.web.handler.AbstractHandlerMapping;
import roman.vertx.web.method.RequestMappingInfo;
import roman.vertx.web.method.RequestMappingInfoHandlerMethodMappingNamingStrategy;

/**
 * Creates {@link RequestMappingInfo} instances from type and method-level
 * {@link RequestMapping @RequestMapping} annotations in {@link Controller
 * @Controller} classes.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @since 3.1
 */
public class RequestMappingHandlerMapping extends AbstractHandlerMapping<RequestMappingInfo> implements EmbeddedValueResolverAware {

	private boolean useSuffixPatternMatch = true;

	private boolean useRegisteredSuffixPatternMatch = false;

	private boolean useTrailingSlashMatch = true;

	private final List<String> fileExtensions = new ArrayList<String>();

	private StringValueResolver embeddedValueResolver;

	public RequestMappingHandlerMapping() {
		setHandlerMethodMappingNamingStrategy(new RequestMappingInfoHandlerMethodMappingNamingStrategy());
	}

	/**
	 * Whether to use suffix pattern match (".*") when matching patterns to
	 * requests. If enabled a method mapped to "/users" also matches to
	 * "/users.*".
	 * <p>
	 * The default value is {@code true}.
	 * <p>
	 * Also see {@link #setUseRegisteredSuffixPatternMatch(boolean)} for more
	 * fine-grained control over specific suffixes to allow.
	 */
	public void setUseSuffixPatternMatch(boolean useSuffixPatternMatch) {
		this.useSuffixPatternMatch = useSuffixPatternMatch;
	}

	/**
	 * Whether to use suffix pattern match for registered file extensions only
	 * when matching patterns to requests.
	 * <p>
	 * If enabled, a controller method mapped to "/users" also matches to
	 * "/users.json" assuming ".json" is a file extension registered with the
	 * provided {@link #setContentNegotiationManager(ContentNegotiationManager)
	 * contentNegotiationManager}. This can be useful for allowing only specific
	 * URL extensions to be used as well as in cases where a "." in the URL path
	 * can lead to ambiguous interpretation of path variable content, (e.g.
	 * given "/users/{user}" and incoming URLs such as "/users/john.j.joe" and
	 * "/users/john.j.joe.json").
	 * <p>
	 * If enabled, this flag also enables
	 * {@link #setUseSuffixPatternMatch(boolean) useSuffixPatternMatch}. The
	 * default value is {@code false}.
	 */
	public void setUseRegisteredSuffixPatternMatch(boolean useRegisteredSuffixPatternMatch) {
		this.useRegisteredSuffixPatternMatch = useRegisteredSuffixPatternMatch;
		this.useSuffixPatternMatch = (useRegisteredSuffixPatternMatch || this.useSuffixPatternMatch);
	}

	/**
	 * Whether to match to URLs irrespective of the presence of a trailing
	 * slash. If enabled a method mapped to "/users" also matches to "/users/".
	 * <p>
	 * The default value is {@code true}.
	 */
	public void setUseTrailingSlashMatch(boolean useTrailingSlashMatch) {
		this.useTrailingSlashMatch = useTrailingSlashMatch;
	}

	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.embeddedValueResolver = resolver;
	}

	/**
	 * Whether to use suffix pattern matching.
	 */
	public boolean useSuffixPatternMatch() {
		return this.useSuffixPatternMatch;
	}

	/**
	 * Whether to use registered suffixes for pattern matching.
	 */
	public boolean useRegisteredSuffixPatternMatch() {
		return this.useRegisteredSuffixPatternMatch;
	}

	/**
	 * Whether to match to URLs irrespective of the presence of a trailing
	 * slash.
	 */
	public boolean useTrailingSlashMatch() {
		return this.useTrailingSlashMatch;
	}

	/**
	 * Return the file extensions to use for suffix pattern matching.
	 */
	public List<String> getFileExtensions() {
		return this.fileExtensions;
	}

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
		if (methodAnnotation != null) {
			info = createRequestMappingInfo(methodAnnotation);
			RequestMapping typeAnnotation = AnnotationUtils.findAnnotation(handlerType, RequestMapping.class);
			if (typeAnnotation != null) {
				info = createRequestMappingInfo(typeAnnotation).combine(info);
			}
		}
		return info;
	}

	/**
	 * Created a RequestMappingInfo from a RequestMapping annotation.
	 */
	protected RequestMappingInfo createRequestMappingInfo(RequestMapping annotation) {
		String[] patterns = resolveEmbeddedValuesInPatterns(annotation.value());
		return new RequestMappingInfo(annotation.name(), new PatternsRequestCondition(patterns, getPathMatcher(), this.useSuffixPatternMatch, this.useTrailingSlashMatch, this.fileExtensions),
				new RequestMethodsRequestCondition(annotation.method()), new ParamsRequestCondition(annotation.params()), new HeadersRequestCondition(annotation.headers()),
				new ConsumesRequestCondition(annotation.consumes(), annotation.headers()), new ProducesRequestCondition(annotation.produces(), annotation.headers()));
	}

	/**
	 * Resolve placeholder values in the given array of patterns.
	 * 
	 * @return a new array with updated patterns
	 */
	protected String[] resolveEmbeddedValuesInPatterns(String[] patterns) {
		if (this.embeddedValueResolver == null) {
			return patterns;
		} else {
			String[] resolvedPatterns = new String[patterns.length];
			for (int i = 0; i < patterns.length; i++) {
				resolvedPatterns[i] = this.embeddedValueResolver.resolveStringValue(patterns[i]);
			}
			return resolvedPatterns;
		}
	}

	/**
	 * Get the URL path patterns associated with this {@link RequestMappingInfo}
	 * .
	 */
	@Override
	protected Set<String> getMappingPathPatterns(RequestMappingInfo info) {
		return info.getPatternsCondition().getPatterns();
	}
}
