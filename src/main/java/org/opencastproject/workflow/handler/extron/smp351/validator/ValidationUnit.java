package org.opencastproject.workflow.handler.extron.smp351.validator;

import java.util.regex.Pattern;

/**
 * 1. regex for validation
 * 2. Key
 * 3. Value for key from SMP JSON */
public class ValidationUnit {
  public final String regex;
  public final String key;
  public final String value;

  public ValidationUnit(String regex, String key, String value) {
    this.regex = regex;
    this.key = key;
    this.value = value;
  }
}
