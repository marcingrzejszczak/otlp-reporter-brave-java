/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package otlp.reporter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class RetryUtil {

  private static final Set<Integer> RETRYABLE_HTTP_STATUS_CODES =
    Collections.unmodifiableSet(new HashSet<>(Arrays.asList(429, 502, 503, 504)));

  private RetryUtil() {
  }

  /** Returns the retryable HTTP status codes. */
  public static Set<Integer> retryableHttpResponseCodes() {
    return RETRYABLE_HTTP_STATUS_CODES;
  }
}
