package to.wetf.logging.slf4j;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.DefaultLoggingEventBuilder;
import org.slf4j.spi.LocationAwareLogger;
import org.slf4j.spi.LoggingEventBuilder;

/**
 * Decorator for a {@link Logger} that augments logging events with marker information.
 *
 * @author Simon Templer
 */
public abstract class AbstractMarkerAugmentedLogger implements Logger {

  private final String fqcn;
  private final boolean locationAware;
  private final Logger logger;

  public AbstractMarkerAugmentedLogger(Logger logger) {
    this(logger, AbstractMarkerAugmentedLogger.class.getName());
  }

  public AbstractMarkerAugmentedLogger(Logger logger, String fqcn) {
    this.fqcn = fqcn;

    this.locationAware = logger instanceof LocationAwareLogger;
    this.logger = logger;
  }

  /**
   * Augment the log event marker.
   *
   * @param marker the current marker, may be <code>null</code>
   * @return the marker to use for the log event, may be <code>null</code>
   */
  protected abstract Marker augmentMarker(Marker marker);

  @Override
  public LoggingEventBuilder makeLoggingEventBuilder(Level level) {
    // create builder directly associated with internal logger
    // reason for this is that if the internal logger implements LoggingEventAware, the builder can directly use it and key value pairs are retained
    var builder = new DefaultLoggingEventBuilder(logger, level);

    // add marker
    Marker marker = augmentMarker(null);
    if (marker != null) {
      return builder.addMarker(marker);
    }

    return builder;
  }

