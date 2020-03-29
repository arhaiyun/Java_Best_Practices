package com.exodus.arhaiyun.Birdies.database;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.osmapps.golf.common.apiservice.IndexEmbeddedListFieldIndexes;
import com.osmapps.golf.common.bean.domain.geo.GeoPoint;
import com.osmapps.golf.common.util.CollectionUtil;
import com.osmapps.golf.common.util.FieldMeta;
import com.osmapps.golf.common.util.ObjectUtil;

public class Query {

  public static double EARTH_RADIUS = 6378.137d;
  private static Map<Class<?>, Map<String, Class<?>>> fieldInfoCache = Maps.newConcurrentMap();
  private static Map<Class<?>, Map<String, Class<?>>> fieldActualTypeArgumentInfoCache =
      Maps.newConcurrentMap();

  private QueryBuilder queryBuilder;
  private BasicDBObject sortBy;
  private int limit;
  private int skip;
  private BasicDBObject projection;
  private BasicDBObject indexHint;
  private boolean forcePrimary;

  private FieldMeta currentfieldMeta;

  private Query() {
  }

  public static Query newQuery() {
    return new Query();
  }

  public static Query of(FieldMeta fieldMeta) {
    return newQuery().set(fieldMeta);
  }

  public static Query satisfyOneOf(Query... queries) {
    return newQuery().setSatisfyOneOf(queries);
  }

  public static Query satisfyAllOf(Query... queries) {
    return newQuery().setSatisfyAllOf(queries);
  }

  public Query andOneOf(Query... queries) {
    return setSatisfyOneOf(queries);
  }

  public Query andAllOf(Query... queries) {
    return setSatisfyAllOf(queries);
  }

  private Query set(FieldMeta fieldMeta) {
    this.currentfieldMeta = fieldMeta;
    return this;
  }

  public Query and(FieldMeta fieldMeta) {
    return set(fieldMeta);
  }

  private Query setSatisfyOneOf(Query... queries) {
    Preconditions.checkArgument(queries.length > 0);
    for (Query query : queries) {
      DBObject dbObject = query.toRawQuery();
      Preconditions.checkNotNull(dbObject);
      getQueryBuilder().or(dbObject);
    }
    return this;
  }

  private Query setSatisfyAllOf(Query... queries) {
    Preconditions.checkArgument(queries.length > 0);
    for (Query query : queries) {
      DBObject dbObject = query.toRawQuery();
      Preconditions.checkNotNull(dbObject);
      getQueryBuilder().and(dbObject);
    }
    return this;
  }

  public Query andSatisfyOneOf(Query... queries) {
    return setSatisfyOneOf(queries);
  }

  public Query andSatisfyAllOf(Query... queries) {
    return setSatisfyAllOf(queries);
  }

  public Query is(Object what) {
    assertValidity(currentfieldMeta, what);
    getQueryBuilder().put(currentfieldMeta.getFieldName()).is(ObjectToMap.transform(what));
    return this;
  }

  public Query keyOfMapIs(String key, Object what) {
    assertFieldExists(currentfieldMeta);
    getQueryBuilder().put(currentfieldMeta.getFieldName() + "." + key)
        .is(ObjectToMap.transform(what));
    return this;
  }

  public Query notEquals(Object what) {
    assertValidity(currentfieldMeta, what);
    getQueryBuilder().put(currentfieldMeta.getFieldName()).notEquals(ObjectToMap.transform(what));
    return this;
  }

  public Query lessThan(Object what) {
    assertValidity(currentfieldMeta, what);
    getQueryBuilder().put(currentfieldMeta.getFieldName()).lessThan(ObjectToMap.transform(what));
    return this;
  }

  public Query lessOrEquals(Object what) {
    assertValidity(currentfieldMeta, what);
    getQueryBuilder().put(currentfieldMeta.getFieldName())
        .lessThanEquals(ObjectToMap.transform(what));
    return this;
  }

  public Query greaterThan(Object what) {
    assertValidity(currentfieldMeta, what);
    getQueryBuilder().put(currentfieldMeta.getFieldName()).greaterThan(ObjectToMap.transform(what));
    return this;
  }

  public Query greaterOrEquals(Object what) {
    assertValidity(currentfieldMeta, what);
    getQueryBuilder().put(currentfieldMeta.getFieldName())
        .greaterThanEquals(ObjectToMap.transform(what));
    return this;
  }

  public Query in(Collection<?> collection) {
    assertValidity(currentfieldMeta, collection);
    Collection<Object> in = Lists.newArrayListWithCapacity(collection.size());
    for (Object what : collection) {
      in.add(ObjectToMap.transform(what));
    }
    getQueryBuilder().put(currentfieldMeta.getFieldName()).in(in);
    return this;
  }

