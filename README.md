logging-util
============

Logging utilities for SLF4J and logback.

# SLF4J util

Small utility library related to using the SLF4J API for logging.

Also supports specific uses cases for the Slack appender like creating Markers with context information.

The library is available in the [wetransform artifactory](https://artifactory.wetransform.to): `to.wetransform.logging:slf4j-util:<version>`.

# Slack appender

This is a simple [Logback](http://logback.qos.ch/) appender which pushes logs to [Slack](https://slack.com/) channel.

The appender is based on https://github.com/maricn/logback-slack-appender

Changes include:

- always use attachments to convey the level of the event via the color
- ignore if neither token nor webhook is configured
- ignore empty string configurations
- ignore second line of message if it is empty
- if icon is a http/https URI, use icon_url
- extend emoji by colons if not present
- add timestamp in attachment footer
- support for context markers (as attachment fields) from utility library above

The library is available in the [wetransform artifactory](https://artifactory.wetransform.to): `to.wetransform.logging:slack-appender:<version>`.

Add the Slack appender to your logback configuration file. Here is an example:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<configuration>
  <!-- ... -->

  <appender name="SLACK" class="com.github.maricn.logback.SlackAppender">
    <!-- Slack incoming webhook uri -->
    <webhookUri>${SLACK_LOG_HOOK}</webhookUri>
    <!-- Slack API token (alternative to webhook, webhook is preferred) -->
    <!-- <token>1111111111-1111111-11111111-111111111</token> -->
    <!-- Channel that you want to post -->
    <channel>${SLACK_LOG_CHANNEL:-#logs}</channel>
    <!-- Formatting (you can use Slack formatting - URL links, code formatting, etc.) -->
    <layout class="ch.qos.logback.classic.PatternLayout">
      <pattern>%message - %logger{10}%n%xException{10}%n</pattern>
    </layout>
    <!-- Username of the messages sender -->
    <username>${SLACK_LOG_NAME:-logger}</username>
    <!-- Emoji to be used for messages -->
    <iconEmoji>${SLACK_LOG_ICON:-exclamation}</iconEmoji>
    <!-- Character limit for short attachment fields  -->
    <shortFieldLimit>50</shortFieldLimit>
  </appender>

  <!-- Currently recommended way of using Slack appender -->
  <appender name="ASYNCSLACK" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="SLACK" />
    <!-- Reject NO_SLACK markers -->
    <filter class="ch.qos.logback.core.filter.EvaluatorFilter">
      <evaluator class="ch.qos.logback.classic.boolex.OnMarkerEvaluator">
        <marker>NO_SLACK</marker>
      </evaluator>
      <onMismatch>NEUTRAL</onMismatch>
      <onMatch>DENY</onMatch>
    </filter>
    <!-- Accept any IMPORTANT markers -->
    <filter class="ch.qos.logback.core.filter.EvaluatorFilter">
      <evaluator class="ch.qos.logback.classic.boolex.OnMarkerEvaluator">
        <marker>IMPORTANT</marker>
      </evaluator>
      <onMismatch>NEUTRAL</onMismatch>
      <onMatch>ACCEPT</onMatch>
    </filter>
    <!-- Accept any SLACK markers -->
    <filter class="ch.qos.logback.core.filter.EvaluatorFilter">
      <evaluator class="ch.qos.logback.classic.boolex.OnMarkerEvaluator">
        <marker>SLACK</marker>
      </evaluator>
      <onMismatch>NEUTRAL</onMismatch>
      <onMatch>ACCEPT</onMatch>
    </filter>
    <!-- Threshold filter -->
    <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
      <!-- OFF by default -->
      <level>${SLACK_LOG_LEVEL:-OFF}</level>
    </filter>
  </appender>

  <root>
    <level value="ALL" />
    <appender-ref ref="ASYNC_SLACK" />
  </root>

</configuration>
```
