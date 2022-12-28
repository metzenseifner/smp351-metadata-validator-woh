package org.opencastproject.workflow.handler.extron.smp351.validator;

public class Smp351MetadataValidatorOperationConfig {
    static public final String CONTRIBUTOR = "dc:contributor";
    static public final String COURSE = "dc:course";
    static public final String COVERAGE = "dc:coverage";
    static public final String CREATOR = "dc:creator";
    static public final String DATE = "dc:date";
    static public final String DESCRIPTION = "dc:description";
    static public final String FORMAT = "dc:format";
    static public final String IDENTIFIER = "dc:identifier";
    static public final String LANGUAGE = "dc:language";
    static public final String PUBLISHER = "dc:publisher";
    static public final String RELATION = "dc:relation";
    static public final String RIGHTS = "dc:rights";
    static public final String SOURCE = "dc:source";
    static public final String SUBJECT = "dc:subject";
    static public final String TITLE = "dc:title";
    static public final String TYPE = "dc:type";

    private String key;

    Smp351MetadataValidatorOperationConfig(String key) {
      this.key=key;
    }

    public String toString() {
      return this.key;
    }

}
