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

package roman.vertx.web.method;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

import java.lang.reflect.Method;

import org.springframework.util.StringUtils;

import roman.vertx.web.condition.ConsumesRequestCondition;
import roman.vertx.web.condition.HeadersRequestCondition;
import roman.vertx.web.condition.ParamsRequestCondition;
import roman.vertx.web.condition.PatternsRequestCondition;
import roman.vertx.web.condition.ProducesRequestCondition;
import roman.vertx.web.condition.RequestCondition;
import roman.vertx.web.condition.RequestMethodsRequestCondition;
import roman.vertx.web.handler.Handler;

/**
 * Encapsulates the following request mapping conditions:
 * <ol>
 * <li>{@link PatternsRequestCondition}
 * <li>{@link RequestMethodsRequestCondition}
 * <li>{@link ParamsRequestCondition}
 * <li>{@link HeadersRequestCondition}
 * <li>{@link ConsumesRequestCondition}
 * <li>{@link ProducesRequestCondition}
 * </ol>
 *
 * @author RomanLuo
 * @email 530827804@qq.com
 * @date 2016年3月17日 下午1:57:37
 */
public final class RequestMappingInfo implements RequestCondition<RequestMappingInfo>, Handler {

	private final Object object;

	private final Method method;

	private Router router;

	private final String name;

	private final PatternsRequestCondition patternsCondition;

	private final RequestMethodsRequestCondition methodsCondition;

	private final ParamsRequestCondition paramsCondition;

	private final HeadersRequestCondition headersCondition;

	private final ConsumesRequestCondition consumesCondition;

	private final ProducesRequestCondition producesCondition;

	public RequestMappingInfo(Object object, Method method, Router router, String name, PatternsRequestCondition patterns, RequestMethodsRequestCondition methods, ParamsRequestCondition params,
			HeadersRequestCondition headers, ConsumesRequestCondition consumes, ProducesRequestCondition produces) {
		this.object = object;
		this.method = method;
		this.router = router;
		this.name = (StringUtils.hasText(name) ? name : null);
		this.patternsCondition = (patterns != null ? patterns : new PatternsRequestCondition());
		this.methodsCondition = (methods != null ? methods : new RequestMethodsRequestCondition());
		this.paramsCondition = (params != null ? params : new ParamsRequestCondition());
		this.headersCondition = (headers != null ? headers : new HeadersRequestCondition());
		this.consumesCondition = (consumes != null ? consumes : new ConsumesRequestCondition());
		this.producesCondition = (produces != null ? produces : new ProducesRequestCondition());
	}

	/**
	 * Return the name for this mapping, or {@code null}.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the URL patterns of this {@link RequestMappingInfo}; or instance
	 * with 0 patterns, never {@code null}.
	 */
	public PatternsRequestCondition getPatternsCondition() {
		return this.patternsCondition;
	}

	/**
	 * Returns the HTTP request methods of this {@link RequestMappingInfo}; or
	 * instance with 0 request methods, never {@code null}.
	 */
	public RequestMethodsRequestCondition getMethodsCondition() {
		return this.methodsCondition;
	}

	/**
	 * Returns the "parameters" condition of this {@link RequestMappingInfo}; or
	 * instance with 0 parameter expressions, never {@code null}.
	 */
	public ParamsRequestCondition getParamsCondition() {
		return this.paramsCondition;
	}

	/**
	 * Returns the "headers" condition of this {@link RequestMappingInfo}; or
	 * instance with 0 header expressions, never {@code null}.
	 */
	public HeadersRequestCondition getHeadersCondition() {
		return this.headersCondition;
	}

	/**
	 * Returns the "consumes" condition of this {@link RequestMappingInfo}; or
	 * instance with 0 consumes expressions, never {@code null}.
	 */
	public ConsumesRequestCondition getConsumesCondition() {
		return this.consumesCondition;
	}

	/**
	 * Returns the "produces" condition of this {@link RequestMappingInfo}; or
	 * instance with 0 produces expressions, never {@code null}.
	 */
	public ProducesRequestCondition getProducesCondition() {
		return this.producesCondition;
	}

	/**
	 * Combines "this" request mapping info (i.e. the current instance) with
	 * another request mapping info instance.
	 * <p>
	 * Example: combine type- and method-level request mappings.
	 * 
	 * @return a new request mapping info instance; never {@code null}
	 */
	@Override
	public RequestMappingInfo combine(RequestMappingInfo other) {
		String name = combineNames(other);
		PatternsRequestCondition patterns = this.patternsCondition.combine(other.patternsCondition);
		RequestMethodsRequestCondition methods = this.methodsCondition.combine(other.methodsCondition);
		ParamsRequestCondition params = this.paramsCondition.combine(other.paramsCondition);
		HeadersRequestCondition headers = this.headersCondition.combine(other.headersCondition);
		ConsumesRequestCondition consumes = this.consumesCondition.combine(other.consumesCondition);
		ProducesRequestCondition produces = this.producesCondition.combine(other.producesCondition);

		return new RequestMappingInfo(object, method, router, name, patterns, methods, params, headers, consumes, produces);
	}

	private String combineNames(RequestMappingInfo other) {
		if (this.name != null && other.name != null) {
			String separator = "#";
			return this.name + separator + other.name;
		} else if (this.name != null) {
			return this.name;
		} else {
			return (other.name != null ? other.name : null);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && obj instanceof RequestMappingInfo) {
			RequestMappingInfo other = (RequestMappingInfo) obj;
			return (this.patternsCondition.equals(other.patternsCondition) && this.methodsCondition.equals(other.methodsCondition) && this.paramsCondition.equals(other.paramsCondition)
					&& this.headersCondition.equals(other.headersCondition) && this.consumesCondition.equals(other.consumesCondition) && this.producesCondition.equals(other.producesCondition));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (this.object.hashCode() + method.hashCode() + this.patternsCondition.hashCode() * 31 + // primary
				this.methodsCondition.hashCode() + this.paramsCondition.hashCode() + this.headersCondition.hashCode() + this.consumesCondition.hashCode() + this.producesCondition.hashCode());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("{");
		builder.append(this.patternsCondition);
		builder.append(",methods=").append(this.methodsCondition);
		builder.append(",params=").append(this.paramsCondition);
		builder.append(",headers=").append(this.headersCondition);
		builder.append(",consumes=").append(this.consumesCondition);
		builder.append(",produces=").append(this.producesCondition);
		builder.append('}');
		return builder.toString();
	}

	@Override
	public void handle() {
		for (Route route : patternsCondition.router(router)) {
			for (HttpMethod httpMethod : methodsCondition.getMethods()) {
				route.method(httpMethod).handler(r -> {
					try {
						method.invoke(object, r.request(), r.response());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
		}
	}

}
