package roman.vertx.web.condition;

import io.vertx.ext.web.Route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import roman.vertx.web.bind.annotation.RequestMapping;
import roman.vertx.web.http.MediaType;

/**
 * A logical disjunction (' || ') request condition to match a request's
 * 'Content-Type' header to a list of media type expressions. Two kinds of media
 * type expressions are supported, which are described in
 * {@link RequestMapping#consumes()} and {@link RequestMapping#headers()} where
 * the header name is 'Content-Type'. Regardless of which syntax is used, the
 * semantics are the same.
 *
 * @author RomanLuo
 * @email 530827804@qq.com
 * @date 2016年3月17日 下午2:04:27
 */
public final class ConsumesRequestCondition extends AbstractRequestCondition<ConsumesRequestCondition> {

	private final List<MediaType> mediaTypes;

	/**
	 * Creates a new instance from 0 or more "consumes" expressions.
	 * 
	 * @param consumes
	 *            expressions with the syntax described in
	 *            {@link RequestMapping#consumes()}; if 0 expressions are
	 *            provided, the condition will match to every request
	 */
	public ConsumesRequestCondition(String... consumes) {
		this(parseExpressions(consumes));
	}

	/**
	 * Private constructor accepting parsed media type expressions.
	 */
	private ConsumesRequestCondition(Collection<MediaType> mediaTypes) {
		this.mediaTypes = new ArrayList<MediaType>(mediaTypes);
		Collections.sort(this.mediaTypes);
	}

	private static Set<MediaType> parseExpressions(String[] consumes) {
		Set<MediaType> result = new LinkedHashSet<MediaType>();
		if (consumes != null) {
			for (String consume : consumes) {
				result.add(MediaType.parseMediaType(consume));
			}
		}
		return result;
	}

	@Override
	protected Collection<MediaType> getContent() {
		return this.mediaTypes;
	}

	@Override
	protected String getToStringInfix() {
		return " || ";
	}

	/**
	 * Returns the "other" instance if it has any expressions; returns "this"
	 * instance otherwise. Practically that means a method-level "consumes"
	 * overrides a type-level "consumes" condition.
	 */
	@Override
	public ConsumesRequestCondition combine(ConsumesRequestCondition other) {
		return !other.mediaTypes.isEmpty() ? other : this;
	}

	@Override
	public Route Router(Route route) {
		for (MediaType contentType : mediaTypes) {
			route.consumes(contentType.toString());
		}
		return route;
	}
}
