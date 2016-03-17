package roman.vertx.web.condition;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import roman.vertx.web.bind.annotation.RequestMapping;

/**
 * A logical conjunction (' && ') request condition that matches a request
 * against a set of header expressions with syntax defined in
 * {@link RequestMapping#headers()}.
 *
 * <p>
 * Expressions passed to the constructor with header names 'Accept' or
 * 'Content-Type' are ignored. See {@link ConsumesRequestCondition} and
 * {@link ProducesRequestCondition} for those.
 *
 * @author RomanLuo
 * @email 530827804@qq.com
 * @date 2016年3月17日 下午2:04:09
 */
public final class HeadersRequestCondition extends AbstractRequestCondition<HeadersRequestCondition> {

	private final Set<HeaderExpression> expressions;

	/**
	 * Create a new instance from the given header expressions. Expressions with
	 * header names 'Accept' or 'Content-Type' are ignored. See
	 * {@link ConsumesRequestCondition} and {@link ProducesRequestCondition} for
	 * those.
	 * 
	 * @param headers
	 *            media type expressions with syntax defined in
	 *            {@link RequestMapping#headers()}; if 0, the condition will
	 *            match to every request
	 */
	public HeadersRequestCondition(String... headers) {
		this(parseExpressions(headers));
	}

	private HeadersRequestCondition(Collection<HeaderExpression> conditions) {
		this.expressions = Collections.unmodifiableSet(new LinkedHashSet<HeaderExpression>(conditions));
	}

	private static Collection<HeaderExpression> parseExpressions(String... headers) {
		Set<HeaderExpression> expressions = new LinkedHashSet<HeaderExpression>();
		if (headers != null) {
			for (String header : headers) {
				HeaderExpression expr = new HeaderExpression(header);
				if ("Accept".equalsIgnoreCase(expr.name) || "Content-Type".equalsIgnoreCase(expr.name)) {
					continue;
				}
				expressions.add(expr);
			}
		}
		return expressions;
	}

	/**
	 * Return the contained request header expressions.
	 */
	public Set<NameValueExpression<String>> getExpressions() {
		return new LinkedHashSet<NameValueExpression<String>>(this.expressions);
	}

	@Override
	protected Collection<HeaderExpression> getContent() {
		return this.expressions;
	}

	@Override
	protected String getToStringInfix() {
		return " && ";
	}

	/**
	 * Returns a new instance with the union of the header expressions from
	 * "this" and the "other" instance.
	 */
	@Override
	public HeadersRequestCondition combine(HeadersRequestCondition other) {
		Set<HeaderExpression> set = new LinkedHashSet<HeaderExpression>(this.expressions);
		set.addAll(other.expressions);
		return new HeadersRequestCondition(set);
	}

	/**
	 * Parses and matches a single header expression to a request.
	 */
	static class HeaderExpression extends AbstractNameValueExpression<String> {

		public HeaderExpression(String expression) {
			super(expression);
		}

		@Override
		protected boolean isCaseSensitiveName() {
			return false;
		}

		@Override
		protected String parseValue(String valueExpression) {
			return valueExpression;
		}

	}

}
