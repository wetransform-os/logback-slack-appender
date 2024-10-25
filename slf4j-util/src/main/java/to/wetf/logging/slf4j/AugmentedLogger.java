package to.wetf.logging.slf4j;

import org.slf4j.Logger;
import org.slf4j.event.KeyValuePair;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AugmentedLogger {

  public static Logger withContext(Logger logger, KeyValuePair... context) {
    return withContext(logger, toMap(context));
  }

  private static Map<String, String> toMap(KeyValuePair[] context) {
    return Arrays.stream(context).collect(Collectors.toMap(kv -> kv.key, kv -> kv.value == null ? null : kv.value.toString()));
  }

  public static Logger withContext(Logger logger, Map<String, String> context) {
    return new MarkerAugmentedLogger(logger, Markers.contextMarker(context));
  }

  public static Logger withEventContext(Logger logger, Supplier<Map<String, String>> contextSupplier) {
    return new GenerateMarkerAugmentedLogger(logger, () -> Markers.contextMarker(contextSupplier.get()));
  }

}
