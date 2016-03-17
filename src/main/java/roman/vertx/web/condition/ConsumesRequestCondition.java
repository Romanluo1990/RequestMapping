package roman.vertx.web.condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import roman.vertx.web.bind.annotation.RequestMapping;
import roman.vertx.web.condition.HeadersRequestCondition.HeaderExpression;
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

	private final List<ConsumeMediaTypeExpression> expressions;

	/**
	 * Creates a new instance from 0 or more "consumes" expressions.
	 * 
	 * @param consumes
	 *            expressions with the syntax described in
	 *            {@link RequestMapping#consumes()}; if 0 expressions are
	 *            provided, the condition will match to every request
	 */
	public ConsumesRequestCondition(String... consumes) {
		this(consumes, null);
	}

	/**
	 * Creates a new instance with "consumes" and "header" expressions. "Header"
	 * expressions where the header name is not 'Content-Type' or have no header
	 * value defined are ignored. If 0 expressions are provided in total, the
	 * condition will match to every request
	 * 
	 * @param consumes
	 *            as described in {@link RequestMapping#consumes()}
	 * @param headers
	 *            as described in {@link RequestMapping#headers()}
	 */
	public ConsumesRequestCondition(String[] consumes, String[] headers) {
		this(parseExpressions(consumes, headers));
	}

	/**
	 * Private constructor accepting parsed media type expressions.
	 */
	private ConsumesRequestCondition(Collection<ConsumeMediaTypeExpression> expressions) {
		this.expressions = new ArrayList<ConsumeMediaTypeExpression>(expressions);
		Collections.sort(this.expressions);
	}

	private static Set<ConsumeMediaTypeExpression> parseExpressions(String[] consumes, String[] headers) {
		Set<ConsumeMediaTypeExpression> result = new LinkedHashSet<ConsumeMediaTypeExpression>();
		if (headers != null) {
			for (String header : headers) {
				HeaderExpression expr = new HeaderExpression(header);
				if ("Content-Type".equalsIgnoreCase(expr.name)) {
					for (MediaType mediaType : MediaType.parseMediaTypes(expr.value)) {
						result.add(new ConsumeMediaTypeExpression(mediaType, expr.isNegated));
					}
				}
			}
		}
		if (consumes != null) {
			for (String consume : consumes) {
				result.add(new ConsumeMediaTypeExpression(consume));
			}
		}
		return result;
	}

	/**
	 * Return the contained MediaType expressions.
	 */
	public Set<MediaTypeExpression> getExpressions() {
		return new LinkedHashSet<MediaTypeExpression>(this.expressions);
	}

	/**
	 * Returns the media types for this condition excluding negated expressions.
	 */
	public Set<MediaType> getConsumableMediaTypes() {
		Set<MediaType> result = new LinkedHashSet<MediaType>();
		for (ConsumeMediaTypeExpression expression : this.expressions) {
			if (!expression.isNegated()) {
				result.add(expression.getMediaType());
			}
		}
		return result;
	}

	/**
	 * Whether the condition has any media type expressions.
	 */
	public boolean isEmpty() {
		return this.expressions.isEmpty();
	}

	@Override
	protected Collection<ConsumeMediaTypeExpression> getContent() {
		return this.expressions;
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
		return !other.expressions.isEmpty() ? other : this;
	}

	/**
	 * Parses and matches a single media type expression to a request's
	 * 'Content-Type' header.
	 */
	static class ConsumeMediaTypeExpression extends AbstractMediaTypeExpression {

		ConsumeMediaTypeExpression(String expression) {
			super(expression);
		}

		ConsumeMediaTypeExpression(MediaType mediaType, boolean negated) {
			super(mediaType, negated);
		}

	}

}
