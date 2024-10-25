package to.wetf.logging.slf4j;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.DefaultLoggingEvent;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;
import org.slf4j.spi.LoggingEventAware;

import java.util.List;

/**
 * Decorator for a {@link Logger} that augments logging events with marker information.
 *
 * @author Simon Templer
 */
public abstract class AbstractMarkerAugmentedLogger implements Logger, LocationAwareLogger, LoggingEventAware {

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
  public void log(Marker marker, String fqcn, int level, String message, Object[] argArray, Throwable t) {
    marker = augmentMarker(marker);
    if (locationAware) {
      ((LocationAwareLogger) logger).log(marker, fqcn, level, message, argArray, t);
    } else {
      switch (level) {
        case TRACE_INT:
          logger.trace(marker, message, argArray, t);
          break;
        case DEBUG_INT:
          logger.debug(marker, message, argArray, t);
          break;
        case INFO_INT:
          logger.info(marker, message, argArray, t);
          break;
        case WARN_INT:
          logger.warn(marker, message, argArray, t);
          break;
        case ERROR_INT:
          logger.error(marker, message, argArray, t);
          break;
        default:
          throw new IllegalStateException("Level number " + level + " is not recognized.");
      }
    }
  }

  @Override
  public void log(LoggingEvent event) {
    Marker marker = augmentMarker(null);
    if (marker != null) {
      // create a copy with the added marker
      var copy = new DefaultLoggingEvent(event.getLevel(), this);

      copy.setCallerBoundary(event.getCallerBoundary());
      copy.setMessage(event.getMessage());
      copy.setThrowable(event.getThrowable());
      copy.setTimeStamp(event.getTimeStamp());

      if (event.getArgumentArray() != null) {
        copy.addArguments(event.getArgumentArray());
      }

      if (event.getKeyValuePairs() != null) {
        for (KeyValuePair kvp : event.getKeyValuePairs()) {
          copy.addKeyValue(kvp.key, kvp.value);
        }
      }

      if (event.getMarkers() != null) {
        for (Marker m : event.getMarkers()) {
          copy.addMarker(m);
        }
      }

      copy.addMarker(marker);

      event = copy;
    }

    if (logger instanceof LoggingEventAware) {
      ((LoggingEventAware) logger).log(event);
      return;
    }
    if (locationAware) {
      logViaLocationAwareLoggerAPI((LocationAwareLogger) logger, event);
    } else {
      logViaPublicSLF4JLoggerAPI(event);
    }
  }

  private void logViaLocationAwareLoggerAPI(LocationAwareLogger locationAwareLogger, LoggingEvent aLoggingEvent) {
    String msg = aLoggingEvent.getMessage();
    List<Marker> markerList = aLoggingEvent.getMarkers();
    String mergedMessage = mergeMarkersAndKeyValuePairsAndMessage(aLoggingEvent);
    locationAwareLogger.log(null, aLoggingEvent.getCallerBoundary(), aLoggingEvent.getLevel().toInt(),
      mergedMessage,
      aLoggingEvent.getArgumentArray(), aLoggingEvent.getThrowable());
  }

  private void logViaPublicSLF4JLoggerAPI(LoggingEvent aLoggingEvent) {
    Object[] argArray = aLoggingEvent.getArgumentArray();
    int argLen = argArray == null ? 0 : argArray.length;

    Throwable t = aLoggingEvent.getThrowable();
    int tLen = t == null ? 0 : 1;

    Object[] combinedArguments = new Object[argLen + tLen];

    if(argArray != null) {
      System.arraycopy(argArray, 0, combinedArguments, 0, argLen);
    }
    if(t != null) {
      combinedArguments[argLen] = t;
    }

    String mergedMessage = mergeMarkersAndKeyValuePairsAndMessage(aLoggingEvent);

    switch(aLoggingEvent.getLevel()) {
      case TRACE:
        logger.trace(mergedMessage, combinedArguments);
        break;
      case DEBUG:
        logger.debug(mergedMessage, combinedArguments);
        break;
      case INFO:
        logger.info(mergedMessage, combinedArguments);
        break;
      case WARN:
        logger.warn(mergedMessage, combinedArguments);
        break;
      case ERROR:
        logger.error(mergedMessage, combinedArguments);
        break;
    }
  }

  /**
   * Prepend markers and key-value pairs to the message.
   *
   * @param aLoggingEvent
   *
   * @return
   */
  private String mergeMarkersAndKeyValuePairsAndMessage(LoggingEvent aLoggingEvent) {
    StringBuilder sb = mergeMarkers(aLoggingEvent.getMarkers(), null);
    sb = mergeKeyValuePairs(aLoggingEvent.getKeyValuePairs(), sb);
    final String mergedMessage = mergeMessage(aLoggingEvent.getMessage(), sb);
    return mergedMessage;
  }

  private StringBuilder mergeMarkers(List<Marker> markerList, StringBuilder sb) {
    if(markerList == null || markerList.isEmpty())
      return sb;

    if(sb == null)
      sb = new StringBuilder();

    for(Marker marker : markerList) {
      sb.append(marker);
      sb.append(' ');
    }
    return sb;
  }

  private StringBuilder mergeKeyValuePairs(List<KeyValuePair> keyValuePairList, StringBuilder sb) {
    if(keyValuePairList == null || keyValuePairList.isEmpty())
      return sb;

    if(sb == null)
      sb = new StringBuilder();

    for(KeyValuePair kvp : keyValuePairList) {
      sb.append(kvp.key);
      sb.append('=');
      sb.append(kvp.value);
      sb.append(' ');
    }
    return sb;
  }

  private String mergeMessage(String msg, StringBuilder sb) {
    if(sb != null) {
      sb.append(msg);
      return sb.toString();
    } else {
      return msg;
    }
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
