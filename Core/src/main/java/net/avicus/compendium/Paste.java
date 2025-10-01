package net.avicus.compendium;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Paste {

  private static final String PASTE_URL = "https://paste.mcnebula.net/";

  private final String title;
  private final String author;
  private final String text;
  private final boolean raw;

  public Paste(String title, String author, String text) {
    this(title, author, text, false);
  }

  public Paste(String title, String author, String text, boolean raw) {
    this.title = title;
    this.author = author;
    this.text = text;
    this.raw = raw;
  }

  public String upload() {
    StringBuilder content = new StringBuilder();
    content.append("Title: ").append(this.title).append("\n");
    content.append("Author: ").append(this.author).append("\n\n");
    content.append(this.text);

    HttpURLConnection connection = null;
    try {
      //Create connection
      URL url = new URL(PASTE_URL + "documents");
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
      connection.setDoOutput(true);

      //Send request
      try (OutputStream os = connection.getOutputStream()) {
        byte[] input = content.toString().getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
      }

      //Check response code
      int responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
        return "Paste Failed: HTTP " + responseCode;
      }

      //Get response
      try (BufferedReader br = new BufferedReader(
              new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
          response.append(responseLine.trim());
        }

        //Parse JSON response
        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
        String key = jsonResponse.get("key").getAsString();

        //Return the URL
        return PASTE_URL + (this.raw ? "raw/" : "") + key;
      }

    } catch (IOException e) {
      e.printStackTrace();
      return "Paste Failed: " + e.getMessage();
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }
}