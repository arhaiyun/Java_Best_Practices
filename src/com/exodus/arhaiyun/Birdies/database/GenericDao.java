package com.exodus.arhaiyun.Birdies.database;

import java.util.Collection;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.mongodb.MongoClient;

public class GenericDao<T> extends ObjectDao<T> {

  public GenericDao(Class<? extends T> clazz) {
    super(clazz);
  }

  @VisibleForTesting
  public GenericDao(Class<? extends T> clazz, MongoClient mongoClient,
      MongoClient noTimeOutMongoClient) {
    super(clazz);
    setMongoClients(mongoClient, noTimeOutMongoClient);
  }

  @VisibleForTesting
  @Override
  public void init() {
    super.init();
  }

  @Override
  public void insert(T object) {
    super.insert(object);
  }

  @Override
  public void insert(Collection<? extends T> objects) {
    super.insert(objects);
  }

  @Override
  public int replaceOne(Query query, T target) {
    return super.replaceOne(query, target);
  }

  @Override
  public T findOne(Query query) {
    return super.findOne(query);
  }

  @Override
  public List<T> find(Query query) {
    return super.find(query);
  }

  @Override
  public ObjectDao<T>.ObjectIterator iterateAll() {
    return super.iterateAll();
  }

  @Override
  public ObjectDao<T>.ObjectIterator iterate(Query query) {
    return super.iterate(query);
  }

  @Override
  public int countAll() {
    return super.countAll();
  }

  @Override
  public int count(Query query) {
    return super.count(query);
  }
}
