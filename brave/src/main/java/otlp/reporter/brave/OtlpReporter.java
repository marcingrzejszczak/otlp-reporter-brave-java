/*
 * Copyright 2016-2020 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package otlp.reporter.brave;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.util.JsonFormat;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.TracesData;
import otlp.reporter.HttpExporter;
import zipkin2.codec.Encoding;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.Sender;

/**
 * Synchronous reporter of {@link ResourceSpans}. Uses a given {@link Sender}
 * to actually send the spans over the wire. Supports {@link Encoding#JSON}
 * and {@link Encoding#PROTO3} formats.
 *
 * // TODO: Think of an async version, currently {@link AsyncReporter} is unusable
 *
 * @since 2.16
 */
public final class OtlpReporter implements Reporter<TracesData>, Closeable {
  static final Logger logger = Logger.getLogger(OtlpReporter.class.getName());

  private final HttpExporter httpExporter;

  OtlpReporter(HttpExporter httpExporter) {
    this.httpExporter = httpExporter;
  }

  /**
   * Creates a new instance of the {@link OtlpReporter}.
   *
   * @param exporter sender to send spans
   * @return {@link Reporter}
   * @since 2.16
   */
  public static Reporter<TracesData> create(HttpExporter exporter) {
    return new OtlpReporter(exporter);
  }

  @Override
  public void report(TracesData tracesData) {
    try {
      byte[] bytes;
      String asString = JsonFormat.printer()
        .printingEnumsAsInts()
        .print(tracesData);
      // TODO: https://stackoverflow.com/questions/53080136/protobuf-jsonformater-printer-convert-long-to-string-in-json
      // this is ridiculous...
      asString = fixOTLPJsonNotBeingInAccordanceWithProtobufsJsonConvertingMechanisms(asString);
      bytes = asString.getBytes(Charset.defaultCharset());
      // TODO: Currently we have 1 span per ResourceSpans, we could batch them
      httpExporter.export(bytes);
    }
    catch (IOException e) {
      logger.log(Level.WARNING, "Exception occurred while trying to send spans", e);
      throw new RuntimeException(e);
    }
  }

  private String fixOTLPJsonNotBeingInAccordanceWithProtobufsJsonConvertingMechanisms(String asString) {
    // "startTimeUnixNano", "endTimeUnixNano", "timeUnixNano" are not numbers
    DocumentContext documentContext = JsonPath.parse(asString, Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS).build());
    // Protobuf does that
    documentContext.map("$.resourceSpans[*].scopeSpans[*].spans[*].startTimeUnixNano", OtlpReporter::convertToNumber);
    documentContext.map("$.resourceSpans[*].scopeSpans[*].spans[*].endTimeUnixNano", OtlpReporter::convertToNumber);
    documentContext.map("$.resourceSpans[*].scopeSpans[*].spans[*].events[*].timeUnixNano", OtlpReporter::convertToNumber);
    // Ids must be converted back from base64 - https://github.com/open-telemetry/opentelemetry-proto/blob/main/docs/specification.md#json-protobuf-encoding
    documentContext.map("$.resourceSpans[*].scopeSpans[*].spans[*].traceId", OtlpReporter::convertBackFromBase64);
    documentContext.map("$.resourceSpans[*].scopeSpans[*].spans[*].spanId", OtlpReporter::convertBackFromBase64);
    documentContext.map("$.resourceSpans[*].scopeSpans[*].spans[*].parentSpanId", OtlpReporter::convertBackFromBase64);
    return documentContext.jsonString();
  }

  private static Object convertToNumber(Object currentValue, Configuration configuration) {
    if (currentValue instanceof String) {
      return Long.valueOf((String) currentValue);
    }
    return currentValue;
  }

  private static Object convertBackFromBase64(Object currentValue, Configuration configuration) {
    if (currentValue instanceof String) {
      return new String(Base64.decode((String) currentValue));
    }
    return currentValue;
  }

  @Override
  public void close() {
    this.httpExporter.shutdown();
  }
}