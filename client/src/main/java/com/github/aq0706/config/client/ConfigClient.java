package com.github.aq0706.config.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ConfigClient {

    private String host;
    private int port;

    public ConfigClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String get(String nameSpace, String appName, String key) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(host + ":" + port + "/config" +
                    "?namespace=" + encode(nameSpace) +
                    "&appName=" + encode(appName) +
                    "&key=" + encode(key));
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            conn.connect();

            if (conn.getResponseCode() == 200) {
                String contentLengthStr = conn.getHeaderField("Content-length");
                if (contentLengthStr == null) {
                    return "";
                }
                int contentLength = Integer.parseInt(contentLengthStr);
                byte[] responseBody = new byte[contentLength];
                int ignored = conn.getInputStream().read(responseBody);
                conn.getInputStream().close();

                return new String(responseBody);
            }

            return "";
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Error occurred.");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String encode(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, StandardCharsets.UTF_8.toString());
    }

}
