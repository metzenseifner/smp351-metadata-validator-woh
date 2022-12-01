package org.opencastproject.workflow.handler.extron.smp351.validator;

import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Result;

/**
 * Data Class for ConfKey (Product Type)
 */
public final class ConfKey {

  public final String name;
  public final String key;
  public final String defaultValue;

  public ConfKey(String name, String key, String defaultValue) {
    this.name = name;
    this.key = key;
    this.defaultValue = defaultValue;
  }

  public static ConfKey of(String key) {
    return new ConfKey(key.toUpperCase(), key, "");
  }

  public static ConfKey of(String name, String key) {
    return new ConfKey(name, key, "");
  }

  public static ConfKey of(String name, String key, String defaultValue) {
    return new ConfKey(name, key, defaultValue);
  }
}