  @Override
  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
  }

  @Override
  public boolean isTraceEnabled(Marker marker) {
    return logger.isTraceEnabled(marker);
  }

  @Override
  public void trace(String msg) {
    Marker marker = augmentMarker(null);
    if (!logger.isTraceEnabled(marker))
      return;

    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.TRACE_INT, msg, null, null);
    } else {
      logger.trace(marker, msg);
    }
  }

  @Override
  public void trace(String format, Object arg) {
    Marker marker = augmentMarker(null);
    if (!logger.isTraceEnabled(marker))
      return;

    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.TRACE_INT, formattedMessage,
          new Object[] { arg }, null);
    } else {
      logger.trace(marker, format, arg);
    }
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    Marker marker = augmentMarker(null);
    if (!logger.isTraceEnabled(marker))
      return;

    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.TRACE_INT, formattedMessage,
          new Object[] { arg1, arg2 }, null);
    } else {
      logger.trace(marker, format, arg1, arg2);
    }
  }

  @Override
  public void trace(String format, Object... args) {
    Marker marker = augmentMarker(null);
    if (!logger.isTraceEnabled(marker))
      return;

    if (locationAware) {
      String formattedMessage = MessageFormatter.arrayFormat(format, args).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.TRACE_INT, formattedMessage, args, null);
    } else {
      logger.trace(marker, format, args);
    }
  }

  @Override
  public void trace(String msg, Throwable t) {
    Marker marker = augmentMarker(null);
    if (!logger.isTraceEnabled(marker))
      return;

    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.TRACE_INT, msg, null, t);
    } else {
      logger.trace(marker, msg, t);
    }
  }

  @Override
  public void trace(Marker marker, String msg) {
    marker = augmentMarker(marker);
    if (!logger.isTraceEnabled(marker))
      return;
    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.TRACE_INT, msg, null, null);
    } else {
      logger.trace(marker, msg);
    }
  }

  @Override
  public void trace(Marker marker, String format, Object arg) {
    marker = augmentMarker(marker);
    if (!logger.isTraceEnabled(marker))
      return;
    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.TRACE_INT, formattedMessage,
          new Object[] { arg }, null);
    } else {
      logger.trace(marker, format, arg);
    }
  }

  @Override
  public void trace(Marker marker, String format, Object arg1, Object arg2) {
    marker = augmentMarker(marker);
    if (!logger.isTraceEnabled(marker))
      return;
    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.TRACE_INT, formattedMessage,
          new Object[] { arg1, arg2 }, null);
    } else {
      logger.trace(marker, format, arg1, arg2);
    }
  }

  @Override
  public void trace(Marker marker, String format, Object... args) {
    marker = augmentMarker(marker);
    if (!logger.isTraceEnabled(marker))
      return;
    if (locationAware) {
      String formattedMessage = MessageFormatter.arrayFormat(format, args).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.TRACE_INT, formattedMessage, args, null);
    } else {
      logger.trace(marker, format, args);
    }
  }

  @Override
  public void trace(Marker marker, String msg, Throwable t) {
    marker = augmentMarker(marker);
    if (!logger.isTraceEnabled(marker))
      return;
    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.TRACE_INT, msg, null, t);
    } else {
      logger.trace(marker, msg, t);
    }
  }

  @Override
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return logger.isDebugEnabled(marker);
  }

  @Override
  public void debug(String msg) {
    Marker marker = augmentMarker(null);
    if (!logger.isDebugEnabled(marker))
      return;

    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.DEBUG_INT, msg, null, null);
    } else {
      logger.debug(marker, msg);
    }
  }

  @Override
  public void debug(String format, Object arg) {
    Marker marker = augmentMarker(null);
    if (!logger.isDebugEnabled(marker))
      return;

    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.DEBUG_INT, formattedMessage,
          new Object[] { arg }, null);
    } else {
      logger.debug(marker, format, arg);
    }
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    Marker marker = augmentMarker(null);
    if (!logger.isDebugEnabled(marker))
      return;

    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.DEBUG_INT, formattedMessage,
          new Object[] { arg1, arg2 }, null);
    } else {
      logger.debug(marker, format, arg1, arg2);
    }
  }

  @Override
  public void debug(String format, Object... argArray) {
    Marker marker = augmentMarker(null);
    if (!logger.isDebugEnabled(marker))
      return;

    if (locationAware) {
      FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.DEBUG_INT, ft.getMessage(), ft.getArgArray(),
          ft.getThrowable());
    } else {
      logger.debug(marker, format, argArray);
    }
  }

  @Override
  public void debug(String msg, Throwable t) {
    Marker marker = augmentMarker(null);
    if (!logger.isDebugEnabled(marker))
      return;

    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.DEBUG_INT, msg, null, t);
    } else {
      logger.debug(marker, msg, t);
    }
  }

  @Override
  public void debug(Marker marker, String msg) {
    marker = augmentMarker(marker);
    if (!logger.isDebugEnabled(marker))
      return;
    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.DEBUG_INT, msg, null, null);
    } else {
      logger.debug(marker, msg);
    }
  }

  @Override
  public void debug(Marker marker, String format, Object arg) {
    marker = augmentMarker(marker);
    if (!logger.isDebugEnabled(marker))
      return;
    if (locationAware) {
      FormattingTuple ft = MessageFormatter.format(format, arg);
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.DEBUG_INT, ft.getMessage(), ft.getArgArray(),
          ft.getThrowable());
    } else {
      logger.debug(marker, format, arg);
    }
  }

  @Override
  public void debug(Marker marker, String format, Object arg1, Object arg2) {
    marker = augmentMarker(marker);
    if (!logger.isDebugEnabled(marker))
      return;
    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.DEBUG_INT, formattedMessage,
          new Object[] { arg1, arg2 }, null);
    } else {
      logger.debug(marker, format, arg1, arg2);
    }
  }

  @Override
  public void debug(Marker marker, String format, Object... argArray) {
    marker = augmentMarker(marker);
    if (!logger.isDebugEnabled(marker))
      return;
    if (locationAware) {

      FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.DEBUG_INT, ft.getMessage(), argArray,
          ft.getThrowable());
    } else {
      logger.debug(marker, format, argArray);
    }
  }

  @Override
  public void debug(Marker marker, String msg, Throwable t) {
    marker = augmentMarker(marker);
    if (!logger.isDebugEnabled(marker))
      return;
    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.DEBUG_INT, msg, null, t);
    } else {
      logger.debug(marker, msg, t);
    }
  }

  @Override
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return logger.isInfoEnabled(marker);
  }

  @Override
  public void info(String msg) {
    Marker marker = augmentMarker(null);
    if (!logger.isInfoEnabled(marker))
      return;

    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.INFO_INT, msg, null, null);
    } else {
      logger.info(marker, msg);
    }
  }

  @Override
  public void info(String format, Object arg) {
    Marker marker = augmentMarker(null);
    if (!logger.isInfoEnabled(marker))
      return;

    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.INFO_INT, formattedMessage,
          new Object[] { arg }, null);
    } else {
      logger.info(marker, format, arg);
    }
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    Marker marker = augmentMarker(null);
    if (!logger.isInfoEnabled(marker))
      return;

    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.INFO_INT, formattedMessage,
          new Object[] { arg1, arg2 }, null);
    } else {
      logger.info(marker, format, arg1, arg2);
    }
  }

  @Override
  public void info(String format, Object... args) {
    Marker marker = augmentMarker(null);
    if (!logger.isInfoEnabled(marker))
      return;

    if (locationAware) {
      String formattedMessage = MessageFormatter.arrayFormat(format, args).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.INFO_INT, formattedMessage, args, null);
    } else {
      logger.info(marker, format, args);
    }
  }

  @Override
  public void info(String msg, Throwable t) {
    Marker marker = augmentMarker(null);
    if (!logger.isInfoEnabled(marker))
      return;

    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.INFO_INT, msg, null, t);
    } else {
      logger.info(marker, msg, t);
    }
  }

  @Override
  public void info(Marker marker, String msg) {
    marker = augmentMarker(marker);
    if (!logger.isInfoEnabled(marker))
      return;
    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.INFO_INT, msg, null, null);
    } else {
      logger.info(marker, msg);
    }
  }

  @Override
  public void info(Marker marker, String format, Object arg) {
    marker = augmentMarker(marker);
    if (!logger.isInfoEnabled(marker))
      return;
    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.INFO_INT, formattedMessage,
          new Object[] { arg }, null);
    } else {
      logger.info(marker, format, arg);
    }
  }

  @Override
  public void info(Marker marker, String format, Object arg1, Object arg2) {
    marker = augmentMarker(marker);
    if (!logger.isInfoEnabled(marker))
      return;
    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.INFO_INT, formattedMessage,
          new Object[] { arg1, arg2 }, null);
    } else {
      logger.info(marker, format, arg1, arg2);
    }
  }

  @Override
  public void info(Marker marker, String format, Object... args) {
    marker = augmentMarker(marker);
    if (!logger.isInfoEnabled(marker))
      return;
    if (locationAware) {
      String formattedMessage = MessageFormatter.arrayFormat(format, args).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.INFO_INT, formattedMessage, args, null);
    } else {
      logger.info(marker, format, args);
    }
  }

  @Override
  public void info(Marker marker, String msg, Throwable t) {
    marker = augmentMarker(marker);
    if (!logger.isInfoEnabled(marker))
      return;
    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.INFO_INT, msg, null, t);
    } else {
      logger.info(marker, msg, t);
    }
  }

  @Override
  public boolean isWarnEnabled() {
    return logger.isWarnEnabled();
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return logger.isWarnEnabled(marker);
  }

  @Override
  public void warn(String msg) {
    Marker marker = augmentMarker(null);
    if (!logger.isWarnEnabled(marker))
      return;

    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.WARN_INT, msg, null, null);
    } else {
      logger.warn(marker, msg);
    }
  }

  @Override
  public void warn(String format, Object arg) {
    Marker marker = augmentMarker(null);
    if (!logger.isWarnEnabled(marker))
      return;

    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.WARN_INT, formattedMessage,
          new Object[] { arg }, null);
    } else {
      logger.warn(marker, format, arg);
    }
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    Marker marker = augmentMarker(null);
    if (!logger.isWarnEnabled(marker))
      return;

    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.WARN_INT, formattedMessage,
          new Object[] { arg1, arg2 }, null);
    } else {
      logger.warn(marker, format, arg1, arg2);
    }
  }

  @Override
  public void warn(String format, Object... args) {
    Marker marker = augmentMarker(null);
    if (!logger.isWarnEnabled(marker))
      return;

    if (locationAware) {
      String formattedMessage = MessageFormatter.arrayFormat(format, args).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.WARN_INT, formattedMessage, args, null);
    } else {
      logger.warn(marker, format, args);
    }
  }

  @Override
  public void warn(String msg, Throwable t) {
    Marker marker = augmentMarker(null);
    if (!logger.isWarnEnabled(marker))
      return;

    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.WARN_INT, msg, null, t);
    } else {
      logger.warn(marker, msg, t);
    }
  }

  @Override
  public void warn(Marker marker, String msg) {
    marker = augmentMarker(marker);
    if (!logger.isWarnEnabled(marker))
      return;
    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.WARN_INT, msg, null, null);
    } else {
      logger.warn(marker, msg);
    }
  }

  @Override
  public void warn(Marker marker, String format, Object arg) {
    marker = augmentMarker(marker);
    if (!logger.isWarnEnabled(marker))
      return;
    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.WARN_INT, formattedMessage,
          new Object[] { arg }, null);
    } else {
      logger.warn(marker, format, arg);
    }
  }

  @Override
  public void warn(Marker marker, String format, Object arg1, Object arg2) {
    marker = augmentMarker(marker);
    if (!logger.isWarnEnabled(marker))
      return;
    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.WARN_INT, formattedMessage,
          new Object[] { arg1, arg2 }, null);
    } else {
      logger.warn(marker, format, arg1, arg2);
    }
  }

  @Override
  public void warn(Marker marker, String format, Object... args) {
    marker = augmentMarker(marker);
    if (!logger.isWarnEnabled(marker))
      return;
    if (locationAware) {
      String formattedMessage = MessageFormatter.arrayFormat(format, args).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.WARN_INT, formattedMessage, args, null);
    } else {
      logger.warn(marker, format, args);
    }
  }

  @Override
  public void warn(Marker marker, String msg, Throwable t) {
    marker = augmentMarker(marker);
    if (!logger.isWarnEnabled(marker))
      return;
    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.WARN_INT, msg, null, t);
    } else {
      logger.warn(marker, msg, t);
    }
  }

  @Override
  public boolean isErrorEnabled() {
    return logger.isErrorEnabled();
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return logger.isErrorEnabled(marker);
  }

  @Override
  public void error(String msg) {
    Marker marker = augmentMarker(null);
    if (!logger.isErrorEnabled(marker))
      return;

    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.ERROR_INT, msg, null, null);
    } else {
      logger.error(marker, msg);
    }
  }

  @Override
  public void error(String format, Object arg) {
    Marker marker = augmentMarker(null);
    if (!logger.isErrorEnabled(marker))
      return;

    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.ERROR_INT, formattedMessage,
          new Object[] { arg }, null);
    } else {
      logger.error(marker, format, arg);
    }
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    Marker marker = augmentMarker(null);
    if (!logger.isErrorEnabled(marker))
      return;

    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.ERROR_INT, formattedMessage,
          new Object[] { arg1, arg2 }, null);
    } else {
      logger.error(marker, format, arg1, arg2);
    }
  }

  @Override
  public void error(String format, Object... args) {
    Marker marker = augmentMarker(null);
    if (!logger.isErrorEnabled(marker))
      return;

    if (locationAware) {
      String formattedMessage = MessageFormatter.arrayFormat(format, args).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.ERROR_INT, formattedMessage, args, null);
    } else {
      logger.error(marker, format, args);
    }
  }

  @Override
  public void error(String msg, Throwable t) {
    Marker marker = augmentMarker(null);
    if (!logger.isErrorEnabled(marker))
      return;

    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.ERROR_INT, msg, null, t);
    } else {
      logger.error(marker, msg, t);
    }
  }

  @Override
  public void error(Marker marker, String msg) {
    marker = augmentMarker(marker);
    if (!logger.isErrorEnabled(marker))
      return;
    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.ERROR_INT, msg, null, null);
    } else {
      logger.error(marker, msg);
    }
  }

  @Override
  public void error(Marker marker, String format, Object arg) {
    marker = augmentMarker(marker);
    if (!logger.isErrorEnabled(marker))
      return;
    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.ERROR_INT, formattedMessage,
          new Object[] { arg }, null);
    } else {
      logger.error(marker, format, arg);
    }
  }

  @Override
  public void error(Marker marker, String format, Object arg1, Object arg2) {
    marker = augmentMarker(marker);
    if (!logger.isErrorEnabled(marker))
      return;
    if (locationAware) {
      String formattedMessage = MessageFormatter.format(format, arg1, arg2).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.ERROR_INT, formattedMessage,
          new Object[] { arg1, arg2 }, null);
    } else {
      logger.error(marker, format, arg1, arg2);
    }
  }

  @Override
  public void error(Marker marker, String format, Object... args) {
    marker = augmentMarker(marker);
    if (!logger.isErrorEnabled(marker))
      return;
    if (locationAware) {
      String formattedMessage = MessageFormatter.arrayFormat(format, args).getMessage();
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.ERROR_INT, formattedMessage, args, null);
    } else {
      logger.error(marker, format, args);
    }
  }

  @Override
  public void error(Marker marker, String msg, Throwable t) {
    marker = augmentMarker(marker);
    if (!logger.isErrorEnabled(marker))
      return;
    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, LocationAwareLogger.ERROR_INT, msg, null, t);
    } else {
      logger.error(marker, msg, t);
    }
  }

  @Override
  public String getName() {
    return logger.getName();
  }

}
