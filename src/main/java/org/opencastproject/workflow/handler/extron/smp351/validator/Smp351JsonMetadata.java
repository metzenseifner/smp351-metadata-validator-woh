package org.opencastproject.workflow.handler.extron.smp351.validator;

import java.time.ZonedDateTime;

/**
 *
 * Represents:
 *
 * <pre>
 *    "metadata": {
 *
 * 		"dc:contributor":"",
 * 		"dc:coverage":"",
 * 		"dc:creator":"",
 * 		"dc:date":"2022-06-28T10:15:13Z",
 * 		"dc:description":"",
 * 		"dc:format":"",
 * 		"dc:identifier":"",
 * 		"dc:language":"",
 * 		"dc:publisher":"",
 * 		"dc:relation":"",
 * 		"dc:rights":"",
 * 		"dc:source":"",
 * 		"dc:subject":"",
 * 		"dc:title":"",
 * 		"dc:type":"",
 * 		"dc:course":""
 *    },
 *    </pre>
 */
public interface Smp351JsonMetadata {

    String getContributor();
    String getCoverage();
    String getCreator();
    ZonedDateTime getDate();
    String getDescription();
    String getFormat();
    String getIdentifier();
    String getLanguage();
    String getPublisher();
    String getRights();
    String getSource();
    String getSubject();
    String getTitle();
    String getType();
    String getCourse();

}
