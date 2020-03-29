package com.exodus.arhaiyun.Birdies.database;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.osmapps.golf.common.bean.domain.BaseId;
import com.osmapps.golf.common.bean.domain.ObjectId;
import com.osmapps.golf.common.bean.domain.geo.GeoPoint;
import com.osmapps.golf.common.bean.domain.user.LocalPlayerId;
import com.osmapps.golf.common.bean.domain.user.PlayerId;
import com.osmapps.golf.common.bean.domain.user.UserId;
import com.osmapps.golf.common.util.Base64;
import com.osmapps.golf.common.util.EnumUtil;
import com.osmapps.golf.common.util.Joiners;
import com.osmapps.golf.common.util.ObjectUtil;

public class MapToObject {

  public static <T> T transform(Object object, Class<T> clazz) {
    return transform(object, clazz, null);
  }

  public static <T> T transform(Object object, Class<T> clazz, List<Class<?>> elementClazzes) {
    return transform(object, clazz, elementClazzes, null);
  }

  private static <T> T transform(Object object, Class<T> clazz, List<Class<?>> elementClazzes, Annotation[] annotations) {
    Preconditions.checkNotNull(clazz);
    if (object == null) {
      return null;
    }

    try {
      if (ObjectUtil.isBoolean(clazz)) {
        Preconditions.checkState(ObjectUtil.isBoolean(object));
        return (T) object;
      } else if (ObjectUtil.isChar(clazz)) {
        Preconditions.checkState(ObjectUtil.isChar(object));
        return (T) object;
      } else if (ObjectUtil.isNumber(clazz)) {
        return (T) transformNumber(object, (Class<? extends Number>) clazz);
      } else if (ObjectUtil.isString(clazz)) {
        Preconditions.checkState(ObjectUtil.isString(object));
        return (T) object;
      } else if (clazz == ObjectId.class) {
        // TODO
      } else if (ObjectUtil.isEnum(clazz)) {
        Preconditions.checkState(ObjectUtil.isString(object));
        String name = (String) object;
        return (T) EnumUtil.valueOf((Class<Enum>) clazz, name);
      } else if (ObjectUtil.isDate(clazz)) {
        return (T) transformDate(object, (Class<? extends Date>) clazz);
      } else if (ObjectUtil.isId(clazz)) {
        return (T) transformId(object, (Class<? extends BaseId>) clazz);
      } else if (ObjectUtil.isGeoPoint(clazz)) {
        return (T) transformGeoPoint(object, (Class<? extends GeoPoint>) clazz);
      } else if (ObjectUtil.isArray(clazz)) {
        return (T) transformArray(object, clazz, annotations);
      } else if (ObjectUtil.isList(clazz)) {
        Preconditions.checkNotNull(elementClazzes);
        Preconditions.checkArgument(elementClazzes.size() == 1);
        return (T) transformList(object, clazz, elementClazzes.get(0), annotations);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return null;
    /*
    try {
      } else if (isSet(object)) {
        return transformSet((Set<?>) object, annotations);
      } else if (isCollection(object)) {
        return transformCollectionExceptListOrSet((Collection<?>) object, annotations);
      } else if (isMap(object)) {
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
    */
  }

  private static Number transformNumber(Object object, Class<? extends Number> clazz) {
    Preconditions.checkState(ObjectUtil.isNumber(object));
    Number number = (Number) object;
    if (clazz == byte.class || clazz == Byte.class) {
      return number.byteValue();
    } else if (clazz == short.class || clazz == Short.class) {
      return number.shortValue();
    } else if (clazz == int.class || clazz == Integer.class) {
      return number.intValue();
    } else if (clazz == long.class || clazz == Long.class) {
      return number.longValue();
    } else if (clazz == float.class || clazz == Float.class) {
      return number.floatValue();
    } else if (clazz == double.class || clazz == Double.class) {
      return number.doubleValue();
    } else {
      Preconditions.checkState(false);
      return null;
    }
  }

  private static Date transformDate(Object object, Class<? extends Date> clazz) {
    Preconditions.checkState(object.getClass() == Long.class);
    long timestamp = (Long) object;
    if (timestamp == 0L) {
      return null;
    } else {
      // TODO hard coded to java.util.Date
      return new Date((Long) object);
    }
  }

  public static BaseId transformId(Object object, Class<? extends BaseId> clazz) {
    Preconditions.checkState(ObjectUtil.isString(object));
    final String id = (String) object;
    if (clazz.equals(PlayerId.class)) {
      if (id.contains(Joiners.FIELD_SEPARATOR)) {
        if (id.length() == 1) {
          return LocalPlayerId.ME;
        } else {
          return new LocalPlayerId(id);
        }
      } else {
        return new UserId(id);
      }
    } else {
      try {
        Constructor<? extends BaseId> constructor = clazz.getConstructor(String.class);
        Preconditions.checkNotNull(constructor);
        return constructor.newInstance(id);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static GeoPoint transformGeoPoint(Object object, Class<? extends GeoPoint> clazz) {
    Preconditions.checkState(ObjectUtil.isMap(object));
    Map<String, Object> map = (Map<String, Object>) object;
    double[] coordinates = (double[]) map.get("coordinates");
    Preconditions.checkNotNull(coordinates);
    // TODO hard coded to GeoPoint
    return GeoPoint.fromArray(coordinates);
  }

  private static Object transformArray(Object object, Class<?> clazz, Annotation[] annotations) {
    if (ObjectUtil.isBasicType(clazz.getComponentType())) {
      if (clazz.getComponentType().equals(byte.class)) {
        if (ObjectToMap.hasRawAnnotation(annotations)) {
          Preconditions.checkState(ObjectUtil.isArray(object));
          Preconditions.checkState(object.getClass().getComponentType().equals(byte.class));
          return object;
        } else {
          Preconditions.checkState(ObjectUtil.isString(object));
          return Base64.decode((String) object);
        }
      } else {
        Preconditions.checkState(ObjectUtil.isArray(object));
        Preconditions.checkState(object.getClass().getComponentType().equals(clazz.getComponentType()));
        return object;
      }
    } else {
      // TODO object may be List
      Preconditions.checkState(ObjectUtil.isArray(object));
      Object[] transformedArray = (Object[]) object;
      Object[] array = new Object[transformedArray.length];
      int index = 0;
      for (Object transformedElement : transformedArray) {
        // TODO elementClazzes
        array[index++] = transform(transformedElement, clazz.getComponentType(), null, annotations);
      }
      return array;
    }
  }

  private static Object transformList(Object object, Class<?> clazz, Class<?> elementClazz, Annotation[] annotations) {
    Preconditions.checkState(ObjectUtil.isList(object));
    List<Object> transformedList = (List<Object>) newInstance(clazz);
    List<Object> list = Lists.newArrayListWithCapacity(transformedList.size());
    for (Object transformedElement : transformedList) {
      // TODO elementClazzes
      list.add(transform(transformedElement, elementClazz, null, annotations));
    }
    return list;
  }

  private static Object newInstance(Class<?> clazz) {
    // TODO
    return null;
  }
}
