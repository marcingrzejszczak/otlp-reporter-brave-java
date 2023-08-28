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

import java.time.Duration;

/**
 * Configuration for exporter exponential retry policy.
 *
 * @since 1.28.0
 */
public abstract class RetryPolicy {

  private static final int DEFAULT_MAX_ATTEMPTS = 5;

  @SuppressWarnings("StronglyTypeTime")
  private static final int DEFAULT_INITIAL_BACKOFF_SECONDS = 1;

  @SuppressWarnings("StronglyTypeTime")
  private static final int DEFAULT_MAX_BACKOFF_SECONDS = 5;

  private static final double DEFAULT_BACKOFF_MULTIPLIER = 1.5;

  private static final RetryPolicy DEFAULT = RetryPolicy.builder().build();

  RetryPolicy() {
  }

  /** Return the default {@link RetryPolicy}. */
  public static RetryPolicy getDefault() {
    return DEFAULT;
  }

  /** Returns a new {@link RetryPolicyBuilder} to construct a {@link RetryPolicy}. */
  public static RetryPolicyBuilder builder() {
    return new AutoValue_RetryPolicy.Builder()
      .setMaxAttempts(DEFAULT_MAX_ATTEMPTS)
      .setInitialBackoff(Duration.ofSeconds(DEFAULT_INITIAL_BACKOFF_SECONDS))
      .setMaxBackoff(Duration.ofSeconds(DEFAULT_MAX_BACKOFF_SECONDS))
      .setBackoffMultiplier(DEFAULT_BACKOFF_MULTIPLIER);
  }

  /**
   * Returns a {@link RetryPolicyBuilder} reflecting configuration values for this {@link
   * RetryPolicy}.
   *
   * @since 1.29.0
   */
  public abstract RetryPolicyBuilder toBuilder();

  /** Returns the max number of attempts, including the original request. */
  public abstract int getMaxAttempts();

  /** Returns the initial backoff. */
  public abstract Duration getInitialBackoff();

  /** Returns the max backoff. */
  public abstract Duration getMaxBackoff();

  /** Returns the backoff multiplier. */
  public abstract double getBackoffMultiplier();

  /** Builder for {@link RetryPolicy}. */
  public abstract static class RetryPolicyBuilder {

    RetryPolicyBuilder() {
    }

    /**
     * Set the maximum number of attempts, including the original request. Must be greater than 1
     * and less than 6. Defaults to {@value DEFAULT_MAX_ATTEMPTS}.
     */
    public abstract RetryPolicyBuilder setMaxAttempts(int maxAttempts);

    /**
     * Set the initial backoff. Must be greater than 0. Defaults to {@value
     * DEFAULT_INITIAL_BACKOFF_SECONDS} seconds.
     */
    public abstract RetryPolicyBuilder setInitialBackoff(Duration initialBackoff);

    /**
     * Set the maximum backoff. Must be greater than 0. Defaults to {@value
     * DEFAULT_MAX_BACKOFF_SECONDS} seconds.
     */
    public abstract RetryPolicyBuilder setMaxBackoff(Duration maxBackoff);

    /**
     * Set the backoff multiplier. Must be greater than 0.0. Defaults to {@value
     * DEFAULT_BACKOFF_MULTIPLIER}.
     */
    public abstract RetryPolicyBuilder setBackoffMultiplier(double backoffMultiplier);

    abstract RetryPolicy autoBuild();

    /** Build and return a {@link RetryPolicy} with the values of this builder. */
    public RetryPolicy build() {
      RetryPolicy retryPolicy = autoBuild();
      checkArgument(
        retryPolicy.getMaxAttempts() > 1 && retryPolicy.getMaxAttempts() < 6,
        "maxAttempts must be greater than 1 and less than 6");
      checkArgument(
        retryPolicy.getInitialBackoff().toNanos() > 0, "initialBackoff must be greater than 0");
      checkArgument(retryPolicy.getMaxBackoff().toNanos() > 0, "maxBackoff must be greater than 0");
      checkArgument(
        retryPolicy.getBackoffMultiplier() > 0, "backoffMultiplier must be greater than 0");

      return retryPolicy;
    }

    private void checkArgument(boolean b, String s) {
      if (!b) {
        throw new IllegalArgumentException(s);
      }
    }
  }
}
