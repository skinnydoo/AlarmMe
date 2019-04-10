package ca.poly.inf8405.alarmme.utils

import com.google.gson.*
import org.joda.time.DateTime
import java.lang.reflect.Type

/**
 * GSON serialiser/deserialiser for converting Joda [DateTime] objects.
 */
class DateTimeConverter : JsonSerializer<DateTime>, JsonDeserializer<DateTime> {

  override fun serialize(
    src: DateTime,
    typeOfSrc: Type,
    context: JsonSerializationContext
  ): JsonElement = JsonPrimitive(src.toString())
  
  
  @Throws(JsonParseException::class)
  override fun deserialize(
    json: JsonElement,
    typeOfT: Type,
    context: JsonDeserializationContext
  ): DateTime? {
    // Do not try to deserialize null or empty values
    if (json.asString == null || json.asString.isEmpty()) {
      return null
    }
    
    return DateTime(json.asLong)
  }
}
