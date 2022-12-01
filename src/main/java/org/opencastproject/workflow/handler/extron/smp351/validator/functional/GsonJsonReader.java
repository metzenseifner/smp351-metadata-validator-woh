package org.opencastproject.workflow.handler.extron.smp351.validator.functional;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class GsonJsonReader {

  private final Result<Map<String, String>> jsonString;
  private final String source;

  // Construction Factories

  public static GsonJsonReader fileJsonReader(String filePath) {
    return new GsonJsonReader(readJsonFromFile(filePath), String.format("File: %s", filePath));
  }

  public static GsonJsonReader stringJsonReader(String string) {
    return new GsonJsonReader(readJsonFromString(string), String.format("String: %s", string));
  }

  public static GsonJsonReader streamJsonReader(InputStream inputStream) {
    return new GsonJsonReader(readJsonFromStream(inputStream), String.format("Stream: %s", inputStream));
  }

  /* Private constructor to support construction from multiple sources yet keeping contstructor a simple (String json, String source) */
  private GsonJsonReader(Result<Map<String, String>> jsonString, String source) {
    this.jsonString = jsonString;
    this.source = source;
  }

  // Useful functions to extract data functionally

  public Result<String> getAsString(String name) {
    try {
      return jsonString.flatMap(j -> j.get(name));
    } catch (Exception e) {
      return Result.failure(e);
    }
  }

  // TODO handle   JsonSyntaxException, JsonIOException for fromJson with separate messages

  // Private functions implement the complex logic for each factory yielding strings in each case for the constructor
  // whereby each factory is a different source.
  private static Result<Map<String, String>> readJsonFromString(String string) {
    try (Reader reader = new StringReader(string)) {
      Gson gson = new Gson();
      return Result.of(gson.fromJson(reader, new TypeToken<Map<String, String>>() {}.getType()));
    } catch (Exception e) {
      return Result.failure(String.format("%s reading property string: %s", e.getClass().getSimpleName(), string));
    }
  }

  private static Result<Map<String, String>> readJsonFromFile(String filePath) {
    try (InputStream inputStream = GsonJsonReader.class.getClassLoader().getResourceAsStream(filePath)) {
      Gson gson = new Gson();
      return Result.of(gson.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), new TypeToken<Map<String, String>>() {}.getType()));
    } catch (NullPointerException e) {
      return Result.failure(String.format("File not found in classpath: %s", filePath));
    } catch (IOException e) {
      return Result.failure(String.format("%s occurred while reading classpath resource: %s", e.getClass().getSimpleName(), filePath));
    } catch (Exception e) {
      return Result.failure(String.format("%s occurred while reading classpath resource: %s", e.getClass().getSimpleName(), filePath));
    }
  }

  private static Result<Map<String, String>> readJsonFromStream(InputStream stream) {
    try {
      Gson gson = new Gson();
      return Result.of(gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), new TypeToken<Map<String, String>>() {}.getType()));
    } catch (Exception e) {
      return Result.failure(String.format("%s occurred while reading stream: %s", e.getClass().getSimpleName(), stream));
    }
  }

}
