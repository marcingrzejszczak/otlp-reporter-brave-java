/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package otlp.reporter.okhttp3;

import org.junit.jupiter.api.Test;
import otlp.reporter.HttpExporter;
import otlp.reporter.HttpExporterBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class HttpExporterBuilderTest {

  private final HttpExporterBuilder builder =
      new HttpExporterBuilder("http://localhost:4318/v1/traces");

  @Test
  void compressionDefault() {
    HttpExporter exporter = builder.build(new OkHttpHttpSenderProvider());
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              HttpExporter.class,
              otlp ->
                  assertThat(otlp)
                      .extracting("httpSender")
                      .isInstanceOf(OkHttpHttpSender.class)
                      .extracting("compressionEnabled")
                      .isEqualTo(false));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionNone() {
    HttpExporter exporter = builder.setCompression("none").build(new OkHttpHttpSenderProvider());
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              HttpExporter.class,
              otlp ->
                  assertThat(otlp)
                      .extracting("httpSender")
                      .isInstanceOf(OkHttpHttpSender.class)
                      .extracting("compressionEnabled")
                      .isEqualTo(false));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionGzip() {
    HttpExporter exporter = builder.setCompression("gzip").build(new OkHttpHttpSenderProvider());
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              HttpExporter.class,
              otlp ->
                  assertThat(otlp)
                      .extracting("httpSender")
                      .isInstanceOf(OkHttpHttpSender.class)
                      .extracting("compressionEnabled")
                      .isEqualTo(true));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionEnabledAndDisabled() {
    HttpExporter exporter =
        builder.setCompression("gzip").setCompression("none").build(new OkHttpHttpSenderProvider());
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              HttpExporter.class,
              otlp ->
                  assertThat(otlp)
                      .extracting("httpSender")
                      .isInstanceOf(OkHttpHttpSender.class)
                      .extracting("compressionEnabled")
                      .isEqualTo(false));
    } finally {
      exporter.shutdown();
    }
  }
}
