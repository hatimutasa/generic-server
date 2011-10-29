package com.myrice.core.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class POJO {

	@Override
	public String toString() {
		return toString(this);
	}

	public static String toString(Object obj) {
		HashMap<String, Object> already = new HashMap<String, Object>(2);
		StringBuffer sb = new StringBuffer("{");

		// ×´Ì¬ÁÐ±í
		toString(obj, obj.getClass(), sb, already);

		sb.append("}");
		return sb.toString();
	}

	public static void toString(Object obj, Class<?> clazz, StringBuffer sb,
			Map<String, Object> already) {
		if (clazz == null)
			return;
		String name = null;
		Object value = null;
		Method[] fields = clazz.getMethods();
		try {
			for (Method method : fields)
				if (method.getParameterTypes().length == 0
						&& method.getReturnType() != void.class) {
					name = method.getName();
					if (already.containsKey(name) == false) {
						already.put(name, null);
						if (name.startsWith("get")) {
							if ((value = method.invoke(obj)) != obj) {
								if (!sb.toString().equals("{"))
									sb.append(", ");
								sb.append(name.substring(3, 4).toLowerCase());
								sb.append(name.substring(4));
								sb.append("=");
								appendValue(value, sb);
							}
						} else if (name.startsWith("is")
						/* && method.getReturnType() == boolean.class */) {
							if ((value = method.invoke(obj)) != obj) {
								if (!sb.toString().equals("{"))
									sb.append(" ");
								sb.append(name.substring(2, 3).toLowerCase());
								sb.append(name.substring(3));
								sb.append("=");
								appendValue(value, sb);
							}
						}
					}
				}
			toString(obj, clazz.getSuperclass(), sb, already);
			toString(obj, clazz.getInterfaces(), sb, already);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void appendValue(Object value, StringBuffer sb) {
		boolean array = value != null ? value.getClass().isArray() : false;
		boolean flag = value != null && !(value instanceof POJO)
				&& array == false;
		if (flag)
			sb.append("\"");
		if (array) {
			Object[] arry = (Object[]) value;
			sb.append("[");
			for (Object obj : arry) {
				if (obj != arry[0])
					sb.append(", ");
				appendValue(obj, sb);
			}
			sb.append("]");
		} else
			sb.append(value);
		if (flag)
			sb.append("\"");
	}

	public static void toString(Object obj, Class<?>[] clases, StringBuffer sb,
			Map<String, Object> already) {
		for (Class<?> clazz : clases)
			toString(obj, clazz, sb, already);
	}

}
