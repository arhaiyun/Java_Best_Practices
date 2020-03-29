package com.exodus.arhaiyun.Birdies.database;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.osmapps.golf.common.util.FieldMeta;

public class Aggregator {

  public enum AccumulatorOperator {
    AVG("avg"),
    MAX("max"),
    MIN("min"),
    SUM("sum");

    // without $
    private String mongoName;

    AccumulatorOperator(final String mongoName) {
      this.mongoName = mongoName;
    }
  }

  private BasicDBObject group;

  private FieldMeta sumFieldMeta;

  private Aggregator() {
  }

  private static Aggregator newAggregator() {
    return new Aggregator();
  }

  /**
   * used for {@see ObjectDao.groupOnly}
   *
   * @param fieldMeta
   * @return
   */
  public static Aggregator of(FieldMeta fieldMeta) {
    return newAggregator().groupBy(fieldMeta);
  }

  /**
   * used for {@see ObjectDao.sumOnly}
   *
   * @param fieldMeta
   * @return
   */
  public static Aggregator ofSum(FieldMeta fieldMeta) {
    return newAggregator().sum(fieldMeta);
  }

  /**
   * used for {@see ObjectDao.accumulate}
   *
   * @param fieldMeta
   * @param operator
   * @return
   */
  public static Aggregator ofAccumulate(FieldMeta fieldMeta, AccumulatorOperator operator) {
    return newAggregator().accumulateBy(fieldMeta, operator);
  }

  /**
   * {$group:{_id:"$groupField", stat:{$accumulateOperator:"$statField"}, count:{$sum:1}}
   *
   * @param groupField
   * @param statField
   * @param operator
   * @return
   */
  public static Aggregator ofGroupAccumulateAndCount(final FieldMeta groupField,
      final FieldMeta statField, final AccumulatorOperator operator) {
    return newAggregator().groupAccumulateAndCountBy(groupField, statField, operator);
  }

  private Aggregator groupAccumulateAndCountBy(final FieldMeta groupField,
      final FieldMeta statField, final AccumulatorOperator operator) {
    Map<String, Object> map = getOrCreateGroupMap();
    map.put("_id", "$" + groupField.getFieldName());
    map.put("stats", accumulateMap(statField, operator));
    map.put("count", countMap());
    return this;
  }

  private Map<String, String> accumulateMap(FieldMeta fieldMeta, AccumulatorOperator operator) {
    Map<String, String> result = Maps.newHashMap();
    result.put("$" + operator.mongoName, "$" + fieldMeta.getFieldName());
    return result;
  }

  private Map<String, Integer> countMap() {
    Map<String, Integer> result = Maps.newHashMap();
    result.put("$sum", 1);
    return result;
  }

  /**
   * used for {@see Object.groupAndCount}
   *
   * @param groupFieldMeta
   * @return
   */
  public static Aggregator ofGroupAndCount(FieldMeta groupFieldMeta) {
    return newAggregator().groupBy(groupFieldMeta).count();
  }

  /**
   * used for {@see ObjectDao.groupAndAccumulate}
   *
   * @param groupFieldMeta
   * @param accumulateFieldMeta
   * @param operator
   * @return
   */
  public static Aggregator ofGroupAndAccumulate(FieldMeta groupFieldMeta,
      FieldMeta accumulateFieldMeta, AccumulatorOperator operator) {
    return newAggregator().groupBy(groupFieldMeta).accumulateBy(accumulateFieldMeta, operator);
  }

  private Map<String, Object> getOrCreateGroupMap() {
    Map<String, Object> map;
    if (group == null) {
      group = new BasicDBObject();
      map = Maps.newLinkedHashMap();
      group.append("$group", map);
      map.put("_id", null);
    } else {
      Preconditions.checkState(group.containsKey("$group"));
      map = (Map<String, Object>) group.get("$group");
    }
    return map;
  }

  private Aggregator accumulateBy(final FieldMeta fieldMeta, final AccumulatorOperator operator) {
    Map<String, Object> map = getOrCreateGroupMap();

    Map<String, String> operationMap = Maps.newLinkedHashMap();
    String operationName = "$" + operator.mongoName;
    String fieldName = "$" + fieldMeta.getFieldName();
    operationMap.put(operationName, fieldName);
    map.put("stats", operationMap);
    return this;
  }

  /**
   * wrap for Query:
   * db.sales.aggregate(
   * [{
   * $group : {
   * _id : "$fieldName" or null,
   * count: { $sum: 1 }}
   * }]
   * )
   *
   * @return
   */
  private Aggregator count() {
    Preconditions.checkNotNull(group);
    Map<String, Object> map = getOrCreateGroupMap();
    Map<String, Integer> operationMap = Maps.newLinkedHashMap();
    operationMap.put("$sum", 1);
    map.put("stats", operationMap);
    return this;
  }

  @SuppressWarnings("unchecked")
  private Aggregator groupBy(FieldMeta fieldMeta) {
    // TODO multiple fields aggregator not implemented yet
    Preconditions.checkState(group == null);
    if (group == null) {
      group = new BasicDBObject();
    }
    Map<String, Object> map = (Map<String, Object>) group.get("$group");
    if (map == null) {
      map = Maps.newLinkedHashMap();
      group.append("$group", map);
    }
    map.put("_id", "$" + fieldMeta.getFieldName());
    return this;
  }

  public Aggregator sum(FieldMeta fieldMeta) {
    // TODO multiple fields aggregator not implemented yet
    Preconditions.checkState(group == null);
    if (group == null) {
      group = new BasicDBObject();
    }
    Map<String, Object> map = (Map<String, Object>) group.get("$group");
    if (map == null) {
      map = Maps.newLinkedHashMap();
      group.append("$group", map);
    }
    map.put("_id", "_sum");
    // TODO only support sum one field now
    Map<String, String> sumMap = Maps.newLinkedHashMap();
    sumMap.put("$sum", "$" + fieldMeta.getFieldName());
    map.put(fieldMeta.getFieldName(), sumMap);
    sumFieldMeta = fieldMeta;
    return this;
  }

  public DBObject toGroup() {
    Preconditions.checkNotNull(group);
    return group;
  }

  public FieldMeta getSumFieldMeta() {
    return sumFieldMeta;
  }
}