  public Query notIn(Collection<?> collection) {
    assertValidity(currentfieldMeta, collection);
    Collection<Object> notIn = Lists.newArrayListWithCapacity(collection.size());
    for (Object what : collection) {
      notIn.add(ObjectToMap.transform(what));
    }
    getQueryBuilder().put(currentfieldMeta.getFieldName())
        .notIn(notIn);
    return this;
  }

  public Query regex(Pattern pattern) {
    Preconditions.checkNotNull(pattern);
    assertFieldExists(currentfieldMeta);
    getQueryBuilder().put(currentfieldMeta.getFieldName()).regex(pattern);
    return this;
  }

  public Query near(GeoPoint geoPoint) {
    assertValidity(currentfieldMeta, geoPoint);
    getQueryBuilder().put(currentfieldMeta.getFieldName()).nearSphere(geoPoint.getLongitude(),
        geoPoint.getLatitude());
    return this;
  }

  /**
   * near clause query
   *
   * @param geoPoint
   * @param maxDistance in meters
   * @return
   */
  public Query near(GeoPoint geoPoint, double maxDistance) {
    assertValidity(currentfieldMeta, geoPoint);
    getQueryBuilder().put(currentfieldMeta.getFieldName()).nearSphere(geoPoint.getLongitude(),
        geoPoint.getLatitude(), maxDistance / (EARTH_RADIUS * 1000));
    return this;
  }

  public Query contains(Object what) {
    assertValidityForListElement(currentfieldMeta, what);
    getQueryBuilder().put(currentfieldMeta.getFieldName()).is(ObjectToMap.transform(what));
    return this;
  }

  public Query containsInList(Object what) {
    // TODO validate
    getQueryBuilder().put(currentfieldMeta.getFieldName()).is(ObjectToMap.transform(what));
    return this;
  }

  public Query contains(Pattern pattern) {
    assertValidityForListElement(currentfieldMeta, pattern);
    getQueryBuilder().put(currentfieldMeta.getFieldName()).regex(pattern);
    return this;
  }

  public Query elemMatchInCollection(Collection<?> collection) {
    // TODO validate
    Collection<Object> in = Lists.newArrayListWithCapacity(collection.size());
    for (Object what : collection) {
      in.add(ObjectToMap.transform(what));
    }
    getQueryBuilder().put(currentfieldMeta.getFieldName()).elemMatch(new BasicDBObject("$in", in));
    return this;
  }

  public Query exists() {
    getQueryBuilder().put(currentfieldMeta.getFieldName()).exists(true);
    return this;
  }

  public Query notExists() {
    getQueryBuilder().put(currentfieldMeta.getFieldName()).exists(false);
    return this;
  }

  // TODO add other operations supported by QueryBuilder

  private QueryBuilder getQueryBuilder() {
    if (queryBuilder == null) {
      queryBuilder = new QueryBuilder();
    }
    return queryBuilder;
  }

  DBObject toRawQuery() {
    return queryBuilder == null ? null : queryBuilder.get();
  }

  public Query sort() {
    return sort(true);
  }

  public Query sort(boolean ascending) {
    assertFieldExists(currentfieldMeta);
    if (sortBy == null) {
      sortBy = new BasicDBObject();
    }
    sortBy.put(currentfieldMeta.getFieldName(), ascending ? 1 : -1);
    return this;
  }

  DBObject getSort() {
    return sortBy;
  }

  public Query limit(int limit) {
    this.limit = limit;
    return this;
  }

  int getLimit() {
    return limit;
  }

  public Query skip(int skip) {
    this.skip = skip;
    return this;
  }

  int getSkip() {
    return skip;
  }

  public Query includeFields(FieldMeta... fieldMetas) {
    assertProjectionValidity(true);
    for (FieldMeta fieldMeta : fieldMetas) {
      Preconditions.checkNotNull(fieldMeta);
      assertFieldExists(fieldMeta);
      if (projection == null) {
        projection = new BasicDBObject();
      }
      projection.append(fieldMeta.getFieldName(), 1);
    }
    return this;
  }

  public Query excludeFields(FieldMeta... fieldMetas) {
    assertProjectionValidity(false);
    for (FieldMeta fieldMeta : fieldMetas) {
      Preconditions.checkNotNull(fieldMeta);
      assertFieldExists(fieldMeta);
      if (projection == null) {
        projection = new BasicDBObject();
      }
      projection.append(fieldMeta.getFieldName(), 0);
    }
    return this;
  }

