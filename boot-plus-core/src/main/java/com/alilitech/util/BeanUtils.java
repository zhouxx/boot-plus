/*
 *    Copyright 2017-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.alilitech.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * bean utils
 * copy properties contains parent's properties, aggregate object's properties. And also open a cache.
 * @author zhouxiaxiang
 *
 */
public class BeanUtils {

	/** Log */
	private static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);

	/** cache */
	private static final Map<Class<?>, List<FieldDesc>> cache = new ConcurrentHashMap<>();

	/**
	 * bean to map，only support the fields of the current class
	 * @param source source object
	 * @return map result
	 */
	public static Map<String, Object> beanToMapCommon(Object source) {
		if(source == null) {
			logger.debug("Source object is null");
			return null;
		}
		Map<String, Object> mapRet = new HashMap<>();
		List<TempBean> list = BeanUtils.findSourceMethod(source);
		try {
			for (TempBean tb : list) {
				try {
					Object value = tb.getObject().getClass().getMethod(tb.getFieldDesc().getGetName(), new Class[]{}).invoke(tb.getObject());
					String fieldName = tb.getFieldDesc().getFieldName();
					mapRet.put(fieldName, value);
				} catch (NoSuchMethodException | SecurityException e) {
					logger.debug(tb.getFieldDesc().getSetName() + " is not exist, not covert!");
				}
			}
		} catch (Exception e) {
			logger.error("copy occur error: {}", e.getMessage());
		}
		return mapRet;
	}

	/**
	 * bean to map，also support the field of the parent class
	 * @param source source object
	 * @return map result
	 */
	public static Map<String, Object> beanToMap(Object source) {
		if(source == null) {
			logger.debug("Source object is null");
			return null;
		}
		Map<String, Object> mapRet = new HashMap<>();
		try {
			List<TempBean> list = BeanUtils.findSourceMethod(source, null);
			for (TempBean tb : list) {
				try {
					Object value = tb.getObject().getClass().getMethod(tb.getFieldDesc().getGetName(), new Class[]{}).invoke(tb.getObject());
					String fieldName = tb.getFieldDesc().getFieldName();
					mapRet.put(fieldName, value);
				} catch (NoSuchMethodException | SecurityException e) {
					logger.debug(tb.getFieldDesc().getSetName() + " is not exist, not covert!");
				}
			}
		}catch (Exception e) {
			logger.error("copy occur error: {}", e.getMessage());
		}
		return mapRet;
	}

	/**
	 * 将源list属性拷贝成目标list
	 * @param source 源list
	 * @param clazz 目标list对象的类
	 * @return 新的集合
	 */
	public static <T> List<T> copyPropertiesList(List<?> source, Class<T> clazz, String... ignoreProperties)  {
		List<T> listRet = new ArrayList<>();
		if(!CollectionUtils.isEmpty(source)) {
			// 解析需要忽略的字段
			IgnoreProperty[] ignorePropertyArray = resolveIgnoreProperties(source.getClass().getSimpleName(), ignoreProperties);
			for (Object o : source) {
				listRet.add(copyPropertiesDeep(o, clazz, ignorePropertyArray));
			}
		} else {
			logger.debug("Source List is empty");
		}
		return listRet;
	}

	/**
	 * support DK1.8 {@link Optional}
	 */
	public static <T> Optional<T> copyPropertiesDeep(Optional<?> source, Class<T> clazz, String... ignoreProperties) {
		if(source.isPresent()) {
			T t = copyPropertiesDeep(source.get(), clazz, ignoreProperties);
			return Optional.ofNullable(t);
		} else {
			return Optional.empty();
		}
	}

	/**
	 * 将源对象的属性（包括聚合对象）拷贝到目标对象，目标对象必须为没有聚合的对象
	 * @param source 源对象
	 * @param clazz 目标对象类
	 * @return 新的对象
	 */
	public static <T> T copyPropertiesDeep(Object source, Class<T> clazz, String... ignoreProperties)  {
		if(source == null) {
			return null;
		}

		T target = null;
		try {
			target = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("init target object error");
		}
		// 解析需要忽略的字段
		IgnoreProperty[] ignorePropertyArray = resolveIgnoreProperties(source.getClass().getSimpleName(), ignoreProperties);
		copyPropertiesDeep(source, target, ignorePropertyArray);
		return target;
	}

	private static <T> T copyPropertiesDeep(Object source, Class<T> clazz, IgnoreProperty... ignoreProperties)  {
		if(source == null) {
			return null;
		}

		T target = null;
		try {
			target = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("init target object error");
		}
		// 解析需要忽略的字段
		copyPropertiesDeep(source, target, ignoreProperties);
		return target;
	}

	/**
	 * 将源对象的属性（包括聚合对象）拷贝到目标对象，目标对象必须为没有聚合的对象
	 * @param source 源对象
	 * @param target 目标对象，不能为空
	 * @param ignoreProperties 需要忽略的源对象类的字段格式为 比如  [类的简单名称（simpleName).]fieldName）,simpleName 可以忽略表示最外层的类
	 */
	public static void copyPropertiesDeep(Object source, Object target, String... ignoreProperties) {
		if(target == null) {
			logger.debug("Target object is null");
			return;
		}
		if(source == null) {
			logger.debug("Source object is null");
			return;
		}
		// 解析需要忽略的字段
		IgnoreProperty[] ignorePropertyArray = resolveIgnoreProperties(source.getClass().getSimpleName(), ignoreProperties);
		copyPropertiesDeep(source, target, ignorePropertyArray);
	}

	/**
	 * 将源对象的属性（包括聚合对象）拷贝到目标对象，目标对象必须为没有聚合的对象
	 * @param target 目标对象，不能为空
	 * @param source 源对象
	 */
	private static void copyPropertiesDeep(Object source, Object target, IgnoreProperty... ignoreProperties) {
		try {
			Map<String, TempBean> tbSourceMap = BeanUtils.findSourceMethodAndTranslateGetNameMap(source, ignoreProperties);
			List<TempBean> listTarget = BeanUtils.findSourceMethod(target, null);
			for (TempBean tempBean : listTarget) {
				try {
					//查找源对象的get方法
					TempBean tempBeanSource = tbSourceMap.get(tempBean.getFieldDesc().getGetName());
					//若不存在，则直接跳过
					if (tempBeanSource == null) {
						continue;
					}
					Method method = target.getClass().getMethod(tempBean.getFieldDesc().getSetName(), tempBean.getFieldDesc().getClazz());
					Object value = tempBeanSource.getObject().getClass().getMethod(tempBeanSource.getFieldDesc().getGetName(), new Class[]{}).invoke(tempBeanSource.getObject());
					//值为空不需要塞
					if (value == null) {
						continue;
					}
					method.invoke(target, value);
				} catch (NoSuchMethodException | SecurityException e) {
					logger.debug(tempBean.getFieldDesc().getSetName() + " is not exist, not covert!");
				}
			}
		} catch (Exception e) {
			logger.error("copy occur error: {}", e.getMessage());
		}
	}

	/**
	 * 查找对象源的get和set方法，包括聚合对象, 并转换成以get方法为key的Map
	 */
	protected static Map<String, TempBean> findSourceMethodAndTranslateGetNameMap(Object source, IgnoreProperty... ignoreProperties) throws Exception {
		List<TempBean> list = BeanUtils.findSourceMethod(source, null);
		Map<String, TempBean> retMap = new HashMap<>();

		// 区分是否有过滤的属性
		if(ignoreProperties == null || ignoreProperties.length == 0) {
			for(TempBean tb : list) {
				retMap.put(tb.getFieldDesc().getGetName(), tb);
			}
		} else {
			for(TempBean tb : list) {
				boolean flag = false;
				for(IgnoreProperty ignoreProperty : ignoreProperties) {
					if(ignoreProperty.equalsWith(tb.getObjectSimpleName(), tb.getFieldDesc().getFieldName())) {
						flag = true;
						break;
					}
				}
				if(flag) {
					continue;
				}
				retMap.put(tb.getFieldDesc().getGetName(), tb);
			}
		}

		return retMap;
	}

	protected static IgnoreProperty[] resolveIgnoreProperties(String simpleName, String... ignoreProperties) {
		if(ignoreProperties == null || ignoreProperties.length == 0) {
			return null;
		}

		IgnoreProperty[] ignorePropertyArray = new IgnoreProperty[ignoreProperties.length];
		for(int i=0; i<ignoreProperties.length; i++) {
			String ignorePropertyStr = ignoreProperties[i];
			String[] splitTmp = ignorePropertyStr.split("\\.");
			if(splitTmp.length == 1) {
				ignorePropertyArray[i] = new IgnoreProperty(simpleName, splitTmp[0]);
			} else {
				ignorePropertyArray[i] = new IgnoreProperty(splitTmp[0], splitTmp[1]);
			}
		}

		return ignorePropertyArray;
	}

	/**
	 * 查找对象源的get和set方法，包括聚合对象
	 */
	protected static List<TempBean> findSourceMethod(Object source, List<TempBean> list) throws Exception {
		if(list == null) {
			list = new ArrayList<>();
		}

		Class<?> clazzSource = source.getClass();

		List<FieldDesc> descList = new ArrayList<>();

		if(cache.containsKey(source.getClass())) {
			descList = cache.get(source.getClass());
		} else {

			List<Field> sourceFields = getAllFields(clazzSource, null);

			for (Field field : sourceFields) {
				if (isDirectConvert(field.getType())) {
					descList.add(new FieldDesc(field));
					continue;
				}
				if (!Collection.class.isAssignableFrom(field.getType())) {
					descList.add(new FieldDesc(field));
				}
			}

			cache.put(source.getClass(), descList);
		}

		for(FieldDesc fieldDesc : descList) {

			if(isDirectConvert(fieldDesc.getClazz())) {
				TempBean tempBean = new TempBean(fieldDesc, source);
				list.add(tempBean);
			} else {
				Field field = source.getClass().getDeclaredField(fieldDesc.getFieldName());
				Object nextO = getFieldObject(field, source);
				if (nextO != null) {
					findSourceMethod(nextO, list);
				}
			}
		}

		return list;
	}

	/**
	 * 只取类的第一层，不包括对象里的对象
	 */
	protected static List<TempBean> findSourceMethod(Object source) {
		List<TempBean> list = new ArrayList<>();
		Class<?> clazzSource = source.getClass();
		List<Field> sourceFields = getAllFields(clazzSource, null);

		for (Field field : sourceFields) {
			list.add(new TempBean(new FieldDesc(field), source));
		}
		return list;
	}

	private static boolean isDirectConvert(Class<?> clazz) {
		return String.class.equals(clazz) ||
				Date.class.equals(clazz) ||
				java.sql.Date.class.equals(clazz) ||
				Number.class.isAssignableFrom(clazz) ||
				Boolean.class.equals(clazz) ||
				clazz.isPrimitive() ||
				clazz.isEnum() ||
				Class.class.equals(clazz);
	}

	/**
	 * 获得类的所有字段，包括父级字段
	 */
	private static List<Field> getAllFields(Class<?> clazz, List<Field> list) {
		if(list == null) {
			list = new ArrayList<>();
		}
		Field[] fields = clazz.getDeclaredFields();
		for(Field field : fields) {
			if(!field.isAnnotationPresent(Transient.class)) {
				list.add(field);
			}
		}
		// list.addAll(Arrays.asList(fields));
		if(!"Object".equals(clazz.getSuperclass().getSimpleName())) {
			getAllFields(clazz.getSuperclass(), list);
		}

		return list;
	}

	/**
	 * 根据对象和字段名获得这个字段的对象值
	 */
	private static Object getFieldObject(Field field, Object source) throws InvocationTargetException, IllegalAccessException {
		String fieldName = field.getName();
		Method method;
		try {
			method = source.getClass().getMethod(JavaBeansUtil.getGetterMethodName(fieldName, field.getType().getSimpleName()));
		} catch (NoSuchMethodException e) {
			return null;
		}
		return method.invoke(source);
	}

	private static class IgnoreProperty {

		private String simpleName;

		private String property;

		public IgnoreProperty(String simpleName, String property) {
			this.simpleName = simpleName;
			this.property = property;
		}

		public String getSimpleName() {
			return simpleName;
		}

		public String getProperty() {
			return property;
		}

		public boolean equalsWith(String simpleName, String property) {
			return simpleName.equals(this.simpleName) && property.equals(this.property);
		}
	}

	protected static class TempBean {

		private FieldDesc fieldDesc;

		private Object object;

		private String objectSimpleName;

		public TempBean(FieldDesc fieldDesc, Object object) {
			this.fieldDesc = fieldDesc;
			this.object = object;
			this.objectSimpleName = object.getClass().getSimpleName();
		}

		public FieldDesc getFieldDesc() {
			return fieldDesc;
		}

		public Object getObject() {
			return object;
		}

		public String getObjectSimpleName() {
			return objectSimpleName;
		}
	}

	protected static class FieldDesc {

		private String fieldName;
		/** getter method name */
		private String getName;
		/** setter method name */
		private String setName;
		/** 参数或返回值类型 */
		private Class<?> clazz;

		public FieldDesc(Field field) {
			this.fieldName = field.getName();
			this.getName = JavaBeansUtil.getGetterMethodName(fieldName, field.getType().getSimpleName());
			this.setName = JavaBeansUtil.getSetterMethodName(fieldName);
			this.clazz = field.getType();
		}

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		public String getGetName() {
			return getName;
		}

		public void setGetName(String getName) {
			this.getName = getName;
		}

		public String getSetName() {
			return setName;
		}

		public void setSetName(String setName) {
			this.setName = setName;
		}

		public Class<?> getClazz() {
			return clazz;
		}

		public void setClazz(Class<?> clazz) {
			this.clazz = clazz;
		}
	}

}
