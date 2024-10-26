package to.wetf.logging.slf4j;

import org.slf4j.Logger;
import org.slf4j.Marker;
import java.util.function.Supplier;

/**
 * Logger that augments log events with marker information.
 * The marker is generated on each log event.
 *
 * @author Simon Templer
 */
public class GenerateMarkerAugmentedLogger extends AbstractMarkerAugmentedLogger {

  private final Supplier<Marker> marker;

  public GenerateMarkerAugmentedLogger(Logger logger, String fqcn, Supplier<Marker> marker) {
    super(logger, fqcn);
    this.marker = marker;
  }

  public GenerateMarkerAugmentedLogger(Logger logger, Supplier<Marker> marker) {
    super(logger);
    this.marker = marker;
  }

  @Override
  protected Marker augmentMarker(Marker marker) {
    return Markers.combineContext(marker, this.marker.get());
  }

}
