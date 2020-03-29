package com.exodus.arhaiyun.Birdies.database;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.osmapps.golf.common.util.FieldMeta;

public class Modifier {

  private static Map<Class<?>, Map<String, Boolean>> fieldNotNullCache = Maps.newConcurrentMap();

  private BasicDBObject updateTo;

  private FieldMeta currentfieldMeta;

  private int currentArrayIndex = -1;
  private int currentArrayIndex2 = -1;

  private Modifier() {
  }

  public static Modifier newModifier() {
    return new Modifier();
  }

  public static Modifier of(FieldMeta fieldMeta) {
    return newModifier().set(fieldMeta);
  }

  private Modifier set(FieldMeta fieldMeta) {
    currentfieldMeta = fieldMeta;
    return this;
  }

  public Modifier and(FieldMeta fieldMeta) {
    return set(fieldMeta);
  }

  public Modifier arrayIndex(int arrayIndex) {
    return arrayIndex(arrayIndex, -1);
  }

  public Modifier arrayIndex(int arrayIndex, int arrayIndex2) {
    Preconditions.checkArgument(arrayIndex >= 0);
    Preconditions.checkArgument(arrayIndex2 >= -1);
    currentArrayIndex = arrayIndex;
    currentArrayIndex2 = arrayIndex2;
    return this;
  }

  @SuppressWarnings("unchecked")
  public Modifier setTo(Object what) {
    if (currentArrayIndex == -1) {
      assertValidityForUpdate(currentfieldMeta, what);
    } else {
      assertValidityForArrayUpdate(currentfieldMeta, what);
    }
    if (what == null) {
      Map<String, Object> map = (Map<String, Object>) getUpdateTo().get("$unset");
      if (map == null) {
        map = Maps.newLinkedHashMap();
        getUpdateTo().put("$unset", map);
      }
      if (currentArrayIndex == -1) {
        map.put(currentfieldMeta.getFieldName(), 1);
      } else {
        String fieldName = currentfieldMeta.getFieldName() + "." + currentArrayIndex;
        if (currentArrayIndex2 >= 0) {
          fieldName += "." + currentArrayIndex2;
        }
        map.put(fieldName, 1);
      }

      processRelatedMaps("$set", false);
      processRelatedMaps("$unset", true);
    } else {
      Map<String, Object> map = (Map<String, Object>) getUpdateTo().get("$set");
      if (map == null) {
        map = Maps.newLinkedHashMap();
        getUpdateTo().put("$set", map);
      }
      if (currentArrayIndex == -1) {
        map.put(currentfieldMeta.getFieldName(), ObjectToMap.transform(what));
      } else {
        String fieldName = currentfieldMeta.getFieldName() + "." + currentArrayIndex;
        if (currentArrayIndex2 >= 0) {
          fieldName += "." + currentArrayIndex2;
        }
        map.put(fieldName, ObjectToMap.transform(what));
      }

      // remove from $unset
      processRelatedMaps("$unset", false);
      processRelatedMaps("$set", true);
    }

    currentArrayIndex = -1;
    currentArrayIndex2 = -1;
    return this;
  }

  private void processRelatedMaps(String mapName, boolean onlyRemoveChild) {
    @SuppressWarnings("unchecked")
    Map<String, Object> map = (Map<String, Object>) getUpdateTo().get(mapName);
    if (map != null) {
      String fieldName = null;
      if (currentArrayIndex == -1) {
        fieldName = currentfieldMeta.getFieldName();
      } else {
        fieldName = currentfieldMeta.getFieldName() + "." + currentArrayIndex;
        if (currentArrayIndex2 >= 0) {
          fieldName += "." + currentArrayIndex2;
        }
      }
      Set<String> keys = Sets.newHashSet(map.keySet());
      for (String key : keys) {
        if ((!onlyRemoveChild && key.equals(fieldName)) || isChild(key, fieldName)) {
          map.remove(key);
        }
      }
      if (map.isEmpty()) {
        getUpdateTo().remove(mapName);
      }
    }
  }

