# smp351-metadata-validator-woh

A workflow operation handler that operates on the proprietary Extron SMP 351 file sent to Opencast.

The following Smp351 fields are supported:

- dc:contributor
- dc:coverage
- dc:creator
- dc:date
- dc:description
- dc:format
- dc:identifier
- dc:language
- dc:publisher
- dc:rights
- dc:source
- dc:subject
- dc:title
- dc:type
- dc:course

## Usage in Workflow

Here is a minimal workflow that demonstrates how this handler can be used:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definition xmlns="http://workflow.opencastproject.org">

    <id>smp-ingest-validation-example</id>
    <title>Ingest with Validation</title>
    <tags/>
    <description/>

    <operations>
        <operation
            id="smp351metadatavalidator"
            description="Validate the proprietary SMP 351 catalog's metadata.">
            <configurations>
                <!-- validate 7 of 15 fields -->
                <configuration key="dc:creator">.+</configuration> <!-- match 1 or more chars -->
                <configuration key="dc:date">.+</configuration>
                <configuration key="dc:identifier">.+</configuration>
                <configuration key="dc:relation">[0-9]+</configuration> <!-- match 1 or more numbers -->
                <configuration key="dc:source">.+</configuration>
                <configuration key="dc:subject">.+</configuration>
                <configuration key="dc:title">.+</configuration>
            </configurations>
        </operation>
    </operations>

</definition>
```