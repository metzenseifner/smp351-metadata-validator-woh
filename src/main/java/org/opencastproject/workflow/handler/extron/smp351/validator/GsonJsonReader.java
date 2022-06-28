package org.opencastproject.workflow.handler.extron.smp351.validator;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Result;

import java.io.IOException;
import java.io.InputStream;

public class GsonJsonReader {

    private final Result<String> jsonString;

    public GsonJsonReader(String configFilePath) {
        this.jsonString = readJsonFromFile(configFilePath);
    }

    private Result<String> readJsonFromFile(String configFilePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(configFilePath)) {
            Gson gson = new Gson();
            String streamedString = IOUtils.toString(inputStream, "UTF-8");
            String json = gson.fromJson(streamedString, String.class);
            return Result.of(json);
        } catch (NullPointerException e) {
            return Result.failure(String.format("File not found on classpath :: %s", configFilePath));
        } catch (IOException e) {
            return Result.failure(String.format("%s reading classpath resource :: %s", e.getClass().getSimpleName(), configFilePath));
        } catch (Exception e) {
            return Result.failure(String.format("%s occurred while reading classpath resource :: %s", e.getClass().getSimpleName(), configFilePath));
        }
    }

    private Result<String> readJsonFromString(String string) {
        try () {
            Gson gson = new Gson();
            gson.fromJson()
        } catch ()
        {
        }
    }

    public static GsonJsonReader fileJsonReader(String filePath) {

    }

    public static GsonJsonReader stringJsonReader(String string) {

    }


}