  private static boolean isChild(String child, String parent) {
    if (Strings.isNullOrEmpty(child)) {
      return false;
    }
    if (child.length() > parent.length() && child.startsWith(parent)) {
      char nextChar = child.charAt(parent.length());
      return nextChar == '.';
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public Modifier increaseBy(int amount) {
    if (currentArrayIndex == -1) {
      assertValidityForUpdate(currentfieldMeta, amount);
    } else {
      assertValidityForArrayUpdate(currentfieldMeta, amount);
    }
    Map<String, Object> map = (Map<String, Object>) getUpdateTo().get("$inc");
    if (map == null) {
      map = Maps.newLinkedHashMap();
      getUpdateTo().put("$inc", map);
    }
    if (currentArrayIndex == -1) {
      map.put(currentfieldMeta.getFieldName(), amount);
    } else {
      String fieldName = currentfieldMeta.getFieldName() + "." + currentArrayIndex;
      if (currentArrayIndex2 >= 0) {
        fieldName += "." + currentArrayIndex2;
      }
      map.put(fieldName, amount);
    }

    currentArrayIndex = -1;
    currentArrayIndex2 = -1;
    return this;
  }

  @SuppressWarnings("unchecked")
  public Modifier increaseBy(long amount) {
    if (currentArrayIndex == -1) {
      assertValidityForUpdate(currentfieldMeta, amount);
    } else {
      assertValidityForArrayUpdate(currentfieldMeta, amount);
    }
    Map<String, Object> map = (Map<String, Object>) getUpdateTo().get("$inc");
    if (map == null) {
      map = Maps.newLinkedHashMap();
      getUpdateTo().put("$inc", map);
    }
    if (currentArrayIndex == -1) {
      map.put(currentfieldMeta.getFieldName(), amount);
    } else {
      String fieldName = currentfieldMeta.getFieldName() + "." + currentArrayIndex;
      if (currentArrayIndex2 >= 0) {
        fieldName += "." + currentArrayIndex2;
      }
      map.put(fieldName, amount);
    }

    currentArrayIndex = -1;
    currentArrayIndex2 = -1;
    return this;
  }

  // TODO $addToSet and $push not work with $pull at the same time

  @SuppressWarnings("unchecked")
  public Modifier addToList(Object what) {
    Preconditions.checkState(currentArrayIndex == -1);
    assertValidityForArrayUpdate(currentfieldMeta, what);

    Map<String, Object> map = (Map<String, Object>) getUpdateTo().get("$push");
    if (map == null) {
      map = Maps.newLinkedHashMap();
      getUpdateTo().put("$push", map);
    }
    Map<String, Object> fieldMap = (Map<String, Object>) map.get(currentfieldMeta.getFieldName());
    if (fieldMap == null) {
      fieldMap = Maps.newLinkedHashMap();
      map.put(currentfieldMeta.getFieldName(), fieldMap);
    }
    List<Object> objects = (List<Object>) fieldMap.get("$each");
    if (objects == null) {
      objects = Lists.newArrayList();
      fieldMap.put("$each", objects);
    }
    objects.add(ObjectToMap.transform(what));

    return this;
  }

  @SuppressWarnings("unchecked")
  public Modifier addToSet(Object what) {
    Preconditions.checkState(currentArrayIndex == -1);
    assertValidityForArrayUpdate(currentfieldMeta, what);

    Map<String, Object> map = (Map<String, Object>) getUpdateTo().get("$addToSet");
    if (map == null) {
      map = Maps.newLinkedHashMap();
      getUpdateTo().put("$addToSet", map);
    }
    Map<String, Object> fieldMap = (Map<String, Object>) map.get(currentfieldMeta.getFieldName());
    if (fieldMap == null) {
      fieldMap = Maps.newLinkedHashMap();
      map.put(currentfieldMeta.getFieldName(), fieldMap);
    }
    List<Object> objects = (List<Object>) fieldMap.get("$each");
    if (objects == null) {
      objects = Lists.newArrayList();
      fieldMap.put("$each", objects);
    }
    objects.add(ObjectToMap.transform(what));

    return this;
  }

  @SuppressWarnings("unchecked")
  public Modifier removeAllOccurrenceFromCollection(Object what) {
    Preconditions.checkState(currentArrayIndex == -1);
    assertValidityForArrayUpdate(currentfieldMeta, what);

    Map<String, Object> map = (Map<String, Object>) getUpdateTo().get("$pullAll");
    if (map == null) {
      map = Maps.newLinkedHashMap();
      getUpdateTo().put("$pullAll", map);
    }
    List<Object> objects = (List<Object>) map.get(currentfieldMeta.getFieldName());
    if (objects == null) {
      objects = Lists.newArrayList();
      map.put(currentfieldMeta.getFieldName(), objects);
    }
    objects.add(ObjectToMap.transform(what));

    return this;
  }

  // will only replace the first found element
  @SuppressWarnings("unchecked")
  public Modifier replaceListElement(Object what) {
    Preconditions.checkState(currentArrayIndex == -1);
    assertValidityForArrayUpdate(currentfieldMeta, what);

    Map<String, Object> map = (Map<String, Object>) getUpdateTo().get("$set");
    if (map == null) {
      map = Maps.newLinkedHashMap();
      getUpdateTo().put("$set", map);
    }
    map.put(currentfieldMeta.getFieldName() + ".$", ObjectToMap.transform(what));

    return this;
  }

  private BasicDBObject getUpdateTo() {
    if (updateTo == null) {
      updateTo = new BasicDBObject();
    }
    return updateTo;
  }

  DBObject toRawUpdate() {
    return updateTo;
  }

  private static void assertValidityForUpdate(FieldMeta fieldMeta, Object what) {
    Preconditions.checkNotNull(fieldMeta);
    int firstDotIndex = fieldMeta.getFieldName().indexOf(".");
    if (firstDotIndex >= 0) {
      Preconditions.checkState(firstDotIndex > 0);
      String fieldName = fieldMeta.getFieldName().substring(0, firstDotIndex);
      if (Query.isNumber(fieldName)) {
        // TODO continue to validate array
        return;
      }
      Class<?> fieldType = Query.assertFieldExists(FieldMeta.of(fieldName, fieldMeta.getOfClazz()));
      String remaining = fieldMeta.getFieldName().substring(firstDotIndex + 1);
      Preconditions.checkState(remaining.length() > 0);
      assertValidityForUpdate(FieldMeta.of(remaining, fieldType), what);
    } else {
      Class<?> fieldType = Query.assertFieldExists(fieldMeta, false);
      if (what != null) {
        Query.assertTypeSafe(fieldType, what);
      }
      Map<String, Boolean> fieldNotNull = getFieldNotNullCache(fieldMeta.getOfClazz());
      if (fieldNotNull.get(fieldMeta.getFieldName())) {
        ObjectToMap.assertValueNotNull(what, fieldMeta.getFieldName(), fieldType,
            fieldMeta.getOfClazz());
      }
    }
  }

  private static void assertValidityForArrayUpdate(FieldMeta fieldMeta, Object what) {
    Preconditions.checkNotNull(fieldMeta);
    // TODO
  }

  private static Map<String, Boolean> getFieldNotNullCache(Class<?> clazz) {
    Map<String, Boolean> fieldNotNull = fieldNotNullCache.get(clazz);
    if (fieldNotNull == null) {
      List<Field> fields = ObjectToMap.fieldsOf(clazz);
      fieldNotNull = Maps.newHashMapWithExpectedSize(fields.size());
      for (Field field : fields) {
        Annotation[] fieldAnnotations = field.getAnnotations();
        fieldNotNull.put(field.getName(), ObjectToMap.hasNotNullAnnotation(fieldAnnotations));
      }
      fieldNotNullCache.put(clazz, fieldNotNull);
    }
    return fieldNotNull;
  }

  @Override
  public String toString() {
    return getUpdateTo().toString();
  }
}
