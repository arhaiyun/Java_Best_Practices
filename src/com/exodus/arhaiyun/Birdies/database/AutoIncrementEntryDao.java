package com.exodus.arhaiyun.Birdies.database;

import org.springframework.stereotype.Repository;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.osmapps.golf.common.util.FieldMeta;


@Repository
public class AutoIncrementEntryDao extends ObjectDao<AutoIncrementEntry> {

  public static final FieldMeta KEY = FieldMeta.of("key", AutoIncrementEntry.class);
  public static final FieldMeta VALUE = FieldMeta.of("value", AutoIncrementEntry.class);

  AutoIncrementEntryDao() {
    super(AutoIncrementEntry.class);
  }

  long getAndIncrease(String key) {
    Preconditions.checkNotNull(key);
    Query query = Query.of(KEY).is(key);
    Modifier modifier = Modifier.of(VALUE).increaseBy(1L);
    AutoIncrementEntry autoIncrementEntry = findAndModify(query, modifier);
    Preconditions.checkNotNull(autoIncrementEntry);
    return autoIncrementEntry.getValue();
  }

  // not thread safe, only can be called in spring initializing phase
  void registerKey(String key, long minValue) {
    Preconditions.checkNotNull(key);
    if (!serverMetaDao.isTestCase() && !serverMetaDao.isMain()
        && !serverMetaDao.isMainWebEnteprirse()) {
      return;
    }
    Query query = Query.of(KEY).is(key);
    AutoIncrementEntry autoIncrementEntry = findOne(query);
    if (autoIncrementEntry == null || autoIncrementEntry.getValue() < minValue) {
      Modifier modifier = Modifier.of(VALUE).setTo(minValue);
      upsertOne(query, modifier);
    }
  }

  @VisibleForTesting
  AutoIncrementEntry getByKey(String key) {
    Preconditions.checkNotNull(key);
    Query query = Query.of(KEY).is(key);
    return findOne(query);
  }
}
