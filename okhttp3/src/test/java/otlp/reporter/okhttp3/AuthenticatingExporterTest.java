/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package otlp.reporter.okhttp3;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.testing.junit5.server.mock.MockWebServerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.junit.jupiter.MockitoExtension;
import otlp.reporter.CompletableResultCode;
import otlp.reporter.HttpExporter;
import otlp.reporter.HttpExporterBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/** Test Authentication in an exporter. */
@ExtendWith(MockitoExtension.class)
class AuthenticatingExporterTest {

  @RegisterExtension static final MockWebServerExtension server = new MockWebServerExtension();

  @Test
  void export() {
    HttpExporter exporter =
        new HttpExporterBuilder(server.httpUri().toASCIIString())
            .setAuthenticator(
                () -> {
                  Map<String, String> headers = new HashMap<>();
                  headers.put("Authorization", "auth");
                  return headers;
                })
            .build(new OkHttpHttpSenderProvider());

    server.enqueue(HttpResponse.of(HttpStatus.UNAUTHORIZED));
    server.enqueue(HttpResponse.of(HttpStatus.OK));

    CompletableResultCode result = exporter.export(new byte[] { 0 });

    assertThat(server.takeRequest().request().headers().get("Authorization")).isNull();
    assertThat(server.takeRequest().request().headers().get("Authorization")).isEqualTo("auth");

    result.join(1, TimeUnit.MINUTES);
    assertThat(result.isSuccess()).isTrue();
  }

  /** Ensure that exporter gives up if a request is always considered UNAUTHORIZED. */
  @Test
  void export_giveup() {
    HttpExporter exporter =
        new HttpExporterBuilder(server.httpUri().toASCIIString())
            .setAuthenticator(
                () -> {
                  server.enqueue(HttpResponse.of(HttpStatus.UNAUTHORIZED));
                  return Collections.emptyMap();
                })
            .build(new OkHttpHttpSenderProvider());
    server.enqueue(HttpResponse.of(HttpStatus.UNAUTHORIZED));
    assertThat(exporter.export(new byte[] { 0 }).join(1, TimeUnit.MINUTES).isSuccess()).isFalse();
  }
}
