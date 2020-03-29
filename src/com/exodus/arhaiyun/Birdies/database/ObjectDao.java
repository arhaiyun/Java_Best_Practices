package com.exodus.arhaiyun.Birdies.database;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.osmapps.golf.action.AbsRequestContext;
import com.osmapps.golf.action.RequestContextProvider;
import com.osmapps.golf.common.apiservice.Entity;
import com.osmapps.golf.common.apiservice.Index;
import com.osmapps.golf.common.apiservice.Index10;
import com.osmapps.golf.common.apiservice.Index11;
import com.osmapps.golf.common.apiservice.Index12;
import com.osmapps.golf.common.apiservice.Index2;
import com.osmapps.golf.common.apiservice.Index3;
import com.osmapps.golf.common.apiservice.Index4;
import com.osmapps.golf.common.apiservice.Index5;
import com.osmapps.golf.common.apiservice.Index6;
import com.osmapps.golf.common.apiservice.Index7;
import com.osmapps.golf.common.apiservice.Index8;
import com.osmapps.golf.common.apiservice.Index9;
import com.osmapps.golf.common.apiservice.IndexEmbeddedListFieldIndexes;
import com.osmapps.golf.common.apiservice.Primary;
import com.osmapps.golf.common.apiservice.SkipEmbeddedIndexes;
import com.osmapps.golf.common.bean.domain.BaseId;
import com.osmapps.golf.common.bean.domain.geo.GeoPoint;
import com.osmapps.golf.common.util.CollectionUtil;
import com.osmapps.golf.common.util.FieldMeta;
import com.osmapps.golf.common.util.ObjectUtil;
import com.osmapps.golf.common.util.Pair;
import com.osmapps.golf.common.util.StringUtil;
import com.osmapps.golf.common.util.UtcClock;
import com.osmapps.golf.model.VisibleForDataFix;
import com.osmapps.golf.model.util.ServerMetaDao;

public abstract class ObjectDao<T> extends MongoDao {

  static final class Key implements Comparable<Key> {

    private String name;
    private int order;
    private boolean ascending;
    private String type;

    public Key(String name, int order, boolean ascending, String type) {
      this.name = name;
      this.order = order;
      this.ascending = ascending;
      if (!Strings.isNullOrEmpty(type)) {
        // just ignore ascending if type has value
        this.ascending = true;
        this.type = type;
      }
    }

    @Override
    public String toString() {
      return name + ".order(" + order + ").ascending(" + ascending + ").type(" + type + ")";
    }

    @Override
    public int compareTo(Key key) {
      Preconditions.checkArgument(order != key.order,
          "Compound index [" + name + ", " + key.name + "] cannot have same order!");
      return order - key.order;
    }

    @Override
    public int hashCode() {
      return name.hashCode() + Boolean.valueOf(ascending).hashCode()
          + (Strings.isNullOrEmpty(type) ? 0 : type.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Key) {
        Key key = (Key) obj;
        return name.equals(key.name) && ascending == key.ascending
            && StringUtil.isEqual(type, key.type);
      }
      return false;
    }
  }

  static final class MongoIndex {

    private List<Key> keys = Lists.newArrayList();
    private boolean unique;
    private boolean sparse;

    public MongoIndex(List<Key> keys, boolean unique, boolean sparse) {
      this.keys.addAll(keys);
      Collections.sort(this.keys);
      this.unique = unique;
      this.sparse = sparse;

      // if has 2dsphere index, force sparse to false
      for (Key key : keys) {
        if (key.type != null && key.type.equals("2dsphere")) {
          this.sparse = false;
          break;
        }
      }
    }

    @Override
    public String toString() {
      return "MongoIndex: " + keys.toString() + ".unique(" + unique + ").sparse(" + sparse + ")";
    }

    public boolean isDuplicatedWith(MongoIndex mongoIndex) {
      return keys.equals(mongoIndex.keys);
    }

    @Override
    public int hashCode() {
      return keys.hashCode() + Boolean.valueOf(unique).hashCode()
          + Boolean.valueOf(sparse).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof MongoIndex) {
        MongoIndex mongoIndex = (MongoIndex) obj;
        return keys.equals(mongoIndex.keys) && unique == mongoIndex.unique
            && sparse == mongoIndex.sparse;
      }
      return false;
    }

    public DBObject toKeys() {
      DBObject keyObject = new BasicDBObject();
      for (Key key : keys) {
        if (Strings.isNullOrEmpty(key.type)) {
          keyObject.put(key.name, key.ascending ? 1 : -1);
        } else {
          keyObject.put(key.name, key.type);
        }
      }
      return keyObject;
    }

