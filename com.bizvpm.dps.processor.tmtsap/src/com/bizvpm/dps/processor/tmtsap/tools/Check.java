package com.bizvpm.dps.processor.tmtsap.tools;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Check {

	public static boolean isTrue(Object target) {
		if (target == null)
			return false;
		if (target instanceof Boolean) {
			return (Boolean) target;
		}
		if ("true".equalsIgnoreCase(target.toString()))
			return true;
		if (target instanceof Number)
			return ((Number) target).doubleValue() != 0;
		return Boolean.TRUE.equals(target);
	}

	public static boolean allTrue(Object... target) {
		if (target == null || target.length == 0)
			return false;
		for (int i = 0; i < target.length; i++) {
			if (!isTrue(target[i]))
				return false;
		}
		return true;
	}

	/**
	 * 检查两个参数是否相等，如果都为null, 返回true
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static boolean equals(Object v1, Object v2) {
		return v1 != null && v1.equals(v2) || v1 == null && v2 == null;
	}

	/**
	 * 检查传入参数是否为空或者只包含空格
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNotAssigned(String str) {
		return str == null || str.trim().isEmpty();
	}

	/**
	 * 检查传入参数是否有值，不为空，去掉空格后也不是空字符串
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isAssigned(String str) {
		return str != null && !str.trim().isEmpty();
	}

	public static boolean isAssigned(String... str) {
		if (str == null || str.length == 0)
			return false;
		for (int i = 0; i < str.length; i++) {
			if (isNotAssigned(str[i])) {
				return false;
			}
		}
		return true;
	}

	public static boolean isAssigned(String s, Consumer<String> then) {
		if (!isNotAssigned(s)) {
			if (then != null)
				then.accept(s);
			return true;
		} else {
			return false;
		}
	}

	public static <T> Optional<T> isAssignedThen(String s, Function<String, T> then) {
		return Optional.ofNullable(!isNotAssigned(s) && then != null ? then.apply(s) : null);
	}

	public static Optional<String> option(String s) {
		if (isAssigned(s)) {
			return Optional.of(s);
		} else {
			return Optional.ofNullable(null);
		}
	}

	public static <T> boolean isNotAssigned(List<T> s) {
		return s == null || s.isEmpty();
	}

	public static <T> boolean isAssigned(List<T> s) {
		return !isNotAssigned(s);
	}

	public static boolean isNotAssigned(Map<?, ?> s) {
		return s == null || s.isEmpty();
	}

	public static <T> boolean isAssigned(Map<?, ?> s) {
		return !isNotAssigned(s);
	}

	public static <T> boolean isNotAssigned(T[] s) {
		return s == null || s.length == 0;
	}

	public static <T> boolean isAssigned(T[] s) {
		return !isNotAssigned(s);
	}

	public static <T> boolean isAssigned(T[] s, Consumer<T[]> then) {
		if (!isNotAssigned(s)) {
			if (then != null)
				then.accept(s);
			return true;
		} else {
			return false;
		}
	}

	public static <T, E> Optional<T> isAssignedThen(E[] s, Function<E[], T> then) {
		return Optional.ofNullable(!isNotAssigned(s) && then != null ? then.apply(s) : null);
	}

	public static <T> boolean isAssigned(List<T> s, Consumer<List<T>> then) {
		if (!isNotAssigned(s)) {
			if (then != null)
				then.accept(s);
			return true;
		} else {
			return false;
		}
	}

	public static boolean isAssigned(Map<?, ?> s, Consumer<Map<?, ?>> then) {
		if (!isNotAssigned(s)) {
			if (then != null)
				then.accept(s);
			return true;
		} else {
			return false;
		}
	}

	public static <T, E> Optional<T> isAssignedThen(List<E> s, Function<List<E>, T> then) {
		return Optional.ofNullable(!isNotAssigned(s) && then != null ? then.apply(s) : null);
	}

	public static <T> Optional<T> isAssignedThen(Map<?, ?> s, Function<Map<?, ?>, T> then) {
		return Optional.ofNullable(!isNotAssigned(s) && then != null ? then.apply(s) : null);
	}

	@SuppressWarnings("unchecked")
	public static <T> boolean instanceThen(Object obj, Class<T> clazz, Consumer<T> then) {
		if (obj != null && clazz.isAssignableFrom(obj.getClass())) {
			then.accept((T) obj);
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static <T> Optional<T> instanceOf(Object obj, Class<T> clazz) {
		T result = null;
		if (obj != null && clazz.isAssignableFrom(obj.getClass())) {
			result = (T) obj;
		}
		return Optional.ofNullable(result);
	}

	public static boolean isValidEmailAddress(String email) {
		if (email == null)
			return false;
		String regEx1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		Pattern p;
		Matcher m;
		p = Pattern.compile(regEx1);
		m = p.matcher(email);
		if (m.matches())
			return true;
		else
			return false;
	}

	public static boolean inArray(Object element, Object[] arr) {
		if (arr == null) {
			return false;
		}
		for (Object o : arr) {
			if (o.equals(element)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNumbers(String content) {
		if (content == null || "".equals(content)) {
			return false;
		}
		int length = content.length();
		for (int i = 0; i < length; i++) {
			char ch = content.charAt(i);
			if (!Character.isDigit(ch)) {
				return false;
			}
		}
		return true;
	}

}
