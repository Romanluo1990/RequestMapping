package roman.vertx.web.condition;

import io.vertx.ext.web.Route;

import java.util.Collection;
import java.util.Collections;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * A logical disjunction (' || ') request condition that matches a request
 * against a set of URL path patterns.
 *
 * @author RomanLuo
 * @email 530827804@qq.com
 * @date 2016年3月17日 下午2:02:39
 */
public final class PatternRequestCondition extends AbstractRequestCondition<PatternRequestCondition> {

	private final String pattern;

	private final PathMatcher pathMatcher;

	public PatternRequestCondition() {
		this("", null);
	}

	/**
	 * Creates a new instance with the given URL patterns. Each pattern that is
	 * not empty and does not start with "/" is prepended with "/".
	 * 
	 * @param patterns
	 *            0 or more URL patterns; if 0 the condition will match to every
	 *            request.
	 */
	public PatternRequestCondition(String pattern) {
		this(pattern, null);
	}

	public PatternRequestCondition(String pattern, PathMatcher pathMatcher) {
		this.pattern = pattern;
		this.pathMatcher = pathMatcher != null ? pathMatcher : new AntPathMatcher();
	}

	public String getPattern() {
		return this.pattern;
	}

	@Override
	protected Collection<String> getContent() {
		return Collections.singletonList(pattern);
	}

	@Override
	protected String getToStringInfix() {
		return " || ";
	}

	/**
	 * Returns a new instance with URL patterns from the current instance
	 * ("this") and the "other" instance as follows:
	 * <ul>
	 * <li>If there are patterns in both instances, combine the patterns in
	 * "this" with the patterns in "other" using
	 * {@link PathMatcher#combine(String, String)}.
	 * <li>If only one instance has patterns, use them.
	 * <li>If neither instance has patterns, use an empty String (i.e. "").
	 * </ul>
	 */
	@Override
	public PatternRequestCondition combine(PatternRequestCondition other) {
		String pattern = this.pathMatcher.combine(this.pattern, other.pattern);
		return new PatternRequestCondition(pattern, this.pathMatcher);
	}

	@Override
	public Route Router(Route route) {
		if (pathMatcher.isPattern(pattern)) {
			route.pathRegex(pattern);
		} else {
			route.path(pattern);
		}
		return route;
	}

}