  public Query excludeInternalIdField() {
    if (projection == null) {
      projection = new BasicDBObject();
    }
    projection.append("_id", 0);
    return this;
  }

  DBObject getProjection() {
    return projection;
  }

  public Query indexBy(String key, Object type) {
    if (indexHint == null) {
      indexHint = new BasicDBObject();
    }
    indexHint.append(key, type);
    return this;
  }

  public Query compound(String key, Object type) {
    return indexBy(key, type);
  }

  DBObject getIndexHint() {
    return indexHint;
  }

  public boolean isForcePrimary() {
    return forcePrimary;
  }

  /**
   * only valid for findOne
   */
  public Query forcePrimary() {
    this.forcePrimary = true;
    return this;
  }

  private void assertProjectionValidity(boolean isInclude) {
    if (projection != null) {
      for (String key : projection.keySet()) {
        if (key.equals("_id")) {
          continue;
        }
        int value = projection.getInt(key);
        if (isInclude) {
          Preconditions.checkState(value == 1, "cannot use include and exclude simultaneously");
        } else {
          Preconditions.checkState(value == 0, "cannot use include and exclude simultaneously");
        }
      }
    }
  }

  private static void assertValidity(FieldMeta fieldMeta, Object what) {
    Preconditions.checkNotNull(fieldMeta);
    // what can be null
    Class<?> fieldType = assertFieldExists(fieldMeta);
    assertTypeSafe(fieldType, what);
  }

  private static void assertValidity(FieldMeta fieldMeta, Collection<?> collection) {
    Preconditions.checkNotNull(fieldMeta);
    Preconditions.checkArgument(!CollectionUtil.isNullOrEmpty(collection));
    Class<?> fieldType = assertFieldExists(fieldMeta);
    for (Object what : collection) {
      assertTypeSafe(fieldType, what);
    }
  }

  private static void assertValidityForListElement(FieldMeta fieldMeta, Object what) {
    Preconditions.checkNotNull(fieldMeta);
    Preconditions.checkNotNull(what);
    Class<?> fieldType = assertFieldExists(fieldMeta);
    Preconditions.checkState(ObjectUtil.isList(fieldType) || ObjectUtil.isSet(fieldType),
        "contains only works with list or set type");
    // TODO check generic type of list
  }

  static Class<?> assertFieldExists(FieldMeta fieldMeta) {
    return assertFieldExists(fieldMeta, true);
  }

  static Class<?> assertFieldExists(FieldMeta fieldMeta, boolean returnFieldActualTypeArgument) {
    Preconditions.checkNotNull(fieldMeta);
    int firstDotIndex = fieldMeta.getFieldName().indexOf(".");
    if (firstDotIndex >= 0) {
      Preconditions.checkState(firstDotIndex > 0);
      String fieldName = fieldMeta.getFieldName().substring(0, firstDotIndex);
      if (isNumber(fieldName)) {
        // TODO continue to validate array
        return Object.class;
      }
      Class<?> fieldType = assertFieldExists(FieldMeta.of(fieldName, fieldMeta.getOfClazz()));
      String remaining = fieldMeta.getFieldName().substring(firstDotIndex + 1);
      Preconditions.checkState(remaining.length() > 0);
      if (ObjectUtil.isCollection(fieldType)) {
        // TODO continue to validate List
        return Object.class;
      }
      return assertFieldExists(FieldMeta.of(remaining, fieldType), returnFieldActualTypeArgument);
    } else {
      Map<String, Class<?>> fieldInfos = getFieldInfos(fieldMeta.getOfClazz());
      final String fieldName = fieldMeta.getFieldName();
      Class<?> fieldClz = fieldInfos.get(fieldName);
      if (returnFieldActualTypeArgument) {
        Class<?> fieldActualTypeArgument = getFieldActualTypeArgument(fieldMeta);
        if (fieldActualTypeArgument != null) {
          fieldClz = fieldActualTypeArgument;
        }
      }
      Preconditions.checkState(fieldClz != null, fieldMeta.getFieldName() + " of class "
          + fieldMeta.getOfClazz().getSimpleName() + " doesn't exist");
      return fieldClz;
    }
  }

  static Class<?> getFieldActualTypeArgument(FieldMeta fieldMeta) {
    Preconditions.checkNotNull(fieldMeta);
    int firstDotIndex = fieldMeta.getFieldName().indexOf(".");
    Preconditions.checkState(firstDotIndex < 0);
    final String fieldName = fieldMeta.getFieldName();
    Map<String, Class<?>> fieldActualTypeArgumentInfos =
        fieldActualTypeArgumentInfoCache.get(fieldMeta.getOfClazz());

    return fieldActualTypeArgumentInfos != null ? fieldActualTypeArgumentInfos.get(fieldName)
        : null;
  }

