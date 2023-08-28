# otlp-reporter-brave-java

[![Gitter chat](http://img.shields.io/badge/gitter-join%20chat%20%E2%86%92-brightgreen.svg)](https://gitter.im/openzipkin/zipkin)
[![Build Status](https://github.com/openzipkin/zipkin-reporter-java/workflows/test/badge.svg)](https://github.com/openzipkin/zipkin/actions?query=workflow%3Atest)
[![Maven Central](https://img.shields.io/maven-central/v/io.zipkin.reporter2/zipkin-reporter.svg)](https://search.maven.org/search?q=g:io.zipkin.reporter2%20AND%20a:otlp-reporter)

This project reuses concepts from [OpenTelemetry Java](https://github.com/open-telemetry/opentelemetry-java/) for Http span exporting and combines it with Brave's Handler and Reporter mechanisms. When a span is finished the `MutableSpan` from Brave is being converted to a OTLP format and sent over the wire (either using JDK 11 or OK HTTP).

## Artifacts
All artifacts publish to the group ID "io.zipkin.zipkin.reporter2". We use a common
release version for all components.

### Library Releases
Releases are uploaded to [Sonatype](https://oss.sonatype.org/content/repositories/releases) which
synchronizes with [Maven Central](http://search.maven.org/)

### Library Snapshots
Snapshots are uploaded to [Sonatype](https://oss.sonatype.org/content/repositories/snapshots) after
commits to master.

### Version alignments
When using multiple reporter components, you'll want to align versions
in one place. This allows you to more safely upgrade, with less worry
about conflicts.

You can use our Maven instrumentation BOM (Bill of Materials) for this:

Ex. in your dependencies section, import the BOM like this:
```xml
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>io.zipkin.reporter2</groupId>
        <artifactId>otlp-reporter-brave-bom</artifactId>
        <version>${otlp-reporter.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
```

Now, you can leave off the version when choosing any supported
instrumentation. Also any indirect use will have versions aligned:
```xml
<dependency>
  <groupId>io.zipkin.reporter2</groupId>
  <artifactId>otlp-sender-okhttp3</artifactId>
</dependency>
```

With this in place, you can use the property `zipkin-reporter.version` to override dependency
versions coherently. This is most commonly to test a new feature or fix.

Note: If you override a version, always double check that your version
is valid (equal to or later) than what you are updating. This will avoid
class conflicts.
