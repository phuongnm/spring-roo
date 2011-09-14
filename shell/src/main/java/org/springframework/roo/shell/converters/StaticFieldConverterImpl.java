package org.springframework.roo.shell.converters;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.roo.shell.Converter;
import org.springframework.roo.shell.MethodTarget;
import org.springframework.roo.support.util.Assert;

/**
 * A simple {@link Converter} for those classes which provide public static fields to represent possible
 * textual values.
 *
 * @author Stefan Schmidt
 * @author Ben Alex
 * @since 1.0
 */
public class StaticFieldConverterImpl implements StaticFieldConverter {

	// Fields
	private Map<Class<?>,Map<String,Field>> fields = new HashMap<Class<?>,Map<String,Field>>();
	
	public void add(Class<?> clazz) {
		Assert.notNull(clazz, "A class to provide conversion services is required");
		Assert.isNull(fields.get(clazz), "Class '" + clazz + "' is already registered for completion services");
		Map<String,Field> ffields = new HashMap<String, Field>();
		for (Field field : clazz.getFields()) {
			int modifier = field.getModifiers();
			if (Modifier.isStatic(modifier) && Modifier.isPublic(modifier)) {
				ffields.put(field.getName(), field);
			}
		}
		Assert.notEmpty(ffields, "Zero public static fields accessible in '" + clazz + "'");
		fields.put(clazz, ffields);
	}
	
	public void remove(Class<?> clazz) {
		Assert.notNull(clazz, "A class that was providing conversion services is required");
		fields.remove(clazz);
	}
	
	public Object convertFromText(String value, Class<?> requiredType, String optionContext) {
		if (value == null || "".equals(value)) {
			return null;
		}
		Map<String,Field> ffields = fields.get(requiredType);
		if (ffields == null) {
			return null;
		}
		Field f = ffields.get(value);
		if (f == null) {
			// Fallback to case insensitive search
			for (Field candidate : ffields.values()) {
				if (candidate.getName().toLowerCase().equals(value.toLowerCase())) {
					f = candidate;
					break;
				}
			}
			if (f == null) {
				// Still not found, despite a case-insensitive search
				return null;
			}
		}
		try {
			return f.get(null);
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to acquire field '" + value + "' from '" + requiredType.getName() + "'", ex);
		}
	}

	public boolean getAllPossibleValues(List<String> completions, Class<?> requiredType, String existingData, String optionContext, MethodTarget target) {
		Map<String,Field> ffields = fields.get(requiredType);
		if (ffields == null) {
			return true;
		}
		completions.addAll(ffields.keySet());
		return true;
	}

	public boolean supports(Class<?> requiredType, String optionContext) {
		return fields.get(requiredType) != null;
	}
}
