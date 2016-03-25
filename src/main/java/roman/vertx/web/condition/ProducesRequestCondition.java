package roman.vertx.web.condition;

import io.vertx.ext.web.Route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import roman.vertx.web.bind.annotation.RequestMapping;
import roman.vertx.web.http.MediaType;

/**
 * A logical disjunction (' || ') request condition to match a request's
 * 'Accept' header to a list of media type expressions. Two kinds of media type
 * expressions are supported, which are described in
 * {@link RequestMapping#produces()} and {@link RequestMapping#headers()} where
 * the header name is 'Accept'. Regardless of which syntax is used, the
 * semantics are the same.
 *
 * @author RomanLuo
 * @email 530827804@qq.com
 * @date 2016年3月17日 下午2:02:26
 */
public final class ProducesRequestCondition extends AbstractRequestCondition<ProducesRequestCondition> {

	private final List<MediaType> mediaTypes;

	/**
	 * Same as {@link #ProducesRequestCondition(String[])} but also
	 * 
	 * @param produces
	 *            expressions with syntax defined by
	 *            {@link RequestMapping#produces()}
	 */
	public ProducesRequestCondition(String... produces) {
		this.mediaTypes = new ArrayList<MediaType>(parseExpressions(produces));
		Collections.sort(this.mediaTypes);
	}

	private Set<MediaType> parseExpressions(String[] produces) {
		Set<MediaType> result = new LinkedHashSet<MediaType>();
		if (produces != null) {
			for (String produce : produces) {
				result.add(MediaType.parseMediaType(produce));
			}
		}
		return result;
	}

	@Override
	protected List<MediaType> getContent() {
		return this.mediaTypes;
	}

	@Override
	protected String getToStringInfix() {
		return " || ";
	}

	/**
	 * Returns the "other" instance if it has any expressions; returns "this"
	 * instance otherwise. Practically that means a method-level "produces"
	 * overrides a type-level "produces" condition.
	 */
	@Override
	public ProducesRequestCondition combine(ProducesRequestCondition other) {
		return (!other.mediaTypes.isEmpty() ? other : this);
	}

	@Override
	public Route Router(Route route) {
		for (MediaType mediaType : mediaTypes) {
			route.produces(mediaType.toString());
		}
		return route;
	}

}
