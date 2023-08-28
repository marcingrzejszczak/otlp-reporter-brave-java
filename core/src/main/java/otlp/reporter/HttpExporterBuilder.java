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
/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package otlp.reporter;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;


/**
 * A builder for {@link HttpExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@SuppressWarnings("checkstyle:JavadocMethod")
public final class HttpExporterBuilder {
  public static final long DEFAULT_TIMEOUT_SECS = 10;

  private static final Logger LOGGER = Logger.getLogger(HttpExporterBuilder.class.getName());

  private String endpoint;

  private long timeoutNanos = TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECS);

  private boolean compressionEnabled = false;

  private boolean exportAsJson = true;

  @Nullable
  private Map<String, String> headers;

  private TlsConfigHelper tlsConfigHelper = new TlsConfigHelper();

  @Nullable
  private RetryPolicy retryPolicy;

  @Nullable
  private Authenticator authenticator;

  public HttpExporterBuilder(String defaultEndpoint) {
    endpoint = defaultEndpoint;
  }

  public HttpExporterBuilder setTimeout(long timeout, TimeUnit unit) {
    timeoutNanos = unit.toNanos(timeout);
    return this;
  }

  public HttpExporterBuilder setTimeout(Duration timeout) {
    return setTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  public HttpExporterBuilder setEndpoint(String endpoint) {
    URI uri = ExporterBuilderUtil.validateEndpoint(endpoint);
    this.endpoint = uri.toString();
    return this;
  }

  public HttpExporterBuilder setCompression(String compressionMethod) {
    this.compressionEnabled = compressionMethod.equals("gzip");
    return this;
  }

  public HttpExporterBuilder addHeader(String key, String value) {
    if (headers == null) {
      headers = new HashMap<>();
    }
    headers.put(key, value);
    return this;
  }

  public HttpExporterBuilder setAuthenticator(Authenticator authenticator) {
    this.authenticator = authenticator;
    return this;
  }

  public HttpExporterBuilder setTrustManagerFromCerts(byte[] trustedCertificatesPem) {
    tlsConfigHelper.setTrustManagerFromCerts(trustedCertificatesPem);
    return this;
  }

  public HttpExporterBuilder setKeyManagerFromCerts(
    byte[] privateKeyPem, byte[] certificatePem) {
    tlsConfigHelper.setKeyManagerFromCerts(privateKeyPem, certificatePem);
    return this;
  }

  public HttpExporterBuilder setSslContext(
    SSLContext sslContext, X509TrustManager trustManager) {
    tlsConfigHelper.setSslContext(sslContext, trustManager);
    return this;
  }

  public HttpExporterBuilder setRetryPolicy(RetryPolicy retryPolicy) {
    this.retryPolicy = retryPolicy;
    return this;
  }

//  public HttpExporterBuilder exportAsJson() {
//    this.exportAsJson = true;
//    return this;
//  }

  @SuppressWarnings("BuilderReturnThis")
  public HttpExporterBuilder copy() {
    HttpExporterBuilder copy = new HttpExporterBuilder(endpoint);
    copy.endpoint = endpoint;
    copy.timeoutNanos = timeoutNanos;
    copy.exportAsJson = exportAsJson;
    copy.compressionEnabled = compressionEnabled;
    if (headers != null) {
      copy.headers = new HashMap<>(headers);
    }
    copy.tlsConfigHelper = tlsConfigHelper.copy();
    if (retryPolicy != null) {
      copy.retryPolicy = retryPolicy.toBuilder().build();
    }
    copy.authenticator = authenticator;
    return copy;
  }

  public HttpExporter build(HttpSenderProvider httpSenderProvider) {
    Map<String, String> headers = this.headers == null ? Collections.emptyMap() : this.headers;
    Supplier<Map<String, String>> headerSupplier = () -> headers;

    HttpSender httpSender =
      httpSenderProvider.createSender(
        endpoint,
        compressionEnabled,
        exportAsJson ? "application/json" : "application/x-protobuf",
        timeoutNanos,
        headerSupplier,
        authenticator,
        retryPolicy,
        tlsConfigHelper.getSslContext(),
        tlsConfigHelper.getTrustManager());
    LOGGER.log(Level.FINE, "Using HttpSender: " + httpSender.getClass().getName());

    return new HttpExporter(httpSender);
  }

  public String toString(boolean includePrefixAndSuffix) {
    StringJoiner joiner =
      includePrefixAndSuffix
        ? new StringJoiner(", ", "HttpExporterBuilder{", "}")
        : new StringJoiner(", ");
    joiner.add("endpoint=" + endpoint);
    joiner.add("timeoutNanos=" + timeoutNanos);
    joiner.add("compressionEnabled=" + compressionEnabled);
    joiner.add("exportAsJson=" + exportAsJson);
    if (headers != null) {
      StringJoiner headersJoiner = new StringJoiner(", ", "Headers{", "}");
      headers.forEach((key, value) -> headersJoiner.add(key + "=OBFUSCATED"));
      joiner.add("headers=" + headersJoiner);
    }
    if (retryPolicy != null) {
      joiner.add("retryPolicy=" + retryPolicy);
    }
    // Note: omit tlsConfigHelper because we can't log the configuration in any readable way
    // Note: omit meterProviderSupplier because we can't log the configuration in any readable way
    // Note: omit authenticator because we can't log the configuration in any readable way
    return joiner.toString();
  }

  @Override
  public String toString() {
    return toString(true);
  }
}
