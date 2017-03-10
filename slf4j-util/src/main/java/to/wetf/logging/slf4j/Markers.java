package to.wetf.logging.slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Helper for creating and accessing markers.
 *
 * @author Simon Templer
 */
public class Markers {

  public static final String MARKER_NAME_SLACK = "SLACK";
  public static final String MARKER_NAME_NO_SLACK = "NO_SLACK";

  public static final String MARKER_NAME_WRAPPER = "WRAPPER";

  public static final String MARKER_NAME_CONTEXT = "CONTEXT";
  public static final String MARKER_PREFIX_CONTEXT_KEY = "CONTEXT_KEY_";
  public static final String MARKER_PREFIX_CONTEXT_VALUE = "CONTEXT_VAL_";

  /**
   * Marker that marks a message that should specifically not be logged via Slack.
   */
  public static final Marker NO_SLACK = MarkerFactory.getMarker(MARKER_NAME_NO_SLACK);

  /**
   * Marker that marks a message that should specifically be logged via Slack.
   */
  public static final Marker SLACK = MarkerFactory.getMarker(MARKER_NAME_SLACK);

  /**
   * Create a marker with context information.
   *
   * @param context the context map, may be <code>null</code>
   * @return the context marker, may be <code>null</code>
   */
  public static Marker contextMarker(Map<String, String> context) {
    if (context != null && !context.isEmpty()) {
      Marker ctx = MarkerFactory.getDetachedMarker(MARKER_NAME_CONTEXT);
      for (Entry<String, String> entry : context.entrySet()) {
        Marker key = MarkerFactory.getDetachedMarker(MARKER_PREFIX_CONTEXT_KEY + entry.getKey());
        Marker value = MarkerFactory.getDetachedMarker(MARKER_PREFIX_CONTEXT_VALUE + entry.getValue());
        key.add(value);
        ctx.add(key);
      }
      return ctx;
    }
    else {
      return null;
    }
  }

  /**
   * Create a marker that should be logged via slack and may contain context information.
   *
   * @param context the context map, may be <code>null</code>
   * @return the context marker, may be <code>null</code>
   */
  public static Marker slackMarker(Map<String, String> context) {
    return combineMarkers(SLACK, contextMarker(context));
  }

  /**
   * Create a combined marker w/ the given ones.
   * @param markers
   * @return
   */
  public static Marker combineMarkers(Marker... markers) {
    List<Marker> list = new ArrayList<>();
    for (Marker marker : markers) {
      if (marker != null) {
        list.add(marker);
      }
    }

    if (list.isEmpty()) {
      return null;
    }
    else if (list.size() == 1) {
      return list.get(0);
    }
    else {
      Marker wrapper = MarkerFactory.getDetachedMarker(MARKER_NAME_WRAPPER);
      for (Marker marker : list) {
        wrapper.add(marker);
      }
      return wrapper;
    }
  }

  /**
   * Get context information from a marker.
   *
   * @param marker the marker
   * @return the map with context information, an empty map if none could be found
   */
  public static Map<String, String> getContext(Marker marker) {
    Map<String, String> result = new HashMap<>();

    Marker context = findMarker(marker, MARKER_NAME_CONTEXT);
    if (context != null && context.hasReferences()) {
      Iterator<Marker> it = context.iterator();
      while (it.hasNext()) {
        Marker key = it.next();
        if (key.getName().startsWith(MARKER_PREFIX_CONTEXT_KEY) && key.hasReferences()) {
          Marker value = key.iterator().next();
          if (value.getName().startsWith(MARKER_PREFIX_CONTEXT_VALUE)) {
            String valueString = value.getName().substring(MARKER_PREFIX_CONTEXT_VALUE.length());
            String keyString = key.getName().substring(MARKER_PREFIX_CONTEXT_KEY.length());
            result.put(keyString, valueString);
          }
        }
      }
    }

    return result;
  }

  /**
   * Find the marker with the given name in the given marker or its references
   *
   * @param marker the marker to search
   * @param name the name of the marker to find
   *
   * @return the found marker or <code>null</code>
   */
  public static Marker findMarker(Marker marker, String name) {
    if (marker == null) {
      return null;
    }

    if (marker.getName().equals(name)) {
      return marker;
    }
    else if (marker.hasReferences()) {
      Iterator<?> refs = marker.iterator();
      while (refs.hasNext()) {
        Object ref = refs.next();
        if (ref instanceof Marker) {
          Marker result = findMarker((Marker) ref, name);
          if (result != null) {
            return result;
          }
        }
      }

      return null;
    }
    else {
      return null;
    }
  }

}
