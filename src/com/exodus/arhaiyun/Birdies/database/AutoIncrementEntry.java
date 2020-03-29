package com.exodus.arhaiyun.Birdies.database;

import com.osmapps.golf.common.apiservice.Entity;
import com.osmapps.golf.common.apiservice.Primary;

@Entity(database = "misc")
public class AutoIncrementEntry {

  @Primary
  private String key;
  private long value;

  public AutoIncrementEntry(String key, long value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public long getValue() {
    return value;
  }
}
