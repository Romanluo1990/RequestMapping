package roman.vertx.web.condition;

/**
 * A contract for {@code "name!=value"} style expression used to specify request
 * parameters and request header conditions in {@code @RequestMapping}.
 *
 * @author RomanLuo
 * @email 530827804@qq.com
 * @date 2016年3月17日 下午2:03:30
 */
public interface NameValueExpression<T> {

	String getName();

	T getValue();

	boolean isNegated();

}
