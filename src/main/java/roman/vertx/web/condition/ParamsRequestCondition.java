package roman.vertx.web.condition;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import roman.vertx.web.bind.annotation.RequestMapping;

/**
 * A logical conjunction (' && ') request condition that matches a request
 * against a set parameter expressions with syntax defined in
 * {@link RequestMapping#params()}.
 *
 * @author RomanLuo
 * @email 530827804@qq.com
 * @date 2016年3月17日 下午2:03:02
 */
public final class ParamsRequestCondition extends AbstractRequestCondition<ParamsRequestCondition> {

	private final Set<ParamExpression> expressions;

	/**
	 * Create a new instance from the given param expressions.
	 * 
	 * @param params
	 *            expressions with syntax defined in
	 *            {@link RequestMapping#params()}; if 0, the condition will
	 *            match to every request.
	 */
	public ParamsRequestCondition(String... params) {
		this(parseExpressions(params));
	}

	private ParamsRequestCondition(Collection<ParamExpression> conditions) {
		this.expressions = Collections.unmodifiableSet(new LinkedHashSet<ParamExpression>(conditions));
	}

	private static Collection<ParamExpression> parseExpressions(String... params) {
		Set<ParamExpression> expressions = new LinkedHashSet<ParamExpression>();
		if (params != null) {
			for (String param : params) {
				expressions.add(new ParamExpression(param));
			}
		}
		return expressions;
	}

	/**
	 * Return the contained request parameter expressions.
	 */
	public Set<NameValueExpression<String>> getExpressions() {
		return new LinkedHashSet<NameValueExpression<String>>(this.expressions);
	}

	@Override
	protected Collection<ParamExpression> getContent() {
		return this.expressions;
	}

	@Override
	protected String getToStringInfix() {
		return " && ";
	}

	/**
	 * Returns a new instance with the union of the param expressions from
	 * "this" and the "other" instance.
	 */
	@Override
	public ParamsRequestCondition combine(ParamsRequestCondition other) {
		Set<ParamExpression> set = new LinkedHashSet<ParamExpression>(this.expressions);
		set.addAll(other.expressions);
		return new ParamsRequestCondition(set);
	}

	/**
	 * Parses and matches a single param expression to a request.
	 */
	static class ParamExpression extends AbstractNameValueExpression<String> {

		ParamExpression(String expression) {
			super(expression);
		}

		@Override
		protected boolean isCaseSensitiveName() {
			return true;
		}

		@Override
		protected String parseValue(String valueExpression) {
			return valueExpression;
		}

	}

}
