package roman.vertx.web;

import java.awt.PageAttributes.MediaType;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base for exceptions related to media types. Adds a list of supported
 * 
 * @author RomanLuo
 * @email 530827804@qq.com
 * @date 2016年3月17日 下午1:56:05
 */
@SuppressWarnings("serial")
public abstract class HttpMediaTypeException extends Exception {

	private final List<MediaType> supportedMediaTypes;

	/**
	 * Create a new HttpMediaTypeException.
	 * 
	 * @param message
	 *            the exception message
	 */
	protected HttpMediaTypeException(String message) {
		super(message);
		this.supportedMediaTypes = Collections.emptyList();
	}

	/**
	 * Create a new HttpMediaTypeException with a list of supported media types.
	 * 
	 * @param supportedMediaTypes
	 *            the list of supported media types
	 */
	protected HttpMediaTypeException(String message, List<MediaType> supportedMediaTypes) {
		super(message);
		this.supportedMediaTypes = Collections.unmodifiableList(supportedMediaTypes);
	}

	/**
	 * Return the list of supported media types.
	 */
	public List<MediaType> getSupportedMediaTypes() {
		return this.supportedMediaTypes;
	}

}