    public DBObject toOptions() {
      DBObject options = new BasicDBObject();
      if (unique) {
        options.put("unique", true);
      }
      if (sparse) {
        options.put("sparse", true);
      }
      return options;
    }
  }

  private static class PrimaryKey {

    private List<Key> keys = Lists.newArrayList();

    @Override
    public String toString() {
      return "PrimaryKey: " + keys.toString();
    }

    public MongoIndex toMongoIndex() {
      MongoIndex mongoIndex = new MongoIndex(keys, true, false);
      return mongoIndex;
    }
  }

  private static class IndexKey {

    private String group;
    private List<Key> keys = Lists.newArrayList();
    private boolean unique;
    private boolean sparse;

    public IndexKey(String group, boolean unique, boolean sparse) {
      this.group = group;
      this.unique = unique;
      this.sparse = sparse;
    }

    @Override
    public String toString() {
      return "IndexKey: group(" + group + ")." + keys.toString() + ".unique(" + unique + ").sparse("
          + sparse + ")";
    }

    public MongoIndex toMongoIndex() {
      MongoIndex mongoIndex = new MongoIndex(keys, unique, sparse);
      return mongoIndex;
    }
  }

  protected Class<? extends T> clazz;

  protected ObjectDao(Class<? extends T> clazz) {
    this(getReplicaSetNameFromEntityClass(clazz), getDatabaseNameFromEntityClass(clazz),
        getCollectionNameFromEntityClass(clazz), clazz);
  }

  /**
   * @param clazz
   * @param collectionName save the entity into a different collection (other than the class name)
   */
  protected ObjectDao(Class<? extends T> clazz, String collectionName) {
    this(getReplicaSetNameFromEntityClass(clazz), getDatabaseNameFromEntityClass(clazz),
        collectionName, clazz);
  }

  private static String getReplicaSetNameFromEntityClass(Class<?> clazz) {
    Entity entity = clazz.getAnnotation(Entity.class);
    Preconditions.checkNotNull(entity);
    String replicaSetName = entity.replicaSet();
    Preconditions.checkState(!Strings.isNullOrEmpty(replicaSetName));
    return replicaSetName;
  }

  private static String getDatabaseNameFromEntityClass(Class<?> clazz) {
    Entity entity = clazz.getAnnotation(Entity.class);
    Preconditions.checkNotNull(entity);
    String databaseName = entity.database();
    Preconditions.checkState(!Strings.isNullOrEmpty(databaseName));
    return databaseName;
  }

  private static String getCollectionNameFromEntityClass(Class<?> clazz) {
    Entity entity = clazz.getAnnotation(Entity.class);
    Preconditions.checkNotNull(entity);
    String collectionName = entity.collection();
    if (Strings.isNullOrEmpty(collectionName)) {
      collectionName = clazz.getSimpleName();
    }
    return collectionName;
  }

  private ObjectDao(String replicaSetName, String databaseName, String collectionName,
      Class<? extends T> clazz) {
    super(replicaSetName, databaseName, collectionName);
    Preconditions.checkNotNull(clazz);
    this.clazz = clazz;
  }

  @Autowired
  protected ServerMetaDao serverMetaDao;
  @Autowired
  private RequestContextProvider requestContextProvider;

  @Override
  @PostConstruct
  void init() {
    super.init();
    boolean needEnsure = false;
    if (isView()) {
      // for view, no need to ensure
      return;
    }
    if (serverMetaDao.isMain()) {
      needEnsure = true;
    } else {
      Set<String> packageNames = serverMetaDao.getPackageNamesForEnsureIndexes();
      if (!CollectionUtil.isNullOrEmpty(packageNames)) {
        final String currentPackageName = clazz.getPackage().getName();
        for (String name : packageNames) {
          if (currentPackageName.startsWith(name)) {
            needEnsure = true;
            break;
          }
        }
      }
    }

    if (needEnsure) {
      // System.out.println("ensureIndexes: " + clazz.getName());
      ensureIndexes();
      ensureData();
    }
  }

  public void ensureData() {
  }

  void ensureIndexes() {
    // System.out.println(this);

    Set<MongoIndex> currentIndexes = getCurrentIndexes();
    Set<MongoIndex> existedIndexes = getExistedIndexes();
    // System.out.println("currentIndexes=" + currentIndexes);
    // System.out.println("existedIndexes=" + existedIndexes);

    Set<MongoIndex> newIndexes = Sets.newHashSet(currentIndexes);
    newIndexes.removeAll(existedIndexes);
    Set<MongoIndex> deprecatedIndexes = Sets.newHashSet(existedIndexes);
    deprecatedIndexes.removeAll(currentIndexes);
    if (!newIndexes.isEmpty() || !deprecatedIndexes.isEmpty()) {
      System.out.println(this);
    }
    if (!newIndexes.isEmpty()) {
      System.out.println("newIndexes=" + newIndexes);
    }
    if (!deprecatedIndexes.isEmpty()) {
      System.out.println("deprecatedIndexes=" + deprecatedIndexes);
    }

    dropIndexes(deprecatedIndexes);
    createIndexes(newIndexes);
  }

  private static class IndexInfo {

    PrimaryKey primaryKey = null;
    List<IndexKey> indexKeys = null;
  }

  Set<MongoIndex> getCurrentIndexes() {
    final IndexInfo indexInfo = new IndexInfo();
    getIndexes(clazz, "", indexInfo);

    // System.out.println(indexInfo.primaryKey);
    // System.out.println(indexInfo.indexKeys);

    Set<MongoIndex> mongoIndexes = Sets.newHashSet();
    if (!CollectionUtil.isNullOrEmpty(indexInfo.indexKeys)) {
      for (IndexKey indexKey : indexInfo.indexKeys) {
        MongoIndex mongoIndex = indexKey.toMongoIndex();
        // check duplicate
        for (MongoIndex hadMongoIndex : mongoIndexes) {
          Preconditions.checkState(!mongoIndex.isDuplicatedWith(hadMongoIndex));
        }
        mongoIndexes.add(mongoIndex);
      }
    }
    if (indexInfo.primaryKey != null) {
      MongoIndex primaryIndex = indexInfo.primaryKey.toMongoIndex();
      // check duplicate
      for (MongoIndex hadMongoIndex : mongoIndexes) {
        Preconditions.checkState(!primaryIndex.isDuplicatedWith(hadMongoIndex));
      }
      mongoIndexes.add(primaryIndex);
    }
    return mongoIndexes;
  }

  void getIndexes(Class<?> clazz, String prefix, IndexInfo indexInfo) {
    List<Field> fields = ObjectToMap.fieldsOf(clazz);
    for (Field field : fields) {
      final boolean twoDimensionSphere = field.getType().equals(GeoPoint.class);
      final String keyName = prefix + field.getName();
      final Primary primary = field.getAnnotation(Primary.class);
      if (primary != null) {
        if (indexInfo.primaryKey == null) {
          indexInfo.primaryKey = new PrimaryKey();
        }
        indexInfo.primaryKey.keys.add(new Key(keyName, primary.order(), primary.ascending(),
            twoDimensionSphere ? "2dsphere" : null));
      }

      processIndexInfo(field.getAnnotation(Index.class), indexInfo, keyName, twoDimensionSphere);
      processIndexInfo(field.getAnnotation(Index2.class), indexInfo, keyName, twoDimensionSphere);
      processIndexInfo(field.getAnnotation(Index3.class), indexInfo, keyName, twoDimensionSphere);
      processIndexInfo(field.getAnnotation(Index4.class), indexInfo, keyName, twoDimensionSphere);
      processIndexInfo(field.getAnnotation(Index5.class), indexInfo, keyName, twoDimensionSphere);
      processIndexInfo(field.getAnnotation(Index6.class), indexInfo, keyName, twoDimensionSphere);
      processIndexInfo(field.getAnnotation(Index7.class), indexInfo, keyName, twoDimensionSphere);
      processIndexInfo(field.getAnnotation(Index8.class), indexInfo, keyName, twoDimensionSphere);
      processIndexInfo(field.getAnnotation(Index9.class), indexInfo, keyName, twoDimensionSphere);
      processIndexInfo(field.getAnnotation(Index10.class), indexInfo, keyName, twoDimensionSphere);
      processIndexInfo(field.getAnnotation(Index11.class), indexInfo, keyName, twoDimensionSphere);
      processIndexInfo(field.getAnnotation(Index12.class), indexInfo, keyName, twoDimensionSphere);

      if (field.getAnnotation(SkipEmbeddedIndexes.class) != null) {
        // we should skip the embedded index
        continue;
      }
      if (field.getAnnotation(IndexEmbeddedListFieldIndexes.class) != null) {
        Class<?> fieldClz = Query.getActualTypeArgumentForListField(field);
        getIndexes(fieldClz, field.getName() + ".", indexInfo);
      } else if (ObjectToMap.isComplexType(field.getType())) {
        // if this field is a complex type and does not mean to skip, recursively check indexes in
        // that class
        getIndexes(field.getType(), field.getName() + ".", indexInfo);
      }

    }
  }

  <T extends Annotation> void processIndexInfo(T index, IndexInfo indexInfo, String keyName,
      boolean twoDimensionSphere) {
    if (index != null) {
      if (indexInfo.indexKeys == null) {
        indexInfo.indexKeys = Lists.newArrayList();
      }
      String group = null;
      int order = 0;
      boolean unique = false;
      boolean sparse = true;
      boolean ascending = true;
      String subfield = null;
      try {
        group = (String) index.getClass().getDeclaredMethod("group").invoke(index);
        order = (int) index.getClass().getDeclaredMethod("order").invoke(index);
        unique = (boolean) index.getClass().getDeclaredMethod("unique").invoke(index);
        sparse = (boolean) index.getClass().getDeclaredMethod("sparse").invoke(index);
        ascending = (boolean) index.getClass().getDeclaredMethod("ascending").invoke(index);
        subfield = (String) index.getClass().getDeclaredMethod("subfield").invoke(index);
      } catch (Exception e) {
        throw new IllegalArgumentException("invalid index: " + index.getClass().getSimpleName(), e);
      }
      // TODO check validation of subfield
      if (!Strings.isNullOrEmpty(subfield)) {
        keyName = keyName + "." + subfield;
      }

      boolean added = false;
      if (!Strings.isNullOrEmpty(group)) {
        for (IndexKey indexKey : indexInfo.indexKeys) {
          if (group.equals(indexKey.group)) {
            Preconditions.checkState(unique == indexKey.unique && sparse == indexKey.sparse);
            indexKey.keys
                .add(new Key(keyName, order, ascending, twoDimensionSphere ? "2dsphere" : null));
            added = true;
            break;
          }
        }
      }
      if (!added) {
        IndexKey indexKey = new IndexKey(group, unique, sparse);
        indexKey.keys
            .add(new Key(keyName, order, ascending, twoDimensionSphere ? "2dsphere" : null));
        indexInfo.indexKeys.add(indexKey);
      }
    }
  }

  @SuppressWarnings("unchecked")
  Set<MongoIndex> getExistedIndexes() {
    Set<MongoIndex> mongoIndexes = Sets.newHashSet();
    List<DBObject> indexes = getIndexes();
    // System.out.println("indexes: " + indexes);
    if (!CollectionUtil.isNullOrEmpty(indexes)) {
      for (DBObject index : indexes) {
        Map<String, Object> keyInfos = (Map<String, Object>) index.get("key");
        if (keyInfos.size() == 1 && keyInfos.containsKey("_id")) {
          continue;
        }
        List<Key> keys = Lists.newArrayListWithCapacity(keyInfos.size());
        int order = 0;
        boolean formatUnknown = false;
        for (String keyName : keyInfos.keySet()) {
          Key key = null;
          Object value = keyInfos.get(keyName);
          if (value instanceof Number) {
            int intValue = ((Number) value).intValue();
            Preconditions.checkState(intValue == 1 || intValue == -1);
            key = new Key(keyName, order++, intValue == 1, null);
          } else if (value instanceof String) {
            key = new Key(keyName, order++, true, (String) value);
          }
          // if key is null, then ignore it because i don't known the type
          if (key == null) {
            System.out.println("unsupport index: " + keyName);
            formatUnknown = true;
            break;
          }
          keys.add(key);
        }
        if (!formatUnknown) {
          Boolean unique = (Boolean) index.get("unique");
          Boolean sparse = (Boolean) index.get("sparse");
          MongoIndex mongoIndex = new MongoIndex(keys, unique == null ? false : unique,
              sparse == null ? false : sparse);
          mongoIndexes.add(mongoIndex);
        }
      }
    }
    return mongoIndexes;
  }

  private void dropIndexes(Set<MongoIndex> mongoIndexes) {
    for (MongoIndex mongoIndex : mongoIndexes) {
      dropIndex(mongoIndex.toKeys());
    }
  }

  private void createIndexes(Set<MongoIndex> mongoIndexes) {
    for (MongoIndex mongoIndex : mongoIndexes) {
      System.out.println("Creating index@Thread: " + Thread.currentThread() + "@"
          + System.identityHashCode(Thread.currentThread()) + " " + mongoIndex.toString() + " ...");
      long timestamp = UtcClock.currentTimeMillis();
      createIndex(mongoIndex.toKeys(), mongoIndex.toOptions());
      System.out.println("Finished, using " + (UtcClock.currentTimeMillis() - timestamp) + "ms.");
    }
  }

  protected void insert(T object) {
    Preconditions.checkNotNull(object);
    Map<String, Object> map = ObjectToMap.toMap(object);
    BasicDBObject dbObject = new BasicDBObject(map);
    rawInsert(dbObject);
  }

  protected void insert(Collection<? extends T> objects) {
    Preconditions.checkArgument(!CollectionUtil.isNullOrEmpty(objects));
    List<DBObject> dbObjects = Lists.newArrayListWithCapacity(objects.size());
    for (Object object : objects) {
      Preconditions.checkNotNull(object);
      Map<String, Object> map = ObjectToMap.toMap(object);
      BasicDBObject dbObject = new BasicDBObject(map);
      dbObjects.add(dbObject);
    }
    rawInsert(dbObjects);
  }

  protected int removeOne(Query query) {
    Preconditions.checkNotNull(query);
    DBObject dbObject = query.toRawQuery();
    Preconditions.checkNotNull(dbObject);
    return rawRemoveOne(dbObject);
  }

  protected int removeMultiple(Query query) {
    Preconditions.checkNotNull(query);
    DBObject dbObject = query.toRawQuery();
    Preconditions.checkNotNull(dbObject);
    return rawRemove(dbObject);
  }

  @VisibleForDataFix
  protected int removeAll() {
    // only for data fix
    Preconditions.checkState(requestContextProvider.get() == null);
    return rawRemove(null);
  }

  // Caution: very dangerous, call it when you are intended
  protected int forceRemoveAll() {
    return rawRemove(null);
  }

  // not recommended, please use update with modifier instead
  protected int replaceOne(Query query, T target) {
    Preconditions.checkNotNull(query);
    Preconditions.checkNotNull(target);
    DBObject rawQuery = query.toRawQuery();
    Preconditions.checkNotNull(rawQuery);
    Map<String, Object> map = ObjectToMap.toMap(target);
    BasicDBObject rawTarget = new BasicDBObject(map);
    return rawUpdateOne(rawQuery, rawTarget, true);
  }

  protected int updateOne(Query query, Modifier modifier) {
    Preconditions.checkNotNull(query);
    Preconditions.checkNotNull(modifier);
    DBObject rawQuery = query.toRawQuery();
    Preconditions.checkNotNull(rawQuery);
    DBObject rawUpdate = modifier.toRawUpdate();
    Preconditions.checkNotNull(rawUpdate);
    return rawUpdateOne(rawQuery, rawUpdate, false);
  }

  protected int updateMultiple(Query query, Modifier modifier) {
    Preconditions.checkNotNull(modifier);
    DBObject rawUpdate = modifier.toRawUpdate();
    Preconditions.checkNotNull(rawUpdate);
    return rawUpdateMultiple(query == null ? null : query.toRawQuery(), rawUpdate, false);
  }

  protected int upsertOne(Query query, Modifier modifier) {
    Preconditions.checkNotNull(query);
    Preconditions.checkNotNull(modifier);
    DBObject rawQuery = query.toRawQuery();
    Preconditions.checkNotNull(rawQuery);
    DBObject rawUpdate = modifier.toRawUpdate();
    Preconditions.checkNotNull(rawUpdate);
    return rawUpsertOne(rawQuery, rawUpdate, false);
  }

  protected int upsertOneWithEmptyQuery(Modifier modifier) {
    Preconditions.checkNotNull(modifier);
    DBObject rawUpdate = modifier.toRawUpdate();
    Preconditions.checkNotNull(rawUpdate);
    return rawUpsertOne(null, rawUpdate, false);
  }

  protected int upsertOne(Query query, T target) {
    Preconditions.checkNotNull(query);
    Preconditions.checkNotNull(target);
    DBObject rawQuery = query.toRawQuery();
    Preconditions.checkNotNull(rawQuery);
    Map<String, Object> map = ObjectToMap.toMap(target);
    BasicDBObject rawTarget = new BasicDBObject(map);
    return rawUpsertOne(rawQuery, rawTarget, true);
  }

  protected List<T> findAll() {
    return find(null);
  }

  protected List<T> find(Query query) {
    List<DBObject> dbObjects = null;
    if (query == null) {
      dbObjects = rawFind(null, null, null, null, 0, 0, false);
    } else {
      dbObjects = rawFind(query.toRawQuery(), query.getProjection(), query.getIndexHint(),
          query.getSort(), query.getLimit(), query.getSkip(), query.isForcePrimary());
    }
    if (CollectionUtil.isNullOrEmpty(dbObjects)) {
      return null;
    } else {
      List<T> ts = Lists.newArrayListWithCapacity(dbObjects.size());
      Set<ObjectId> duplicateCheckSet = null;
      for (DBObject dbObject : dbObjects) {
        ObjectId objectId = (ObjectId) dbObject.get("_id");
        // objectId could be null if _id is excluded in the query projection
        if (objectId != null) {
          if (duplicateCheckSet == null) {
            duplicateCheckSet = Sets.newHashSetWithExpectedSize(dbObjects.size());
          }
          if (duplicateCheckSet.contains(objectId)) {
            continue;
          }
          duplicateCheckSet.add(objectId);
        }
        T t = doDeserialize(dbObject);
        ts.add(t);
      }
      return ts;
    }
  }

  protected ObjectIterator iterateAll() {
    return iterate(null);
  }

  protected ObjectIterator iterate(Query query) {
    DBCursor dbCursor = null;
    if (query == null) {
      dbCursor = rawIterate(null, null, null, null, 0, 0, false);
    } else {
      dbCursor = rawIterate(query.toRawQuery(), query.getProjection(), query.getIndexHint(),
          query.getSort(), query.getLimit(), query.getSkip(), query.isForcePrimary());
    }
    ObjectIterator objectIterator = new ObjectIterator(dbCursor);
    return objectIterator;
  }

  protected T findOne() {
    return findOne(null);
  }

  protected T findOne(Query query) {
    DBObject dbObject = null;
    if (query == null) {
      dbObject = rawFindOne(null, null, null, false);
    } else {
      dbObject = rawFindOne(query.toRawQuery(), query.getProjection(), query.getSort(),
          query.isForcePrimary());
    }
    if (dbObject == null) {
      return null;
    } else {
      return doDeserialize(dbObject);
    }
  }

  protected int count(Query query) {
    if (query == null) {
      return rawCount(null, false);
    } else {
      return rawCount(query.toRawQuery(), query.isForcePrimary());
    }
  }

  protected int countAll() {
    return rawCount(null, false);
  }

  public class ObjectIterator {

    private DBCursor dbCursor;

    private ObjectIterator(DBCursor dbCursor) {
      this.dbCursor = dbCursor;
    }

    public boolean hasNext() {
      return dbCursor.hasNext();
    }

    public T next() {
      DBObject dbObject = dbCursor.next();
      return doDeserialize(dbObject);
    }

    public void close() {
      dbCursor.close();
    }
  }

  private T doDeserialize(DBObject dbObject) {
    long currentNanos = System.nanoTime();
    T t = deserialize(dbObject);
    AbsRequestContext requestContext = requestContextProvider.get();
    if (requestContext != null) {
      requestContext.increaseMapToObjectNanos(System.nanoTime() - currentNanos);
    }
    return t;
  }

  protected T deserialize(DBObject dbObject) {
    String jsonString = dbObject.toString();
    T t = ObjectToMap.fromJsonString(jsonString, clazz);
    return t;
  }

  private static final Map<Class<?>, FieldMeta[]> clazzFieldMetasCache = Maps.newConcurrentMap();

  public static FieldMeta[] allFieldMetasOf(Class<?> clazz) {
    FieldMeta[] fieldMetas = clazzFieldMetasCache.get(clazz);
    if (fieldMetas == null) {
      List<Field> fields = ObjectToMap.fieldsOf(clazz);
      fieldMetas = new FieldMeta[fields.size()];
      int index = 0;
      for (Field field : fields) {
        FieldMeta fieldMeta = FieldMeta.of(field.getName(), clazz);
        fieldMetas[index++] = fieldMeta;
      }
      clazzFieldMetasCache.put(clazz, fieldMetas);
    }
    return fieldMetas;
  }

  protected T findAndModify(Query query, Modifier modifier) {
    Preconditions.checkNotNull(query);
    Preconditions.checkNotNull(modifier);
    DBObject dbObject = rawFindAndModify(query.toRawQuery(), modifier.toRawUpdate(), false);
    if (dbObject == null) {
      return null;
    } else {
      return doDeserialize(dbObject);
    }
  }

  protected T findAndRemove(Query query) {
    Preconditions.checkNotNull(query);
    DBObject dbObject = rawFindAndRemove(query.toRawQuery());
    if (dbObject == null) {
      return null;
    } else {
      return doDeserialize(dbObject);
    }
  }

  @SuppressWarnings("unchecked")
  protected <F> List<F> distinct(Query query, FieldMeta fieldMeta, Class<F> fieldClazz) {
    Preconditions.checkNotNull(fieldMeta);
    Preconditions.checkNotNull(fieldClazz);
    Preconditions.checkArgument(ObjectUtil.isBasicType(fieldClazz) || ObjectUtil.isId(fieldClazz),
        "complex type not supported yet");
    List<?> values = null;
    if (query == null) {
      values = rawDistinct(null, fieldMeta.getFieldName(), false);
    } else {
      values = rawDistinct(query.toRawQuery(), fieldMeta.getFieldName(), query.isForcePrimary());
    }
    if (CollectionUtil.isNullOrEmpty(values)) {
      return null;
    } else {
      List<F> objects = Lists.newArrayList();
      if (ObjectUtil.isBasicType(fieldClazz)) {
        for (Object object : values) {
          F simpleObject = (F) object;
          if (simpleObject != null) {
            Preconditions.checkState(fieldClazz.isInstance(simpleObject));
          }
          objects.add(simpleObject);
        }
      } else if (ObjectUtil.isId(fieldClazz)) {
        for (Object object : values) {
          if (object == null) {
            objects.add(null);
          } else {
            Preconditions.checkState(ObjectUtil.isString(object));
            F id = (F) MapToObject.transformId(object, (Class<? extends BaseId>) fieldClazz);
            objects.add(id);
          }
        }
      } else {
        // TODO
      }
      return objects;
    }
  }

  // TODO add other aggregate operations

  // TODO not support group and sum together
  @SuppressWarnings("unchecked")
  private <F> List<F> aggregate(Query query, Aggregator aggregator, Class<F> fieldClazz) {
    Preconditions.checkNotNull(aggregator);
    Preconditions.checkNotNull(fieldClazz);
    Preconditions.checkArgument(ObjectUtil.isBasicType(fieldClazz),
        "complex type not supported yet");
    List<DBObject> dbObjects = null;
    if (query == null) {
      dbObjects = rawAggregate(null, aggregator.toGroup(), false);
    } else {
      dbObjects = rawAggregate(query.toRawQuery(), aggregator.toGroup(), query.isForcePrimary());
    }
    if (CollectionUtil.isNullOrEmpty(dbObjects)) {
      return null;
    } else {
      List<F> objects = Lists.newArrayList();
      if (ObjectUtil.isBasicType(fieldClazz)) {
        for (DBObject dbObject : dbObjects) {
          String key = "_id";
          if (aggregator.getSumFieldMeta() != null) {
            key = aggregator.getSumFieldMeta().getFieldName();
          }
          F basicObject = (F) dbObject.get(key);
          if (basicObject != null) {
            Preconditions.checkState(fieldClazz.isInstance(basicObject));
          }
          objects.add(basicObject);
        }
      } else {
        // TODO
      }
      return objects;
    }
  }

  protected <T> T accumulateOnly(Query query, FieldMeta accumulateFieldMeta,
      Aggregator.AccumulatorOperator operator, Class<T> accumulatorFieldClazz) {
    Preconditions.checkNotNull(query);
    Preconditions.checkNotNull(accumulateFieldMeta);
    Preconditions.checkNotNull(operator);
    Preconditions.checkNotNull(accumulatorFieldClazz);
    Preconditions.checkArgument(ObjectUtil.isBasicType(accumulatorFieldClazz),
        "complex type not supported yet");

    Aggregator aggregator = Aggregator.ofAccumulate(accumulateFieldMeta, operator);
    List<DBObject> dbObjects = null;
    if (query == null) {
      dbObjects = rawAggregate(null, aggregator.toGroup(), false);
    } else {
      dbObjects = rawAggregate(query.toRawQuery(), aggregator.toGroup(), query.isForcePrimary());
    }
    if (CollectionUtil.isNullOrEmpty(dbObjects)) {
      return null;
    } else {
      Preconditions.checkState(dbObjects.size() == 1);
      DBObject dbObject = dbObjects.get(0);
      return (T) dbObject.get("stats");
    }
  }

  // group and count
  protected <K> Map<K, Integer> groupAndCount(Query query, FieldMeta fieldMeta,
      Class<K> fieldClazz) {
    Preconditions.checkNotNull(fieldMeta);
    Preconditions.checkNotNull(fieldClazz);
    Preconditions.checkArgument(ObjectUtil.isBasicType(fieldClazz));

    Aggregator aggregator = Aggregator.ofGroupAndCount(fieldMeta);
    List<DBObject> dbObjects = null;
    if (query == null) {
      dbObjects = rawAggregate(null, aggregator.toGroup(), false);
    } else {
      dbObjects = rawAggregate(query.toRawQuery(), aggregator.toGroup(), query.isForcePrimary());
    }
    if (CollectionUtil.isNullOrEmpty(dbObjects)) {
      return null;
    } else {
      Map<K, Integer> map = Maps.newHashMap();
      for (DBObject dbObject : dbObjects) {
        String key = "_id";
        K idObject = (K) dbObject.get("_id");
        Integer count = (Integer) dbObject.get("stats");
        map.put(idObject, count);
      }
      return map;
    }
  }

  /**
   * {$group:{_id:"$groupField", stat:{$accumulateOperator:"$statField"}, count:{$sum:1}}
   * sample:
   * {$group:{_id:"$category", stat:{$avg:"$value"}, count:{$sum:1}}
   *
   * @param query
   * @param <K>
   * @param <S>
   * @return
   */
  protected <K, S> Map<K, Pair<S, Integer>> groupAccumulateAndCount(Query query,
      FieldMeta groupField, FieldMeta statField, Class<K> groupFieldClazz, Class<S> statFieldClazz,
      Aggregator.AccumulatorOperator operator) {
    Preconditions.checkNotNull(groupField);
    Preconditions.checkNotNull(statField);
    Preconditions.checkNotNull(groupFieldClazz);
    Preconditions.checkNotNull(statFieldClazz);
    Preconditions.checkArgument(ObjectUtil.isBasicType(groupFieldClazz));
    Preconditions.checkArgument(ObjectUtil.isBasicType(statFieldClazz));

    Aggregator aggregator = Aggregator.ofGroupAccumulateAndCount(groupField, statField, operator);
    List<DBObject> dbObjects = null;
    if (query == null) {
      dbObjects = rawAggregate(null, aggregator.toGroup(), false);
    } else {
      dbObjects = rawAggregate(query.toRawQuery(), aggregator.toGroup(), query.isForcePrimary());
    }
    if (CollectionUtil.isNullOrEmpty(dbObjects)) {
      return null;
    } else {
      Map<K, Pair<S, Integer>> map = Maps.newHashMap();
      for (DBObject dbObject : dbObjects) {
        K idObject = (K) dbObject.get("_id");
        S statObject = (S) dbObject.get("stats");
        Integer count = (Integer) dbObject.get("count");
        map.put(idObject, Pair.of(statObject, count));
      }
      return map;
    }
  }

  // group and accumulate
  // support group and sum together
  // NOTICE: accumulatorFieldClazz should be Double for avg operation
  protected <K, F> Map<K, F> groupAndAccumulate(Query query, FieldMeta groupFieldMeta,
      FieldMeta accumulateFieldMeta, Aggregator.AccumulatorOperator operator, Class<K> idFieldClazz,
      Class<F> accumulatorFieldClazz) {
    Preconditions.checkNotNull(groupFieldMeta);
    Preconditions.checkNotNull(accumulateFieldMeta);
    Preconditions.checkNotNull(operator);
    Preconditions.checkNotNull(idFieldClazz);
    Preconditions.checkNotNull(accumulatorFieldClazz);
    Preconditions.checkArgument(ObjectUtil.isBasicType(idFieldClazz),
        "complex type not supported yet");
    Preconditions.checkArgument(ObjectUtil.isBasicType(accumulatorFieldClazz),
        "complex type not supported yet");
    Aggregator aggregator =
        Aggregator.ofGroupAndAccumulate(groupFieldMeta, accumulateFieldMeta, operator);
    List<DBObject> dbObjects = null;
    if (query == null) {
      dbObjects = rawAggregate(null, aggregator.toGroup(), false);
    } else {
      dbObjects = rawAggregate(query.toRawQuery(), aggregator.toGroup(), query.isForcePrimary());
    }
    if (CollectionUtil.isNullOrEmpty(dbObjects)) {
      return null;
    } else {
      Map<K, F> map = Maps.newHashMap();
      for (DBObject dbObject : dbObjects) {
        String key = "_id";
        K idObject = (K) dbObject.get("_id");
        F accumulatorObject = (F) dbObject.get("stats");
        map.put(idObject, accumulatorObject);
      }
      return map;
    }
  }

  protected <T> T accumulate(Query query, FieldMeta fieldMeta,
      Aggregator.AccumulatorOperator operator, Class<T> resultClazz) {
    return null;
  }

  protected <F> List<F> groupOnly(Query query, FieldMeta fieldMeta, Class<F> fieldClazz) {
    return aggregate(query, Aggregator.of(fieldMeta), fieldClazz);
  }

  protected <F> List<F> sumOnly(Query query, FieldMeta fieldMeta, Class<F> fieldClazz) {
    return aggregate(query, Aggregator.ofSum(fieldMeta), fieldClazz);
  }

  public static class ObjectsWithTotalPage<T> {

    public final List<T> objects;
    public final int count;
    public final int totalPage;

    public ObjectsWithTotalPage(List<T> objects, int count, int totalPage) {
      this.objects = objects;
      this.count = count;
      this.totalPage = totalPage;
    }

    public boolean isEmpty() {
      return count == 0;
    }

    public static <T> ObjectsWithTotalPage<T> empty() {
      return new ObjectsWithTotalPage<>(null, 0, 0);
    }

    /**
     * Be careful, only type of objects that does not explode is applicable for the method
     *
     * @param objects  all objects for the model in memory
     * @param page
     * @param pageSize
     * @param <T>
     * @return
     */
    public static <T> ObjectsWithTotalPage<T> constructPaginatedObjectsFromMemory(List<T> objects,
        int page, int pageSize) {
      Preconditions.checkArgument(page >= 0);
      Preconditions.checkArgument(pageSize > 0);
      if (objects == null) {
        return empty();
      }
      final int start = page * pageSize;
      int end = 0;
      List<T> objectsForCurrentPage = null;
      int totalPage = 0;
      int count = objects.size();
      if (count > 0) {
        totalPage = count / pageSize;
        if (count % pageSize > 0) {
          totalPage += 1;
        }
        Preconditions.checkArgument(count >= start);
        end = count > start + pageSize ? start + pageSize : count;
        objectsForCurrentPage = objects.subList(start, end);
      }
      return new ObjectsWithTotalPage<T>(objectsForCurrentPage, count, totalPage);
    }
  }

  protected ObjectsWithTotalPage<T> findObjectsWithTotalPage(Query query, int page, int pageSize) {
    Preconditions.checkArgument(page >= 0);
    Preconditions.checkArgument(pageSize > 0);
    final int start = page * pageSize;
    final int size = pageSize;
    List<T> objects = null;
    int totalPage = 0;
    int count = count(query);
    if (count > 0) {
      totalPage = count / pageSize;
      if (count % pageSize > 0) {
        totalPage += 1;
      }
      query.limit(size).skip(start);
      objects = find(query);
    }
    return new ObjectsWithTotalPage<T>(objects, count, totalPage);
  }

  protected boolean isView() {
    return false;
  }
}
