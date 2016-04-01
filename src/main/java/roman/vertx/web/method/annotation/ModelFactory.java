
package roman.vertx.web.method.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.Conventions;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;

/**
 * Provides methods to initialize the {@link Model} before controller method
 * invocation and to update it afterwards.
 *
 * <p>On initialization, the model is populated with attributes from the session
 * and by invoking methods annotated with {@code @ModelAttribute}.
 *
 * <p>On update, model attributes are synchronized with the session and also
 * {@link BindingResult} attributes are added where missing.
 *
 * @author Rossen Stoyanchev
 * @since 3.1
 */
public final class ModelFactory {

	private static final Log logger = LogFactory.getLog(ModelFactory.class);

	/**
	 * Derives the model attribute name for a method parameter based on:
	 * <ol>
	 * 	<li>The parameter {@code @ModelAttribute} annotation value
	 * 	<li>The parameter type
	 * </ol>
	 * @return the derived name; never {@code null} or an empty string
	 */
	public static String getNameForParameter(MethodParameter parameter) {
//		ModelAttribute annot = parameter.getParameterAnnotation(ModelAttribute.class);
		String attrName = parameter.getParameterName();
		return StringUtils.hasText(attrName) ? attrName :  Conventions.getVariableNameForParameter(parameter);
	}

}
