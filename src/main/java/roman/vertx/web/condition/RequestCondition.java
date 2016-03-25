package roman.vertx.web.condition;

import io.vertx.ext.web.Route;
import roman.vertx.web.bind.annotation.RequestMapping;

/**
 * The contract for request conditions in Spring MVC's mapping infrastructure.
 *
 * <p>
 * Request conditions can be combined via {@link #combine(Object)}, matched to a
 * request via {@link #getMatchingCondition(HttpServletRequest)}, and compared
 * to each other via {@link #compareTo(Object, HttpServletRequest)} to determine
 * which matches a request more closely.
 *
 * @param <T>
 *            the type of objects that this RequestCondition can be combined
 *            with and compared to
 * @author RomanLuo
 * @email 530827804@qq.com
 * @date 2016年3月17日 下午2:02:02
 */
public interface RequestCondition<T> {

	/**
	 * Defines the rules for combining this condition (i.e. the current
	 * instance) with another condition. For example combining type- and
	 * method-level {@link RequestMapping} conditions.
	 * 
	 * @param other
	 *            the condition to combine with.
	 * @return a request condition instance that is the result of combining the
	 *         two condition instances.
	 */
	T combine(T other);

	Route Router(Route route);
}
