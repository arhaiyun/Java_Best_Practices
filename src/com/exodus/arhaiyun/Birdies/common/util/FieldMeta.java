package com.exodus.arhaiyun.Birdies.common.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public final class FieldMeta {

  private final String fieldName;
  private final Class<?> ofClazz;

  private FieldMeta(String fieldName) {
    this.fieldName = fieldName;
    ofClazz = null;
  }

  private FieldMeta(String fieldName, Class<?> ofClazz) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(fieldName));
    Preconditions.checkArgument(ofClazz != null && !ofClazz.equals(Object.class));
    this.fieldName = fieldName;
    this.ofClazz = ofClazz;
  }

  public static FieldMeta of(String fieldName, Class<?> ofClazz) {
    return new FieldMeta(fieldName, ofClazz);
  }

  public String getFieldName() {
    return fieldName;
  }

  public Class<?> getOfClazz() {
    return ofClazz;
  }
}
