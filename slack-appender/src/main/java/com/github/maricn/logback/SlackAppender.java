package com.github.maricn.logback;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import to.wetf.logging.slf4j.Markers;

public class SlackAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private final static String API_URL = "https://slack.com/api/chat.postMessage";
    private static final int SHORT_FIELD_THRESHOLD = 20;
    private static Layout<ILoggingEvent> defaultLayout = new LayoutBase<ILoggingEvent>() {
        public String doLayout(ILoggingEvent event) {
            return "-- [" + event.getLevel() + "]" +
                    event.getLoggerName() + " - " +
                    event.getFormattedMessage().replaceAll("\n", "\n\t");
        }
    };

    private String webhookUri;
    private String token;
    private String channel;
    private String username;
    private String iconEmoji;
    private Layout<ILoggingEvent> layout = defaultLayout;

    private int timeout = 30_000;

    @Override
    protected void append(final ILoggingEvent evt) {
        try {
            if (webhookUri != null) {
                sendMessageWithWebhookUri(evt);
            } else if (token != null) {
                sendMessageWithToken(evt);
            }
            // just ignore if there is neither token nor webhook configured
        } catch (Exception ex) {
            ex.printStackTrace();
            addError("Error posting log to Slack.com (" + channel + "): " + evt, ex);
        }
    }

    private void sendMessageWithWebhookUri(final ILoggingEvent evt) throws IOException {
        String[] parts = layout.doLayout(evt).split("\n", 2);

        Map<String, Object> message = new HashMap<>();
        if (channel != null && !channel.trim().isEmpty()) {
            message.put("channel", channel);
        }
        if (username != null && !username.trim().isEmpty()) {
            message.put("username", username);
        }
        if (iconEmoji != null && !iconEmoji.trim().isEmpty()) {
            boolean isUrl = false;
            try {
                URI uri = URI.create(iconEmoji);
                isUrl = uri.isAbsolute() && uri.getScheme().startsWith("http");
            } catch (Exception e) {
                // ignore
            }

            if (isUrl) {
                // use icon URL if a URL is provided
                message.put("icon_url", iconEmoji);
            }
            else {
                String emoji = iconEmoji;
                /*
                 * Ensure that the emoji is correctly identified.
                 *
                 * This is done to support configurations like
                 * ${SLACK_LOG_ICON:-exclamation} in the logback config
                 * file, because it does not seem possible to put colons
                 * there (and I did not find a way to escape them).
                 */
                if (!emoji.startsWith(":")) {
                    emoji = ":" + emoji;
                }
                if (!emoji.endsWith(":")) {
                    emoji = emoji + ":";
                }

                message.put("icon_emoji", emoji);
            }
        }

        String mainMsg = parts[0];
        List<Map<String, Object>> attachments = new ArrayList<>();
        String levelColor = getDefaultLevelColor(evt.getLevel());
        
        List<Map<String, Object>> fields = createFields(evt);

        // Send the lines below the first line as an attachment.
        if (parts.length > 1 && !parts[1].trim().isEmpty()) {
            // we have two parts -> use main part a pretext
            Map<String, Object> attachment = new HashMap<>();
            attachment.put("pretext", mainMsg);
            attachment.put("fallback", mainMsg);
            attachment.put("color", levelColor);
            attachment.put("text", parts[1]);
            if (fields != null) {
                attachment.put("fields", fields);
            }
            attachment.put("ts", evt.getTimeStamp());
            attachments.add(attachment);
        }
        else {
            // just message -> use as attachment text
            Map<String, Object> attachment = new HashMap<>();
            attachment.put("fallback", mainMsg);
            attachment.put("color", levelColor);
            attachment.put("text", mainMsg);
            if (fields != null) {
                attachment.put("fields", fields);
            }
            attachment.put("ts", evt.getTimeStamp());
            attachments.add(attachment);
        }
        message.put("attachments", attachments);

        ObjectMapper objectMapper = new ObjectMapper();
        final byte[] bytes = objectMapper.writeValueAsBytes(message);

        postMessage(webhookUri, "application/json", bytes);
    }

    private List<Map<String, Object>> createFields(ILoggingEvent evt) {
        Map<String, String> contextInfo = Markers.getContext(evt.getMarker());
        
        //TODO also include extra information that may be configured?
        
        if (!contextInfo.isEmpty()) {
            List<Map<String, Object>> fields = new ArrayList<>();
            
            for (Entry<String, String> entry : contextInfo.entrySet()) {
                Map<String, Object> field = new HashMap<>();
                field.put("title", entry.getKey());
                field.put("value", entry.getValue());
                
                field.put("short", entry.getValue() == null
                        || entry.getValue().length() <= SHORT_FIELD_THRESHOLD);
            }
            
            return fields;
        }
        else {
            return null;
        }
    }

    /**
     * Get a default color based on the log level.
     *
     * @param level the log level
     * @return the default color, may be <code>null</code>
     */
    private String getDefaultLevelColor(Level level) {
        if (level.isGreaterOrEqual(Level.ERROR)) {
            return "danger";
        }
        if (level.isGreaterOrEqual(Level.WARN)) {
            return "warning";
        }
        if (level.isGreaterOrEqual(Level.INFO)) {
            return "#439FE0";
        }
        return null;
    }

    private void sendMessageWithToken(final ILoggingEvent evt) throws IOException {
        final StringWriter requestParams = new StringWriter();
        requestParams.append("token=").append(token).append("&");

        String[] parts = layout.doLayout(evt).split("\n", 2);
        requestParams.append("text=").append(URLEncoder.encode(parts[0], "UTF-8")).append('&');

        // Send the lines below the first line as an attachment.
        if (parts.length > 1) {
            Map<String, String> attachment = new HashMap<>();
            attachment.put("text", parts[1]);
            List<Map<String, String>> attachments = Collections.singletonList(attachment);
            String json = new ObjectMapper().writeValueAsString(attachments);
            requestParams.append("attachments=").append(URLEncoder.encode(json, "UTF-8")).append('&');
        }
        if (channel != null) {
            requestParams.append("channel=").append(URLEncoder.encode(channel, "UTF-8")).append('&');
        }
        if (username != null) {
            requestParams.append("username=").append(URLEncoder.encode(username, "UTF-8")).append('&');
        }
        if (iconEmoji != null) {
            requestParams.append("icon_emoji=").append(URLEncoder.encode(iconEmoji, "UTF-8"));
        }

        final byte[] bytes = requestParams.toString().getBytes("UTF-8");

        postMessage(API_URL, "application/x-www-form-urlencoded", bytes);
    }

    private void postMessage(String uri, String contentType, byte[] bytes) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) new URL(uri).openConnection();
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setFixedLengthStreamingMode(bytes.length);
        conn.setRequestProperty("Content-Type", contentType);

        final OutputStream os = conn.getOutputStream();
        os.write(bytes);

        os.flush();
        os.close();
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(final String channel) {
        this.channel = channel;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIconEmoji() {
        return iconEmoji;
    }

    public void setIconEmoji(String iconEmojiArg) {
        this.iconEmoji = iconEmojiArg;
        if (iconEmoji != null && !iconEmoji.isEmpty() && iconEmoji.startsWith(":") && !iconEmoji.endsWith(":")) {
            iconEmoji += ":";
        }
    }

    public Layout<ILoggingEvent> getLayout() {
        return layout;
    }

    public void setLayout(final Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getWebhookUri() {
        return webhookUri;
    }

    public void setWebhookUri(String webhookUri) {
        this.webhookUri = webhookUri;
    }

}
