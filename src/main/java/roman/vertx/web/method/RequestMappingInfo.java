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

import io.vertx.ext.web.Route;

import java.lang.reflect.Method;

import roman.vertx.web.condition.ConsumesRequestCondition;
import roman.vertx.web.condition.PatternRequestCondition;
import roman.vertx.web.condition.ProducesRequestCondition;
import roman.vertx.web.condition.RequestCondition;
import roman.vertx.web.condition.RequestMethodsRequestCondition;

/**
 * Encapsulates the following request mapping conditions:
 * <ol>
 * <li>{@link PatternRequestCondition}
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
public final class RequestMappingInfo implements RequestCondition<RequestMappingInfo> {

	private final Object object;

	private final Method method;

	private final PatternRequestCondition patternsCondition;

	private final RequestMethodsRequestCondition methodsCondition;

	private final ConsumesRequestCondition consumesCondition;

	private final ProducesRequestCondition producesCondition;

	public RequestMappingInfo(Object object, Method method, PatternRequestCondition patterns, RequestMethodsRequestCondition methods, ConsumesRequestCondition consumes,
			ProducesRequestCondition produces) {
		this.object = object;
		this.method = method;
		this.patternsCondition = (patterns != null ? patterns : new PatternRequestCondition());
		this.methodsCondition = (methods != null ? methods : new RequestMethodsRequestCondition());
		this.consumesCondition = (consumes != null ? consumes : new ConsumesRequestCondition());
		this.producesCondition = (produces != null ? produces : new ProducesRequestCondition());
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
		PatternRequestCondition patterns = this.patternsCondition.combine(other.patternsCondition);
		RequestMethodsRequestCondition methods = this.methodsCondition.combine(other.methodsCondition);
		ConsumesRequestCondition consumes = this.consumesCondition.combine(other.consumesCondition);
		ProducesRequestCondition produces = this.producesCondition.combine(other.producesCondition);

		return new RequestMappingInfo(object, method, patterns, methods, consumes, produces);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && obj instanceof RequestMappingInfo) {
			RequestMappingInfo other = (RequestMappingInfo) obj;
			return (this.patternsCondition.equals(other.patternsCondition) && this.methodsCondition.equals(other.methodsCondition) && this.consumesCondition.equals(other.consumesCondition) && this.producesCondition
					.equals(other.producesCondition));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (this.object.hashCode() + method.hashCode() + this.patternsCondition.hashCode() * 31 + // primary
				this.methodsCondition.hashCode() + this.consumesCondition.hashCode() + this.producesCondition.hashCode());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("{");
		builder.append(this.patternsCondition);
		builder.append(",methods=").append(this.methodsCondition);
		builder.append(",consumes=").append(this.consumesCondition);
		builder.append(",produces=").append(this.producesCondition);
		builder.append('}');
		return builder.toString();
	}

	@Override
	public Route Router(Route route) {
		route = consumesCondition.Router(consumesCondition.Router(methodsCondition.Router(patternsCondition.Router(route))));
		route.handler(r -> {
			try {
				method.invoke(object, r.request(), r.response());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		return route;
	}

}
