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
package otlp.reporter.sender;

import java.util.Map;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import otlp.reporter.Authenticator;
import otlp.reporter.HttpSender;
import otlp.reporter.HttpSenderProvider;
import otlp.reporter.RetryPolicy;


/**
 * {@link HttpSender} SPI implementation for {@link JdkHttpSender}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class JdkHttpSenderProvider implements HttpSenderProvider {

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
    return new JdkHttpSender(
      endpoint,
      compressionEnabled,
      contentType,
      timeoutNanos,
      headerSupplier,
      retryPolicy,
      sslContext);
  }
}
