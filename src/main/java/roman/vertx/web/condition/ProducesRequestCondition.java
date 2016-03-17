package roman.vertx.web.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import roman.vertx.web.bind.annotation.RequestMapping;
import roman.vertx.web.condition.HeadersRequestCondition.HeaderExpression;
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

	private final List<ProduceMediaTypeExpression> MEDIA_TYPE_ALL_LIST = Collections.singletonList(new ProduceMediaTypeExpression("*/*"));

	private final List<ProduceMediaTypeExpression> expressions;

	/**
	 * Creates a new instance from "produces" expressions. If 0 expressions are
	 * provided in total, this condition will match to any request.
	 * 
	 * @param produces
	 *            expressions with syntax defined by
	 *            {@link RequestMapping#produces()}
	 */
	public ProducesRequestCondition(String... produces) {
		this(produces, (String[]) null);
	}

	/**
	 * Same as {@link #ProducesRequestCondition(String[], String[])} but also
	 * accepting a {@link ContentNegotiationManager}.
	 * 
	 * @param produces
	 *            expressions with syntax defined by
	 *            {@link RequestMapping#produces()}
	 * @param headers
	 *            expressions with syntax defined by
	 *            {@link RequestMapping#headers()}
	 */
	public ProducesRequestCondition(String[] produces, String[] headers) {
		this.expressions = new ArrayList<ProduceMediaTypeExpression>(parseExpressions(produces, headers));
		Collections.sort(this.expressions);
	}

	private Set<ProduceMediaTypeExpression> parseExpressions(String[] produces, String[] headers) {
		Set<ProduceMediaTypeExpression> result = new LinkedHashSet<ProduceMediaTypeExpression>();
		if (headers != null) {
			for (String header : headers) {
				HeaderExpression expr = new HeaderExpression(header);
				if ("Accept".equalsIgnoreCase(expr.name)) {
					for (MediaType mediaType : MediaType.parseMediaTypes(expr.value)) {
						result.add(new ProduceMediaTypeExpression(mediaType, expr.isNegated));
					}
				}
			}
		}
		if (produces != null) {
			for (String produce : produces) {
				result.add(new ProduceMediaTypeExpression(produce));
			}
		}
		return result;
	}

	/**
	 * Return the contained "produces" expressions.
	 */
	public Set<MediaTypeExpression> getExpressions() {
		return new LinkedHashSet<MediaTypeExpression>(this.expressions);
	}

	/**
	 * Return the contained producible media types excluding negated
	 * expressions.
	 */
	public Set<MediaType> getProducibleMediaTypes() {
		Set<MediaType> result = new LinkedHashSet<MediaType>();
		for (ProduceMediaTypeExpression expression : this.expressions) {
			if (!expression.isNegated()) {
				result.add(expression.getMediaType());
			}
		}
		return result;
	}

	@Override
	protected List<ProduceMediaTypeExpression> getContent() {
		return this.expressions;
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
		return (!other.expressions.isEmpty() ? other : this);
	}

	/**
	 * Parses and matches a single media type expression to a request's 'Accept'
	 * header.
	 */
	class ProduceMediaTypeExpression extends AbstractMediaTypeExpression {

		ProduceMediaTypeExpression(MediaType mediaType, boolean negated) {
			super(mediaType, negated);
		}

		ProduceMediaTypeExpression(String expression) {
			super(expression);
		}

	}

}
