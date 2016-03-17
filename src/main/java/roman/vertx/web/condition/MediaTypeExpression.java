package roman.vertx.web.condition;

import roman.vertx.web.http.MediaType;

/**
 * A contract for media type expressions (e.g. "text/plain", "!text/plain") as
 * defined in the {@code @RequestMapping} annotation for "consumes" and
 * "produces" conditions.
 * 
 * @author RomanLuo
 * @email 530827804@qq.com
 * @date 2016年3月17日 下午2:03:46
 */
public interface MediaTypeExpression {

	MediaType getMediaType();

	boolean isNegated();

}
