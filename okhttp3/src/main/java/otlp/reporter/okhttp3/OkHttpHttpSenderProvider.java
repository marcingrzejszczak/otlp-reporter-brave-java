/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package otlp.reporter.okhttp3;

import java.util.Map;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import otlp.reporter.Authenticator;
import otlp.reporter.HttpSender;
import otlp.reporter.HttpSenderProvider;
import otlp.reporter.Nullable;
import otlp.reporter.RetryPolicy;


/**
 * {@link HttpSender} SPI implementation for {@link OkHttpHttpSender}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OkHttpHttpSenderProvider implements HttpSenderProvider {

  @Override
  public HttpSender createSender(
      String endpoint,
      boolean compressionEnabled,
      String contentType,
      long timeoutNanos,
      Supplier<Map<String, String>> headerSupplier,
      @Nullable Authenticator authenticator,
      @Nullable RetryPolicy retryPolicy,
      @Nullable SSLContext sslContext,
      @Nullable X509TrustManager trustManager) {
    return new OkHttpHttpSender(
        endpoint,
        compressionEnabled,
        contentType,
        timeoutNanos,
        headerSupplier,
        authenticator,
        retryPolicy,
        sslContext,
        trustManager);
  }
}
