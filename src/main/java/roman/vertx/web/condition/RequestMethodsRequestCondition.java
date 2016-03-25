package roman.vertx.web.condition;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A logical disjunction (' || ') request condition that matches a request
 * against a set of {@link HttpMethod}s.
 *
 * @author RomanLuo
 * @email 530827804@qq.com
 * @date 2016年3月17日 下午2:01:44
 */
public final class RequestMethodsRequestCondition extends AbstractRequestCondition<RequestMethodsRequestCondition> {

	private final Set<HttpMethod> methods;

	/**
	 * Create a new instance with the given request methods.
	 * 
	 * @param requestMethods
	 *            0 or more HTTP request methods; if, 0 the condition will match
	 *            to every request
	 */
	public RequestMethodsRequestCondition(HttpMethod... requestMethods) {
		this(asList(requestMethods));
	}

	private RequestMethodsRequestCondition(Collection<HttpMethod> requestMethods) {
		this.methods = Collections.unmodifiableSet(new LinkedHashSet<HttpMethod>(requestMethods));
	}

	private static List<HttpMethod> asList(HttpMethod... requestMethods) {
		return (requestMethods != null ? Arrays.asList(requestMethods) : Collections.<HttpMethod> emptyList());
	}

	/**
	 * Returns all {@link RequestMethod}s contained in this condition.
	 */
	public Set<HttpMethod> getMethods() {
		return this.methods;
	}

	@Override
	protected Collection<HttpMethod> getContent() {
		return this.methods;
	}

	@Override
	protected String getToStringInfix() {
		return " || ";
	}

	/**
	 * Returns a new instance with a union of the HTTP request methods from
	 * "this" and the "other" instance.
	 */
	@Override
	public RequestMethodsRequestCondition combine(RequestMethodsRequestCondition other) {
		Set<HttpMethod> set = new LinkedHashSet<HttpMethod>(this.methods);
		set.addAll(other.methods);
		return new RequestMethodsRequestCondition(set);
	}

	@Override
	public Route Router(Route route) {
		for (HttpMethod method : methods) {
			route.method(method);
		}
		return route;
	}

}
