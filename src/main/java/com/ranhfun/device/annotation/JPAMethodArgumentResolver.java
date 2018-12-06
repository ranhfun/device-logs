package com.ranhfun.device.annotation;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import javax.servlet.ServletRequest;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.ModelFactory;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class JPAMethodArgumentResolver implements HandlerMethodArgumentResolver {

	private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
	
	private EntityManager em;
	
	public JPAMethodArgumentResolver(EntityManager em) {
		this.em = em;
	}
	
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().getDeclaredAnnotation(Entity.class)!=null;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		Metamodel metamodel = em.getMetamodel();
		EntityType entityType = metamodel.entity(parameter.getParameterType());
		final Type<?> idType = entityType.getIdType();

        final SingularAttribute<?, ?> idAttribute = entityType.getId(idType.getJavaType());

        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
		String idPropertyName = (requestParam == null || StringUtils.isEmpty(requestParam.name()) ?
				idAttribute.getName() : requestParam.name());
        
        String name = ModelFactory.getNameForParameter(parameter);
        DataBinder binder = binderFactory.createBinder(webRequest, null, name);
		ConversionService conversionService = binder.getConversionService();
        
        Object idValue = conversionService.convert(webRequest.getParameter(idPropertyName), idAttribute.getJavaType());
		
		Object attribute = null;
		BindingResult bindingResult = null;
		
		if (idValue!=null) {
			attribute = em.find(entityType.getJavaType(), idValue);
		} else {
			// Create attribute instance
			try {
				attribute = createAttribute(name, parameter, binderFactory, webRequest);
			}
			catch (BindException ex) {
				if (isBindExceptionRequired(parameter)) {
					// No BindingResult parameter -> fail with BindException
					throw ex;
				}
				// Otherwise, expose null/empty value and associated BindingResult
				if (parameter.getParameterType() == Optional.class) {
					attribute = Optional.empty();
				}
				bindingResult = ex.getBindingResult();
			}
		}

		if (bindingResult == null) {
			WebDataBinder webbinder = binderFactory.createBinder(webRequest, attribute, name);
			if (webbinder.getTarget() != null) {
				if (!mavContainer.isBindingDisabled(name)) {
					bindRequestParameters(webbinder, webRequest);
				}
			}
			// Value type adaptation, also covering java.util.Optional
			if (!parameter.getParameterType().isInstance(attribute)) {
				attribute = binder.convertIfNecessary(binder.getTarget(), parameter.getParameterType(), parameter);
			}
			bindingResult = binder.getBindingResult();
		}
		Map<String, Object> bindingResultModel = bindingResult.getModel();
		mavContainer.removeAttributes(bindingResultModel);
		mavContainer.addAllAttributes(bindingResultModel);
		return attribute;
	}

	protected boolean isBindExceptionRequired(MethodParameter parameter) {
		int i = parameter.getParameterIndex();
		Class<?>[] paramTypes = parameter.getExecutable().getParameterTypes();
		boolean hasBindingResult = (paramTypes.length > (i + 1) && Errors.class.isAssignableFrom(paramTypes[i + 1]));
		return !hasBindingResult;
	}

	protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest request) {
		ServletRequest servletRequest = request.getNativeRequest(ServletRequest.class);
		Assert.state(servletRequest != null, "No ServletRequest");
		ServletRequestDataBinder servletBinder = (ServletRequestDataBinder) binder;
		servletBinder.bind(servletRequest);
	}
	
	protected Object createAttribute(String attributeName, MethodParameter parameter,
			WebDataBinderFactory binderFactory, NativeWebRequest webRequest) throws Exception {

		MethodParameter nestedParameter = parameter.nestedIfOptional();
		Class<?> clazz = nestedParameter.getNestedParameterType();

		Constructor<?> ctor = BeanUtils.findPrimaryConstructor(clazz);
		if (ctor == null) {
			Constructor<?>[] ctors = clazz.getConstructors();
			if (ctors.length == 1) {
				ctor = ctors[0];
			}
			else {
				try {
					ctor = clazz.getDeclaredConstructor();
				}
				catch (NoSuchMethodException ex) {
					throw new IllegalStateException("No primary or default constructor found for " + clazz, ex);
				}
			}
		}

		Object attribute = constructAttribute(ctor, attributeName, binderFactory, webRequest);
		if (parameter != nestedParameter) {
			attribute = Optional.of(attribute);
		}
		return attribute;
	}
	
	protected Object constructAttribute(Constructor<?> ctor, String attributeName,
			WebDataBinderFactory binderFactory, NativeWebRequest webRequest) throws Exception {

		if (ctor.getParameterCount() == 0) {
			// A single default constructor -> clearly a standard JavaBeans arrangement.
			return BeanUtils.instantiateClass(ctor);
		}

		// A single data class constructor -> resolve constructor arguments from request parameters.
		ConstructorProperties cp = ctor.getAnnotation(ConstructorProperties.class);
		String[] paramNames = (cp != null ? cp.value() : parameterNameDiscoverer.getParameterNames(ctor));
		Assert.state(paramNames != null, () -> "Cannot resolve parameter names for constructor " + ctor);
		Class<?>[] paramTypes = ctor.getParameterTypes();
		Assert.state(paramNames.length == paramTypes.length,
				() -> "Invalid number of parameter names: " + paramNames.length + " for constructor " + ctor);

		Object[] args = new Object[paramTypes.length];
		WebDataBinder binder = binderFactory.createBinder(webRequest, null, attributeName);
		String fieldDefaultPrefix = binder.getFieldDefaultPrefix();
		String fieldMarkerPrefix = binder.getFieldMarkerPrefix();
		boolean bindingFailure = false;

		for (int i = 0; i < paramNames.length; i++) {
			String paramName = paramNames[i];
			Class<?> paramType = paramTypes[i];
			Object value = webRequest.getParameterValues(paramName);
			if (value == null) {
				if (fieldDefaultPrefix != null) {
					value = webRequest.getParameter(fieldDefaultPrefix + paramName);
				}
				if (value == null && fieldMarkerPrefix != null) {
					if (webRequest.getParameter(fieldMarkerPrefix + paramName) != null) {
						value = binder.getEmptyValue(paramType);
					}
				}
			}
			try {
				MethodParameter methodParam = new MethodParameter(ctor, i);
				if (value == null && methodParam.isOptional()) {
					args[i] = (methodParam.getParameterType() == Optional.class ? Optional.empty() : null);
				}
				else {
					args[i] = binder.convertIfNecessary(value, paramType, methodParam);
				}
			}
			catch (TypeMismatchException ex) {
				ex.initPropertyName(paramName);
				binder.getBindingErrorProcessor().processPropertyAccessException(ex, binder.getBindingResult());
				bindingFailure = true;
				args[i] = value;
			}
		}

		if (bindingFailure) {
			BindingResult result = binder.getBindingResult();
			for (int i = 0; i < paramNames.length; i++) {
				result.recordFieldValue(paramNames[i], paramTypes[i], args[i]);
			}
			throw new BindException(result);
		}

		return BeanUtils.instantiateClass(ctor, args);
	}
}
