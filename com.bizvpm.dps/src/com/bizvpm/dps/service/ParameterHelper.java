package com.bizvpm.dps.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;

public class ParameterHelper {

	private static final String[] formats = { "yyyy-MM-dd", "yyyy/MM/dd", "HH:mm:ss", "yyyy-MM-dd HH:mm:ss",
			"yyyy/MM/dd HH:mm:ss" };;

	public static Parameter createParamter(IConfigurationElement ce) {

		Parameter parameter = new Parameter();

		parameter.name = ce.getAttribute("name");
		parameter.type = ce.getAttribute("type");
		parameter.description = ce.getAttribute("description");
		parameter.optional = !"false".equalsIgnoreCase(ce.getAttribute("optional"));
		parameter.restrictions = new ArrayList<Object>();
		IConfigurationElement[] children = ce.getChildren("restriction");
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				String sValue = children[i].getAttribute("value");
				Object value = getValue(parameter.type, sValue);
				if (value != null) {
					parameter.restrictions.add(value);
				}
			}
		}
		return parameter;
	}

	private static Object getValue(String type, String value) {
		if ("String".equals(type)) {
			return getStringValue(value);
		} else if ("Double".equals(type)) {
			return getDoubleValue(value);
		} else if ("Float".equals(type)) {
			return getFloatValue(value);
		} else if ("Integer".equals(type)) {
			return getIntegerValue(value);
		} else if ("Boolean".equals(type)) {
			return getBooleanValue(value);
		} else if ("Date".equals(type)) {
			return getDateValue(value);
		} else if ("Long".equals(type)) {
			return getLongValue(value);
		}
		return null;
	}

	private static String getStringValue(String value) {
		return value;
	}

	private static Boolean getBooleanValue(String value) {
		if ("true".equalsIgnoreCase(value)) { //$NON-NLS-1$
			return Boolean.TRUE;
		} else if ("false".equalsIgnoreCase(value)) { //$NON-NLS-1$
			return Boolean.FALSE;
		}
		return null;
	}

	private static Date getDateValue(String value) {
		Date date = null;
		if (value == null) {
			date = null;
		} else {
			SimpleDateFormat sdf;
			for (int i = 0; i < formats.length; i++) {
				try {
					sdf = new SimpleDateFormat(formats[i]);
					date = sdf.parse(value);
					break;
				} catch (Exception e) {
				}
			}
		}
		return date;
	}

	private static Double getDoubleValue(String value) {
		if (value != null) {
			try {
				return Double.parseDouble(value);
			} catch (Exception e) {
			}
		}
		return null;

	}

	private static Long getLongValue(String value) {
		if (value != null) {
			try {
				return Long.parseLong(value);
			} catch (Exception e) {
			}
		}
		return null;
	}

	private static Integer getIntegerValue(String value) {
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (Exception e) {
			}
		}
		return null;
	}

	private static Float getFloatValue(String value) {
		if (value != null) {
			try {
				return Float.parseFloat(value);
			} catch (Exception e) {
			}
		}
		return null;
	}

	public static boolean isValid(Parameter parameter, KeyValuePair keyValuePair) {
		String type = parameter.getType();
		DataObject data = keyValuePair.getValue();
		Object inputValue = new DataObjectConverter().getValue(data);

		if ("String".equals(type) && !(inputValue instanceof String)) {
			return false;
		}
		if ("Long".equals(type) && !(inputValue instanceof Long)) {
			return false;
		}
		if ("Float".equals(type) && !(inputValue instanceof Float)) {
			return false;
		}
		if ("Integer".equals(type) && !(inputValue instanceof Integer)) {
			return false;
		}
		if ("Double".equals(type) && !(inputValue instanceof Double)) {
			return false;
		}
		if ("Boolean".equals(type) && !(inputValue instanceof Boolean)) {
			return false;
		}
		if ("Date".equals(type) && !(inputValue instanceof Date)) {
			return false;
		}
		if ("List".equals(type) && !(inputValue instanceof List)) {
			return false;
		}
		if ("DataHandler".equals(type) && !(inputValue instanceof List)) {
			return false;
		}

		List<Object> restrictions = parameter.getRestrictions();
		if (restrictions != null && !restrictions.isEmpty() && !restrictions.contains(inputValue)) {
			return false;
		}

		return true;
	}
}
