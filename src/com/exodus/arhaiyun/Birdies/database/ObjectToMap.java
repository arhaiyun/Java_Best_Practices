package com.exodus.arhaiyun.Birdies.database;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.osmapps.golf.common.apiservice.InheritanceMeta;
import com.osmapps.golf.common.apiservice.NoPersistence;
import com.osmapps.golf.common.apiservice.NotNull;
import com.osmapps.golf.common.apiservice.Raw;
import com.osmapps.golf.common.apiservice.Subclass;
import com.osmapps.golf.common.bean.domain.BaseId;
import com.osmapps.golf.common.bean.domain.ObjectId;
import com.osmapps.golf.common.bean.domain.geo.GeoPoint;
import com.osmapps.golf.common.util.Base64;
import com.osmapps.golf.common.util.ObjectUtil;

public class ObjectToMap {

  private static Gson gson;
  private static Map<Class<?>, List<Field>> fieldCache = Maps.newConcurrentMap();

  @VisibleForTesting
  public static Gson getGson() {
    return gson;
  }

  public static void setGson(Gson gson) {
    ObjectToMap.gson = gson;
  }

  public static <T> T fromJsonString(String jsonString, Class<? extends T> clazz) {
    return gson.fromJson(jsonString, clazz);
  }

  public static <T> T fromJsonString(String json, Type typeOfT) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(json));
    Preconditions.checkNotNull(typeOfT);
    return gson.fromJson(json, typeOfT);
  }

  public static String toJsonString(Object o) {
    return gson.toJson(o);
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> toMap(Object object) {
    Object transformed = transform(object);
    if (!(transformed instanceof Map)) {
      throw new RuntimeException("object is illegal: " + object.toString());
    }
    return (Map<String, Object>) transformed;
  }

  public static Object transform(Object object) {
    return transform(object, null);
  }

  private static Object transform(Object object, Annotation[] annotations) {
    if (object == null) {
      return null;
    }

    try {
      if (ObjectUtil.isBasicType(object)) {
        return object;
      } else if (object.getClass() == ObjectId.class) {
        return new org.bson.types.ObjectId(((ObjectId) object).getId());
      } else if (ObjectUtil.isEnum(object)) {
        return ((Enum<?>) object).name();
      } else if (ObjectUtil.isDate(object)) {
        return transformDate((Date) object);
      } else if (ObjectUtil.isId(object)) {
        return ((BaseId) object).getId();
      } else if (ObjectUtil.isGeoPoint(object)) {
        return transformGeoPoint((GeoPoint) object);
      } else if (ObjectUtil.isArray(object)) {
        return transformArray(object, annotations);
      } else if (ObjectUtil.isList(object)) {
        return transformList((List<?>) object, annotations);
      } else if (ObjectUtil.isSet(object)) {
        return transformSet((Set<?>) object, annotations);
      } else if (ObjectUtil.isCollection(object)) {
        return transformCollectionExceptListOrSet((Collection<?>) object, annotations);
      } else if (ObjectUtil.isMap(object)) {
        return transformMap((Map<?, ?>) object);
      } else {
        Map<String, Object> map = Maps.newLinkedHashMap();
        List<Field> fields = getFieldsOfComplexObject(object);
        for (Field field : fields) {
          String fieldName = field.getName();
          Object value = field.get(object);
          Annotation[] fieldAnnotations = field.getAnnotations();
          if (hasNotNullAnnotation(fieldAnnotations)) {
            assertValueNotNull(value, fieldName, field.getType(), object.getClass());
          }
          putToMap(map, fieldName, transform(value, fieldAnnotations));
        }
        if (hasSubclassAnnotation(annotations)) {
          String classSimpleName = object.getClass().getSimpleName();
          int dollarIndex = classSimpleName.lastIndexOf("$");
          if (dollarIndex >= 0) {
            classSimpleName = classSimpleName.substring(dollarIndex + 1);
          }
          putToMap(map, InheritanceMeta.TYPE_FIELD, classSimpleName);
        }
        return map;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void putToMap(Map<String, Object> map, String fieldName, Object value) {
    if (map.containsKey(fieldName)) {
      throw new RuntimeException("duplicated field name detected: " + fieldName);
    }
    if (value != null) {
      map.put(fieldName, value);
    }
  }

  private static Object transformDate(Date date) {
    long timestamp = date.getTime();
    return timestamp;
  }

  private static Object transformGeoPoint(GeoPoint geoPoint) {
    Map<String, Object> map = Maps.newLinkedHashMap();
    map.put("type", "Point");
    map.put("coordinates", geoPoint.toArray());
    return map;
  }

  private static Object transformArray(Object object, Annotation[] annotations) throws Exception {
    Class<?> clazz = object.getClass();
    if (ObjectUtil.isBasicType(clazz.getComponentType())) {
      if (clazz.getComponentType().equals(byte.class)) {
        if (hasRawAnnotation(annotations)) {
          return object;
        } else {
          String base64 = Base64.encodeToString((byte[]) object);
          return base64;
        }
      } else {
        return object;
      }
    } else {
      Object[] array = (Object[]) object;
      Object[] transformedArray = new Object[array.length];
      int index = 0;
      for (Object element : array) {
        transformedArray[index++] = transform(element, annotations);
      }
      return transformedArray;
    }
  }

  private static List<Object> transformList(List<?> list, Annotation[] annotations)
      throws Exception {
    List<Object> transformedList = Lists.newArrayListWithCapacity(list.size());
    for (Object element : list) {
      transformedList.add(transform(element, annotations));
    }
    return transformedList;
  }

  private static List<Object> transformSet(Set<?> set, Annotation[] annotations) throws Exception {
    List<Object> transformedList = Lists.newArrayListWithCapacity(set.size());
    for (Object element : set) {
      transformedList.add(transform(element, annotations));
    }
    return transformedList;
  }

  private static Object transformCollectionExceptListOrSet(Collection<?> collection,
      Annotation[] annotations) throws Exception {
    throw new RuntimeException("collection (except for list or set) field is not supported yet");
  }

  private static Map<String, Object> transformMap(Map<?, ?> map) throws Exception {
    Map<String, Object> transformedMap = Maps.newLinkedHashMap();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      Object key = entry.getKey();
      Object value = entry.getValue();
      if (ObjectUtil.isBasicType(key)) {
        putToMap(transformedMap, key.toString(), transform(value));
      } else if (ObjectUtil.isEnum(key)) {
        putToMap(transformedMap, ((Enum<?>) key).name(), transform(value));
      } else if (ObjectUtil.isId(key)) {
        putToMap(transformedMap, ((BaseId) key).getId(), transform(value));
      } else {
        throw new RuntimeException("Map's key cannot be complex type");
      }
    }
    return transformedMap;
  }

  static boolean isComplexType(Class<?> clazz) {
    return !(ObjectUtil.isBasicType(clazz) || ObjectUtil.isEnum(clazz) || ObjectUtil.isDate(clazz)
        || ObjectUtil.isId(clazz) || ObjectUtil.isGeoPoint(clazz) || ObjectUtil.isArray(clazz)
        || ObjectUtil.isCollection(clazz) || ObjectUtil.isMap(clazz));
  }

  private static List<Field> getFieldsOfComplexObject(Object object) {
    Class<?> clazz = object.getClass();
    if (!isComplexType(clazz)) {
      throw new RuntimeException("internal error");
    }
    return fieldsOf(clazz);
  }

  public static List<Field> fieldsOf(Class<?> clazz) {
    Preconditions.checkNotNull(clazz);
    List<Field> fields = fieldCache.get(clazz);
    if (fields == null) {
      fields = Lists.newArrayList();
      fieldCache.put(clazz, fields);
      while (clazz != null) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
          int modifiers = field.getModifiers();
          if ((modifiers & Modifier.STATIC) != 0 || (modifiers & Modifier.TRANSIENT) != 0) {
            continue;
          }
          if (field.getAnnotation(NoPersistence.class) != null) {
            continue;
          }
          field.setAccessible(true);
          fields.add(field);
        }
        clazz = clazz.getSuperclass();
      }
    }
    return fields;
  }

  static boolean hasRawAnnotation(Annotation[] annotations) {
    if (annotations != null) {
      for (Annotation annotation : annotations) {
        if (annotation.annotationType().equals(Raw.class)) {
          return true;
        }
      }
    }
    return false;
  }

  static boolean hasNotNullAnnotation(Annotation[] annotations) {
    if (annotations != null) {
      for (Annotation annotation : annotations) {
        if (annotation.annotationType().equals(NotNull.class)) {
          return true;
        }
      }
    }
    return false;
  }

  static boolean hasSubclassAnnotation(Annotation[] annotations) {
    if (annotations != null) {
      for (Annotation annotation : annotations) {
        if (annotation.annotationType().equals(Subclass.class)) {
          return true;
        }
      }
    }
    return false;
  }

  static void assertValueNotNull(Object value, String fieldName, Class<?> fieldType,
      Class<?> ofClazz) {
    Preconditions.checkNotNull(value,
        fieldName + " of class " + ofClazz.getSimpleName() + " cannot be null");
    if (String.class.equals(fieldType)) {
      Preconditions.checkState(((String) value).length() > 0,
          fieldName + " of class " + ofClazz.getSimpleName() + " cannot be empty");
      // } else if (isCollection(fieldType)) {
      // Preconditions.checkState(((Collection<?>) value).size() > 0,
      // fieldName + " of class " + ofClazz.getSimpleName() + " cannot be empty");
    } else if (ObjectUtil.isMap(fieldType)) {
      Preconditions.checkState(((Map<?, ?>) value).size() > 0,
          fieldName + " of class " + ofClazz.getSimpleName() + " cannot be empty");
    }
  }
}