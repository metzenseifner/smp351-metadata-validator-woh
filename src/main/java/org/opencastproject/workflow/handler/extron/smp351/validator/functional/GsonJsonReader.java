package org.opencastproject.workflow.handler.extron.smp351.validator.functional;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public class GsonJsonReader {

  private final Result<String> jsonString;
  private final String source;

  public static GsonJsonReader fileJsonReader(String filePath) {
    return new GsonJsonReader(readJsonFromFile(filePath), String.format("File: %s", filePath));
  }

  public static GsonJsonReader stringJsonReader(String string) {
    return new GsonJsonReader(readJsonFromString(string), String.format("String: %s", string));
  }

  public static GsonJsonReader streamJsonReader(InputStream inputStream) {
    return new GsonJsonReader(readJsonFromStream(inputStream), String.format("Stream: %s", inputStream));
  }

  private GsonJsonReader(Result<String> jsonString, String source) {
    this.jsonString = jsonString;
    this.source = source;
  }


  // TODO handle   JsonSyntaxException, JsonIOException for fromJson with separate messages

  private static Result<String> readJsonFromString(String string) {
    try (Reader reader = new StringReader(string)) {
      Gson gson = new Gson();
      return Result.of(gson.fromJson(reader, String.class));
    } catch (Exception e) {
      return Result.failure(String.format("%s reading property string: %s", e.getClass().getSimpleName(), string));
    }
  }

  private static Result<String> readJsonFromFile(String filePath) {
    try (InputStream inputStream = GsonJsonReader.class.getClassLoader().getResourceAsStream(filePath)) {
      Gson gson = new Gson();
      return Result.of(gson.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), String.class));
    } catch (NullPointerException e) {
      return Result.failure(String.format("File not found in classpath: %s", filePath));
    } catch (IOException e) {
      return Result.failure(String.format("%s occurred while reading classpath resource: %s", e.getClass().getSimpleName(), filePath));
    } catch (Exception e) {
      return Result.failure(String.format("%s occurred while reading classpath resource: %s", e.getClass().getSimpleName(), filePath));
    }
  }

  private static Result<String> readJsonFromStream(InputStream stream) {
    try {
      Gson gson = new Gson();
      return Result.of(gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), String.class));
    } catch (Exception e) {
      return Result.failure(String.format("%s occurred while reading stream: %s", e.getClass().getSimpleName(), stream));
    }
  }

}
