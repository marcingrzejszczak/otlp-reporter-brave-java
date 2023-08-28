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

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * An exporter for http/protobuf or http/json using a signal-specific Marshaler.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@SuppressWarnings("checkstyle:JavadocMethod")
public final class HttpExporter {

  private static final Logger logger = Logger.getLogger(HttpExporter.class.getName());

  private final AtomicBoolean isShutdown = new AtomicBoolean();

  private final HttpSender httpSender;

  private final boolean exportAsJson = true;

  public HttpExporter(HttpSender httpSender) {
    this.httpSender = httpSender;
  }

  public CompletableResultCode export(byte[] exportRequest) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    CompletableResultCode result = new CompletableResultCode();

    Consumer<OutputStream> marshaler =
      os -> {
        try {
          os.write(exportRequest);
        }
        catch (IOException e) {
          throw new IllegalStateException(e);
        }
      };

    httpSender.send(
      marshaler,
      exportRequest.length,
      httpResponse -> {
        int statusCode = httpResponse.statusCode();

        if (statusCode >= 200 && statusCode < 300) {
          result.succeed();
          return;
        }

        byte[] body;
        try {
          body = httpResponse.responseBody();
        }
        catch (IOException ex) {
          throw new IllegalStateException(ex);
        }

        String status = extractErrorStatus(httpResponse.statusMessage(), body);

        logger.log(
          Level.WARNING,
          "Failed to export "
            + ". Server responded with HTTP status code "
            + statusCode
            + ". Error message: "
            + status);
        result.fail();
      },
      e -> {
        logger.log(
          Level.SEVERE,
          "Failed to export. The request could not be executed. Full error message: "
            + e.getMessage(),
          e);
        result.fail();
      });

    return result;
  }

  public CompletableResultCode shutdown() {
    if (!isShutdown.compareAndSet(false, true)) {
      logger.log(Level.INFO, "Calling shutdown() multiple times.");
      return CompletableResultCode.ofSuccess();
    }
    return httpSender.shutdown();
  }

  private static String extractErrorStatus(String statusMessage, @Nullable byte[] responseBody) {
    if (responseBody == null) {
      return "Response body missing, HTTP status message: " + statusMessage;
    }
    return "Unable to parse response body, HTTP status message: " + statusMessage;
  }
}
