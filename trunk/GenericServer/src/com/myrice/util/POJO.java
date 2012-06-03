package com.myrice.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单数据对象
 * 
 * @author yiyongpeng
 * 
 */
public abstract class POJO {

	@Override
	public String toString() {
		return toString(this);
	}

	public static String toString(Object obj) {
		HashMap<String, Object> already = new HashMap<String, Object>(2);
		StringBuffer sb = new StringBuffer("{");

		toString(obj, obj.getClass(), sb, already);

		sb.append("}");
		return sb.toString();
	}

	public static void toString(Object obj, Class<?> clazz, StringBuffer sb,
			Map<String, Object> already) {
		if (clazz == null)
			return;
		if (clazz.isArray() || clazz.isPrimitive()) {
			appendValue(obj, sb);
			return;
		}
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
								if (sb.length() > 1)
									sb.append(", ");
								sb.append(name.substring(3, 4).toLowerCase());
								sb.append(name.substring(4));
								sb.append("=");
								appendValue(value, sb);
							}
						} else if (name.startsWith("is")
								&& method.getReturnType() == boolean.class) {
							if ((value = method.invoke(obj)) != obj) {
								if (sb.length() > 1)
									sb.append(", ");
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
		boolean array = value != null ? !(value instanceof Class)
				&& value.getClass().isArray() : false;
		boolean flag = value != null && !(value instanceof POJO)
				&& array == false;
		if (flag)
			sb.append("\"");
		if (array) {
			Class<?> clazz = value.getClass();
			if (clazz == boolean[].class) {
				boolean[] arry = (boolean[]) value;
				sb.append(Arrays.toString(arry));
			} else if (clazz == char[].class) {
				char[] arry = (char[]) value;
				sb.append(Arrays.toString(arry));
			} else if (clazz == byte[].class) {
				byte[] arry = (byte[]) value;
				sb.append(Arrays.toString(arry));
			} else if (clazz == short[].class) {
				short[] arry = (short[]) value;
				sb.append(Arrays.toString(arry));
			} else if (clazz == int[].class) {
				int[] arry = (int[]) value;
				sb.append(Arrays.toString(arry));
			} else if (clazz == long[].class) {
				long[] arry = (long[]) value;
				sb.append(Arrays.toString(arry));
			} else if (clazz == float[].class) {
				float[] arry = (float[]) value;
				sb.append(Arrays.toString(arry));
			} else if (clazz == double[].class) {
				double[] arry = (double[]) value;
				sb.append(Arrays.toString(arry));
			} else if (clazz == String[].class) {
				String[] arry = (String[]) value;
				sb.append(Arrays.toString(arry));
			} else if (value instanceof Object[]) {
				Object[] arry = (Object[]) value;
				sb.append("[");
				for (int i = 0; i < arry.length; i++) {
					if (i > 0)
						sb.append(", ");
					appendValue(arry[i], sb);
				}
				sb.append("]");
			} else {
				sb.append(value.toString());
			}
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
