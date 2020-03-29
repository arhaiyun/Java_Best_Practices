package com.exodus.arhaiyun.Birdies.database;

import com.osmapps.golf.action.AbsRequestContext;
import com.osmapps.golf.action.RequestContextProvider;

public class MongoProfiler {

  public static void increaseMongoCallCount() {
    AbsRequestContext absRequestContext = RequestContextProvider.getAbsRequestContext();
    if (absRequestContext != null) {
      absRequestContext.increaseMongoCallCount(1);
    }
  }
}
