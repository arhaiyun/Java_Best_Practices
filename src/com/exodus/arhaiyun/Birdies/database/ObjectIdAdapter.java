package com.exodus.arhaiyun.Birdies.database;

import java.lang.reflect.Type;

import com.google.common.base.Strings;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.osmapps.golf.common.bean.domain.ObjectId;

public class ObjectIdAdapter implements JsonSerializer<ObjectId>, JsonDeserializer<ObjectId> {

  private final static String OID = "$oid";

  @Override
  public ObjectId deserialize(JsonElement jsonElement, Type type,
      JsonDeserializationContext context) throws JsonParseException {
    if (jsonElement instanceof JsonObject) {
      JsonObject jsonObject = (JsonObject) jsonElement;
      final String id = jsonObject.get(OID).getAsString();
      if (!Strings.isNullOrEmpty(id)) {
        return new ObjectId(id);
      }
    }
    return null;
  }

  @Override
  public JsonElement serialize(ObjectId objectId, Type type, JsonSerializationContext context) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty(OID, objectId.getId());
    return jsonObject;
  }
}
