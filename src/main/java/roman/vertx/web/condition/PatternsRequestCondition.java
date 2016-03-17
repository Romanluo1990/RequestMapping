package roman.vertx.web.condition;

import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

/**
 * A logical disjunction (' || ') request condition that matches a request
 * against a set of URL path patterns.
 *
 * @author RomanLuo
 * @email 530827804@qq.com
 * @date 2016年3月17日 下午2:02:39
 */
public final class PatternsRequestCondition extends AbstractRequestCondition<PatternsRequestCondition> {

	private final Set<String> patterns;

	private final PathMatcher pathMatcher;

	/**
	 * Creates a new instance with the given URL patterns. Each pattern that is
	 * not empty and does not start with "/" is prepended with "/".
	 * 
	 * @param patterns
	 *            0 or more URL patterns; if 0 the condition will match to every
	 *            request.
	 */
	public PatternsRequestCondition(String... patterns) {
		this(asList(patterns), null);
	}

	public PatternsRequestCondition(String[] patterns, PathMatcher pathMatcher) {
		this(asList(patterns), pathMatcher);
	}

	/**
	 * Private constructor accepting a collection of patterns.
	 */
	private PatternsRequestCondition(Collection<String> patterns, PathMatcher pathMatcher) {

		this.patterns = Collections.unmodifiableSet(prependLeadingSlash(patterns));
		this.pathMatcher = pathMatcher != null ? pathMatcher : new AntPathMatcher();
	}

	private static List<String> asList(String... patterns) {
		return (patterns != null ? Arrays.asList(patterns) : Collections.<String> emptyList());
	}

	private static Set<String> prependLeadingSlash(Collection<String> patterns) {
		if (patterns == null) {
			return Collections.emptySet();
		}
		Set<String> result = new LinkedHashSet<String>(patterns.size());
		for (String pattern : patterns) {
			if (StringUtils.hasLength(pattern) && !pattern.startsWith("/")) {
				pattern = "/" + pattern;
			}
			result.add(pattern);
		}
		return result;
	}

	public Set<String> getPatterns() {
		return this.patterns;
	}

	@Override
	protected Collection<String> getContent() {
		return this.patterns;
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
	public PatternsRequestCondition combine(PatternsRequestCondition other) {
		Set<String> result = new LinkedHashSet<String>();
		if (!this.patterns.isEmpty() && !other.patterns.isEmpty()) {
			for (String pattern1 : this.patterns) {
				for (String pattern2 : other.patterns) {
					result.add(this.pathMatcher.combine(pattern1, pattern2));
				}
			}
		} else if (!this.patterns.isEmpty()) {
			result.addAll(this.patterns);
		} else if (!other.patterns.isEmpty()) {
			result.addAll(other.patterns);
		} else {
			result.add("");
		}
		return new PatternsRequestCondition(result, this.pathMatcher);
	}

	public List<Route> router(Router router) {
		List<Route> routes = new ArrayList<Route>();
		for (String pattern : patterns) {
			if (pathMatcher.isPattern(pattern)) {
				routes.add(router.routeWithRegex(pattern));
			} else {
				routes.add(router.route(pattern));
			}
		}
		return routes;
	}

}
