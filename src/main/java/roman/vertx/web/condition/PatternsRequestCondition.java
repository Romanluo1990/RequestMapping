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
 * @author Rossen Stoyanchev
 * @since 3.1
 */
public final class PatternsRequestCondition extends AbstractRequestCondition<PatternsRequestCondition> {

	private final Set<String> patterns;

	private final PathMatcher pathMatcher;

	private final boolean useSuffixPatternMatch;

	private final boolean useTrailingSlashMatch;

	private final List<String> fileExtensions = new ArrayList<String>();

	/**
	 * Creates a new instance with the given URL patterns. Each pattern that is
	 * not empty and does not start with "/" is prepended with "/".
	 * 
	 * @param patterns
	 *            0 or more URL patterns; if 0 the condition will match to every
	 *            request.
	 */
	public PatternsRequestCondition(String... patterns) {
		this(asList(patterns), null, true, true, null);
	}

	/**
	 * Additional constructor with flags for using suffix pattern (.*) and
	 * trailing slash matches.
	 * 
	 * @param patterns
	 *            the URL patterns to use; if 0, the condition will match to
	 *            every request.
	 * @param urlPathHelper
	 *            for determining the lookup path of a request
	 * @param pathMatcher
	 *            for path matching with patterns
	 * @param useSuffixPatternMatch
	 *            whether to enable matching by suffix (".*")
	 * @param useTrailingSlashMatch
	 *            whether to match irrespective of a trailing slash
	 */
	public PatternsRequestCondition(String[] patterns, PathMatcher pathMatcher, boolean useSuffixPatternMatch, boolean useTrailingSlashMatch) {
		this(asList(patterns), pathMatcher, useSuffixPatternMatch, useTrailingSlashMatch, null);
	}

	/**
	 * Creates a new instance with the given URL patterns. Each pattern that is
	 * not empty and does not start with "/" is pre-pended with "/".
	 * 
	 * @param patterns
	 *            the URL patterns to use; if 0, the condition will match to
	 *            every request.
	 * @param urlPathHelper
	 *            a {@link UrlPathHelper} for determining the lookup path for a
	 *            request
	 * @param pathMatcher
	 *            a {@link PathMatcher} for pattern path matching
	 * @param useSuffixPatternMatch
	 *            whether to enable matching by suffix (".*")
	 * @param useTrailingSlashMatch
	 *            whether to match irrespective of a trailing slash
	 * @param fileExtensions
	 *            a list of file extensions to consider for path matching
	 */
	public PatternsRequestCondition(String[] patterns, PathMatcher pathMatcher, boolean useSuffixPatternMatch, boolean useTrailingSlashMatch, List<String> fileExtensions) {

		this(asList(patterns), pathMatcher, useSuffixPatternMatch, useTrailingSlashMatch, fileExtensions);
	}

	/**
	 * Private constructor accepting a collection of patterns.
	 */
	private PatternsRequestCondition(Collection<String> patterns, PathMatcher pathMatcher, boolean useSuffixPatternMatch, boolean useTrailingSlashMatch, List<String> fileExtensions) {

		this.patterns = Collections.unmodifiableSet(prependLeadingSlash(patterns));
		this.pathMatcher = pathMatcher != null ? pathMatcher : new AntPathMatcher();
		this.useSuffixPatternMatch = useSuffixPatternMatch;
		this.useTrailingSlashMatch = useTrailingSlashMatch;
		if (fileExtensions != null) {
			for (String fileExtension : fileExtensions) {
				if (fileExtension.charAt(0) != '.') {
					fileExtension = "." + fileExtension;
				}
				this.fileExtensions.add(fileExtension);
			}
		}
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
		return new PatternsRequestCondition(result, this.pathMatcher, this.useSuffixPatternMatch, this.useTrailingSlashMatch, this.fileExtensions);
	}

}
