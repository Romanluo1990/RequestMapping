package roman.vertx.web.condition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import roman.vertx.web.bind.annotation.RequestMapping;
import roman.vertx.web.http.MediaType;

/**
 * Supports media type expressions as described in:
 * {@link RequestMapping#consumes()} and {@link RequestMapping#produces()}.
 *
 * @author RomanLuo
 * @email 530827804@qq.com
 * @date 2016年3月17日 下午2:05:25
 */
abstract class AbstractMediaTypeExpression implements Comparable<AbstractMediaTypeExpression>, MediaTypeExpression {

	protected final Log logger = LogFactory.getLog(getClass());

	private final MediaType mediaType;

	private final boolean isNegated;

	AbstractMediaTypeExpression(String expression) {
		if (expression.startsWith("!")) {
			isNegated = true;
			expression = expression.substring(1);
		} else {
			isNegated = false;
		}
		this.mediaType = MediaType.parseMediaType(expression);
	}

	AbstractMediaTypeExpression(MediaType mediaType, boolean negated) {
		this.mediaType = mediaType;
		isNegated = negated;
	}

	@Override
	public MediaType getMediaType() {
		return mediaType;
	}

	@Override
	public boolean isNegated() {
		return isNegated;
	}

	@Override
	public int compareTo(AbstractMediaTypeExpression other) {
		return MediaType.SPECIFICITY_COMPARATOR.compare(this.getMediaType(), other.getMediaType());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && getClass().equals(obj.getClass())) {
			AbstractMediaTypeExpression other = (AbstractMediaTypeExpression) obj;
			return (this.mediaType.equals(other.mediaType)) && (this.isNegated == other.isNegated);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return mediaType.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (isNegated) {
			builder.append('!');
		}
		builder.append(mediaType.toString());
		return builder.toString();
	}

}
