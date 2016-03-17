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

package roman.vertx.web.condition;


/**
 * The contract for request conditions in Spring MVC's mapping infrastructure.
 *
 * <p>
 * Request conditions can be combined via {@link #combine(Object)}, matched to a
 * request via {@link #getMatchingCondition(HttpServletRequest)}, and compared
 * to each other via {@link #compareTo(Object, HttpServletRequest)} to determine
 * which matches a request more closely.
 *
 * @param <T>
 *            the type of objects that this RequestCondition can be combined
 *            with and compared to
 *
 * @author Rossen Stoyanchev
 * @author Arjen Poutsma
 * @since 3.1
 */
public interface RequestCondition<T> {

	/**
	 * Defines the rules for combining this condition (i.e. the current
	 * instance) with another condition. For example combining type- and
	 * method-level {@link RequestMapping} conditions.
	 * 
	 * @param other
	 *            the condition to combine with.
	 * @return a request condition instance that is the result of combining the
	 *         two condition instances.
	 */
	T combine(T other);

}
