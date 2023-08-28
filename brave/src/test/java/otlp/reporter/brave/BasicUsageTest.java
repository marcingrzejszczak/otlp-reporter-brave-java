/*
 * Copyright 2016-2023 The OpenZipkin Authors
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

import java.util.function.Supplier;

import brave.Span;
import brave.Span.Kind;
import brave.Tracer;
import brave.Tracing;
import brave.handler.SpanHandler;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import io.opentelemetry.proto.trace.v1.TracesData;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import org.assertj.core.api.BDDAssertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import otlp.reporter.HttpExporter;
import otlp.reporter.HttpExporterBuilder;
import otlp.reporter.HttpSenderProvider;
import otlp.reporter.okhttp3.OkHttpHttpSenderProvider;
import otlp.reporter.sender.JdkHttpSenderProvider;
import zipkin2.reporter.Reporter;

@Testcontainers
class BasicUsageTest {

  @Container
  static JaegerAllInOne jaegerAllInOne = new JaegerAllInOne();

  HttpExporter httpExporter;

  Tracing tracing;

  @ParameterizedTest
  @EnumSource(HttpProvider.class)
  void shouldSendSpansToOtlpEndpoint(HttpProvider provider) throws InterruptedException {
    // Setup
    httpExporter = new HttpExporterBuilder("http://localhost:" + jaegerAllInOne.getHttpOtlpPort() + "/v1/traces").build(provider.get());
    Reporter<TracesData> otlpReporter = OtlpReporter.create(httpExporter);
    SpanHandler spanHandler = OtlpSpanHandler
      .create(otlpReporter);
    ThreadLocalCurrentTraceContext braveCurrentTraceContext = ThreadLocalCurrentTraceContext.newBuilder()
      .build();
    tracing = Tracing.newBuilder()
      .currentTraceContext(braveCurrentTraceContext)
      .supportsJoin(false)
      .traceId128Bit(true)
      .sampler(Sampler.ALWAYS_SAMPLE)
      .addSpanHandler(spanHandler)
      .localServiceName("my-service")
      .build();
    Tracer braveTracer = tracing.tracer();

    // Given
    Span started = braveTracer.nextSpan().name("foo")
      .tag("foo tag", "foo value")
      .kind(Kind.CONSUMER)
      .error(new RuntimeException("BOOOOOM!"))
      .remoteServiceName("remote service")
      .start();
    String traceId = started.context().traceIdString();
    System.out.println("Trace Id <" + traceId + ">");
    started.remoteIpAndPort("http://localhost", 123456);
    Thread.sleep(50);
    started.annotate("boom!");
    Thread.sleep(50);

    // When
    started.finish();

    // Then
    Awaitility.await().untilAsserted(() -> {
      OkHttpClient client = new Builder()
        .build();
      Request request = new Request.Builder().url("http://localhost:" + jaegerAllInOne.getQueryPort() + "/api/traces/" + traceId).build();
      try (Response response = client.newCall(request).execute()) {
        BDDAssertions.then(response.isSuccessful()).isTrue();
      }
    });
  }

  enum HttpProvider implements Supplier<HttpSenderProvider> {

    JDK {
      @Override
      public HttpSenderProvider get() {
        return new JdkHttpSenderProvider();
      }
    },

    OK_HTTP {
      @Override
      public HttpSenderProvider get() {
        return new OkHttpHttpSenderProvider();
      }
    }
  }

  @AfterEach
  void shutdown() {
    if (tracing != null) {
      tracing.close();
    }
    if (httpExporter != null) {
      httpExporter.shutdown();
    }
  }
}
