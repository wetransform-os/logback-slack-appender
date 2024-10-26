package to.wetf.logging.slf4j;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * Logger that augments log events with marker information.
 *
 * @author Simon Templer
 */
public class MarkerAugmentedLogger extends AbstractMarkerAugmentedLogger {

  private final Marker marker;

  public MarkerAugmentedLogger(Logger logger, String fqcn, Marker marker) {
    super(logger, fqcn);
    this.marker = marker;
  }

  public MarkerAugmentedLogger(Logger logger, Marker marker) {
    super(logger);
    this.marker = marker;
  }

  @Override
  protected Marker augmentMarker(Marker marker) {
    return Markers.combineContext(marker, this.marker);
  }

}