  static void assertTypeSafe(Class<?> fieldType, Object what) {
    Preconditions.checkNotNull(fieldType);
    if (what == null) {
      Preconditions.checkState(!fieldType.isPrimitive());
    } else {
      if (fieldType.isPrimitive()) {
        Preconditions.checkState(isPrimitiveTypeMatch(fieldType, what.getClass()), "expected type "
            + fieldType.getSimpleName() + ", actual type " + what.getClass().getSimpleName());
      } else {
        Preconditions.checkState(fieldType.isInstance(what), "expected type "
            + fieldType.getSimpleName() + ", actual type " + what.getClass().getSimpleName());
      }
    }
  }

  private static Map<String, Class<?>> getFieldInfos(Class<?> clazz) {
    Map<String, Class<?>> fieldInfos = fieldInfoCache.get(clazz);
    if (fieldInfos == null) {
      // use synchronized to prevent disorder execution
      synchronized (Query.class) {
        List<Field> fields = ObjectToMap.fieldsOf(clazz);
        fieldInfos = Maps.newConcurrentMap();
        Map<String, Class<?>> fieldActualTypeArgumentInfos = Maps.newConcurrentMap();
        for (Field field : fields) {
          fieldInfos.put(field.getName(), getFieldClass(field));
          Class<?> actualTypeArgument = getActualTypeArgumentForListField(field);
          if (actualTypeArgument != null) {
            fieldActualTypeArgumentInfos.put(field.getName(), actualTypeArgument);
          }
        }
        fieldInfoCache.put(clazz, fieldInfos);
        fieldActualTypeArgumentInfoCache.put(clazz, fieldActualTypeArgumentInfos);
      }
    }
    return fieldInfos;
  }

  public static Class<?> getFieldClass(Field field) {
    Preconditions.checkNotNull(field);
    return field.getType();
  }

  public static Class<?> getActualTypeArgumentForListField(Field field) {
    Preconditions.checkNotNull(field);
    if (field.getAnnotation(IndexEmbeddedListFieldIndexes.class) == null) {
      return null;
    }
    Type fieldGenericType = field.getGenericType();
    Preconditions.checkState(fieldGenericType instanceof ParameterizedType,
        "invalid field generic type %s", fieldGenericType);
    ParameterizedType fieldParameterizedType = (ParameterizedType) fieldGenericType;
    Type fieldRawType = fieldParameterizedType.getRawType();
    Preconditions.checkState(fieldRawType == List.class,
        "field raw type must be java.util.List, actual is %s", fieldRawType);
    Type[] actualTypeArguments = fieldParameterizedType.getActualTypeArguments();
    Preconditions.checkState(actualTypeArguments != null && actualTypeArguments.length == 1,
        "actual types arguments should only contain one element, but actual is %s",
        actualTypeArguments.toString());
    return (Class<?>) actualTypeArguments[0];
  }

  private static boolean isPrimitiveTypeMatch(Class<?> primitiveType, Class<?> type) {
    if (primitiveType.equals(boolean.class)) {
      return type.equals(Boolean.class) || type.equals(boolean.class);
    } else if (primitiveType.equals(byte.class)) {
      return type.equals(Byte.class) || type.equals(byte.class);
    } else if (primitiveType.equals(char.class)) {
      return type.equals(Character.class) || type.equals(char.class);
    } else if (primitiveType.equals(short.class)) {
      return type.equals(Short.class) || type.equals(short.class);
    } else if (primitiveType.equals(int.class)) {
      return type.equals(Integer.class) || type.equals(int.class);
    } else if (primitiveType.equals(long.class)) {
      return type.equals(Long.class) || type.equals(long.class);
    } else if (primitiveType.equals(float.class)) {
      return type.equals(Float.class) || type.equals(float.class);
    } else if (primitiveType.equals(double.class)) {
      return type.equals(Double.class) || type.equals(double.class);
    }
    return false;
  }

  static boolean isNumber(String fieldName) {
    try {
      Integer.parseInt(fieldName);
      return true;
    } catch (RuntimeException ex) {
      return false;
    }
  }

  @Override
  public String toString() {
    String string = getQueryBuilder().get().toString();
    if (getIndexHint() != null) {
      string += ", hint=" + getIndexHint().toString();
    }
    return string;
  }
}
