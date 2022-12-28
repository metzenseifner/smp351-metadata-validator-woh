package org.opencastproject.workflow.handler.extron.smp351.validator.utilities;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Result;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CheapSmp351ProprietaryMetadataReader {

  private Map<String, Object> metadataObj;

  public CheapSmp351ProprietaryMetadataReader(InputStream inputStream) {
    try {
      String jsonString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      String.format("Stream: %s", inputStream);
      Map<String, Object> mapObj = new Gson().fromJson(jsonString, new TypeToken<HashMap<String, Object>>() {
      }.getType());
      Map<String, Object> packageObj = (Map<String, Object>) mapObj.get("package");
      this.metadataObj = (Map<String, Object>) packageObj.get("metadata");
    } catch (Exception e) {
      throw new RuntimeException(String.format("Could not process Json File from SMP because: %s", e));
    }
  }

  public Result<String> get(String key) {
    try {
      Object value = this.metadataObj.get(key);
      if (value == null) return Result.failure(String.format("Unknown key in map: (%s in %s)", key, this.metadataObj));
      return Result.of((String) value);
    } catch (Exception e) {
      return Result.failure(e);
    }
  }
}
