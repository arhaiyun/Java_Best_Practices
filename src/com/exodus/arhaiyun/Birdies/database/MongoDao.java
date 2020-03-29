package com.exodus.arhaiyun.Birdies.database;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.osmapps.golf.common.util.CollectionUtil;
import com.osmapps.golf.common.util.UtcClock;

public abstract class MongoDao {

  private String replicaSetName;
  private String databaseName;
  private String collectionName;

  private DB database;
  private DBCollection collection;

  @Autowired
  private MongoDataSource mongoDataSource;

  private MongoClient mongoClient;
  private MongoClient noTimeoutMongoClient;

  private static final String MODIFY_TIMESTAMP_FIELD_NAME = "_modifyTimestamp";

  MongoDao(String replicaSetName, String databaseName, String collectionName) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(replicaSetName));
    Preconditions.checkArgument(!Strings.isNullOrEmpty(databaseName));
    Preconditions.checkArgument(!Strings.isNullOrEmpty(collectionName));
    this.replicaSetName = replicaSetName;
    this.databaseName = databaseName;
    this.collectionName = collectionName;
  }

  // call this method before init()
  @VisibleForTesting
  protected void setMongoClients(MongoClient mongoClient, MongoClient noTimeoutMongoClient) {
    this.mongoClient = mongoClient;
    this.noTimeoutMongoClient = noTimeoutMongoClient;
  }

  void init() {
    // if test application already called setMongoClients manually, then do not get
    // mongoClients from mongoDataSource
    if (mongoClient == null) {
      mongoClient = mongoDataSource.getMongoClient(replicaSetName);
      Preconditions.checkNotNull(
          String.format("failed to locate mongoClient for replicaSet %s", replicaSetName),
          mongoClient);
    }
    if (noTimeoutMongoClient == null) {
      noTimeoutMongoClient = mongoDataSource.getMongoClient(replicaSetName, true);
      Preconditions.checkNotNull(noTimeoutMongoClient);
    }

    database = mongoClient.getDB(databaseName);
    collection = database.getCollection(collectionName);
  }

  List<DBObject> getIndexes() {
    return collection.getIndexInfo();
  }

  void dropIndex(DBObject keys) {
    collection.dropIndex(keys);
  }

  void createIndex(DBObject keys, DBObject options) {
    noTimeoutMongoClient.getDB(databaseName).getCollection(collectionName).createIndex(keys,
        options);
  }

  void rawInsert(DBObject dbObject) {
    MongoProfiler.increaseMongoCallCount();
    appendModifyTimestamp(dbObject, UtcClock.currentTimeMillis(), true);
    collection.insert(dbObject, WriteConcern.ACKNOWLEDGED);
  }

  // bad api supplied by mongo java driver, the generic type should be '? extends DBObject'
  void rawInsert(List<DBObject> dbObjects) {
    MongoProfiler.increaseMongoCallCount();
    long modifyTimestamp = UtcClock.currentTimeMillis();
    for (DBObject dbObject : dbObjects) {
      appendModifyTimestamp(dbObject, modifyTimestamp, true);
    }
    collection.insert(dbObjects, WriteConcern.ACKNOWLEDGED);
  }

  int rawRemoveOne(DBObject query) {
    MongoProfiler.increaseMongoCallCount();
    DBObject dbObject = collection.findAndModify(query, new BasicDBObject("_id", 1), null, true,
        null, false, false);
    return dbObject == null ? 0 : 1;
  }

  int rawRemove(DBObject query) {
    MongoProfiler.increaseMongoCallCount();
    if (query == null) {
      query = new BasicDBObject();
    }
    WriteResult writeResult = collection.remove(query, WriteConcern.ACKNOWLEDGED);
    return writeResult.getN();
  }

  int rawUpdateOne(DBObject query, DBObject updateTo, boolean isReplace) {
    MongoProfiler.increaseMongoCallCount();
    if (query == null) {
      query = new BasicDBObject();
    }
    appendModifyTimestamp(updateTo, UtcClock.currentTimeMillis(), isReplace);
    WriteResult writeResult =
        collection.update(query, updateTo, false, false, WriteConcern.ACKNOWLEDGED);
    return writeResult.getN();
  }

  int rawUpdateMultiple(DBObject query, DBObject updateTo, boolean isReplace) {
    MongoProfiler.increaseMongoCallCount();
    if (query == null) {
      query = new BasicDBObject();
    }
    appendModifyTimestamp(updateTo, UtcClock.currentTimeMillis(), isReplace);
    WriteResult writeResult =
        collection.update(query, updateTo, false, true, WriteConcern.ACKNOWLEDGED);
    return writeResult.getN();
  }

  int rawUpsertOne(DBObject query, DBObject updateTo, boolean isReplace) {
    MongoProfiler.increaseMongoCallCount();
    if (query == null) {
      query = new BasicDBObject();
    }
    appendModifyTimestamp(updateTo, UtcClock.currentTimeMillis(), isReplace);
    try {
      WriteResult writeResult =
          collection.update(query, updateTo, true, false, WriteConcern.ACKNOWLEDGED);
      return writeResult.getN();
    } catch (DuplicateKeyException ex) {
      // try once more, should not happen again
      WriteResult writeResult =
          collection.update(query, updateTo, true, false, WriteConcern.ACKNOWLEDGED);
      return writeResult.getN();
    }
  }

  List<DBObject> rawFind(DBObject query, DBObject projection, DBObject indexHint, DBObject sort,
      int limit, int skip, boolean forcePrimary) {
    MongoProfiler.increaseMongoCallCount();
    DBCursor cursor = rawIterate(query, projection, indexHint, sort, limit, skip, forcePrimary);
    try {
      List<DBObject> dbObjects = cursor.toArray();
      return dbObjects;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  @SuppressWarnings("resource")
  DBCursor rawIterate(DBObject query, DBObject projection, DBObject indexHint, DBObject sort,
      int limit, int skip, boolean forcePrimary) {
    // do not add MongoProfiler.increaseMongoCallCount
    // rawIterate is only used in fix data task, or it is embedded by rawFind
    DBCursor cursor = null;
    try {
      cursor = new DBCursor(collection, query, projection,
          forcePrimary ? ReadPreference.primary() : collection.getReadPreference());
      if (indexHint != null) {
        cursor = cursor.hint(indexHint);
      }
      if (sort != null) {
        cursor = cursor.sort(sort);
      }
      if (limit > 0) {
        cursor = cursor.limit(limit);
      }
      if (skip > 0) {
        cursor = cursor.skip(skip);
      }
      return cursor;
    } catch (RuntimeException e) {
      if (cursor != null) {
        cursor.close();
      }
      throw e;
    }
  }

  DBObject rawFindOne(DBObject query, DBObject projection, DBObject sort, boolean forcePrimary) {
    MongoProfiler.increaseMongoCallCount();
    if (query == null) {
      query = new BasicDBObject();
    }
    if (forcePrimary) {
      return collection.findOne(query, projection, sort, ReadPreference.primary());
    } else {
      return collection.findOne(query, projection, sort);
    }
  }

  int rawCount(DBObject query, boolean forcePrimary) {
    MongoProfiler.increaseMongoCallCount();
    DBCursor cursor = null;
    try {
      cursor = new DBCursor(collection, query, null,
          forcePrimary ? ReadPreference.primary() : collection.getReadPreference());
      return cursor.count();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  DBObject rawFindAndModify(DBObject query, DBObject updateTo, boolean isReplace) {
    MongoProfiler.increaseMongoCallCount();
    if (query == null) {
      query = new BasicDBObject();
    }
    appendModifyTimestamp(updateTo, UtcClock.currentTimeMillis(), isReplace);
    return collection.findAndModify(query, updateTo);
  }

  DBObject rawFindAndRemove(DBObject query) {
    MongoProfiler.increaseMongoCallCount();
    if (query == null) {
      query = new BasicDBObject();
    }
    return collection.findAndRemove(query);
  }

  List<?> rawDistinct(DBObject query, String fieldName, boolean forcePrimary) {
    Preconditions.checkNotNull(fieldName);
    MongoProfiler.increaseMongoCallCount();
    if (query == null) {
      query = new BasicDBObject();
    }
    if (forcePrimary) {
      return collection.distinct(fieldName, query, ReadPreference.primary());
    } else {
      return collection.distinct(fieldName, query);
    }
  }

  List<DBObject> rawAggregate(DBObject query, DBObject group, boolean forcePrimary) {
    Preconditions.checkNotNull(group);
    MongoProfiler.increaseMongoCallCount();
    List<DBObject> pipeline = Lists.newArrayList();
    if (query != null) {
      BasicDBObject dbObject = new BasicDBObject("$match", query);
      pipeline.add(dbObject);
    }
    pipeline.add(group);
    AggregationOutput aggregationOutput = forcePrimary
        ? collection.aggregate(pipeline, ReadPreference.primary()) : collection.aggregate(pipeline);
    Preconditions.checkNotNull(aggregationOutput);
    Iterable<DBObject> results = aggregationOutput.results();
    if (results == null) {
      return null;
    }
    List<DBObject> dbObjects = Lists.newArrayList();
    for (DBObject dbObject : results) {
      dbObjects.add(dbObject);
    }
    if (CollectionUtil.isNullOrEmpty(dbObjects)) {
      return null;
    }
    return dbObjects;
  }

  private void appendModifyTimestamp(DBObject dbObject, long modifyTimestamp,
      boolean isFullObject) {
    if (isFullObject) {
      dbObject.put(MODIFY_TIMESTAMP_FIELD_NAME, modifyTimestamp);
    } else {
      Map<String, Object> map = (Map<String, Object>) dbObject.get("$set");
      if (map == null) {
        map = Maps.newLinkedHashMap();
        dbObject.put("$set", map);
      }
      map.put(MODIFY_TIMESTAMP_FIELD_NAME, modifyTimestamp);
    }
  }

  // TODO remove it
  public DBCollection getCollection() {
    return collection;
  }
}
