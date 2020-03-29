package com.exodus.arhaiyun.Birdies.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

@Service
public class AutoIncrementService {

  @Autowired
  private AutoIncrementEntryDao autoIncrementEntryDao;

  public long getAndIncrease(String key) {
    return autoIncrementEntryDao.getAndIncrease(key);
  }

  // not thread safe, only can be called in spring initializing phase
  public void registerKey(String key, long minValue) {
    Preconditions.checkNotNull(key);
    autoIncrementEntryDao.registerKey(key, minValue);
  }
}
