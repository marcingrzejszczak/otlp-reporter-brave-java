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

final class AutoValue_RetryPolicy extends RetryPolicy {

  private final int maxAttempts;

  private final Duration initialBackoff;

  private final Duration maxBackoff;

  private final double backoffMultiplier;

  private AutoValue_RetryPolicy(
    int maxAttempts,
    Duration initialBackoff,
    Duration maxBackoff,
    double backoffMultiplier) {
    this.maxAttempts = maxAttempts;
    this.initialBackoff = initialBackoff;
    this.maxBackoff = maxBackoff;
    this.backoffMultiplier = backoffMultiplier;
  }

  @Override
  public int getMaxAttempts() {
    return maxAttempts;
  }

  @Override
  public Duration getInitialBackoff() {
    return initialBackoff;
  }

  @Override
  public Duration getMaxBackoff() {
    return maxBackoff;
  }

  @Override
  public double getBackoffMultiplier() {
    return backoffMultiplier;
  }

  @Override
  public String toString() {
    return "RetryPolicy{"
      + "maxAttempts=" + maxAttempts + ", "
      + "initialBackoff=" + initialBackoff + ", "
      + "maxBackoff=" + maxBackoff + ", "
      + "backoffMultiplier=" + backoffMultiplier
      + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof RetryPolicy) {
      RetryPolicy that = (RetryPolicy) o;
      return this.maxAttempts == that.getMaxAttempts()
        && this.initialBackoff.equals(that.getInitialBackoff())
        && this.maxBackoff.equals(that.getMaxBackoff())
        && Double.doubleToLongBits(this.backoffMultiplier) == Double.doubleToLongBits(that.getBackoffMultiplier());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= maxAttempts;
    h$ *= 1000003;
    h$ ^= initialBackoff.hashCode();
    h$ *= 1000003;
    h$ ^= maxBackoff.hashCode();
    h$ *= 1000003;
    h$ ^= (int) ((Double.doubleToLongBits(backoffMultiplier) >>> 32) ^ Double.doubleToLongBits(backoffMultiplier));
    return h$;
  }

  @Override
  public RetryPolicyBuilder toBuilder() {
    return new Builder(this);
  }

  static final class Builder extends RetryPolicyBuilder {
    private int maxAttempts;

    private Duration initialBackoff;

    private Duration maxBackoff;

    private double backoffMultiplier;

    private byte set$0;

    Builder() {
    }

    private Builder(RetryPolicy source) {
      this.maxAttempts = source.getMaxAttempts();
      this.initialBackoff = source.getInitialBackoff();
      this.maxBackoff = source.getMaxBackoff();
      this.backoffMultiplier = source.getBackoffMultiplier();
      set$0 = (byte) 3;
    }

    @Override
    public RetryPolicyBuilder setMaxAttempts(int maxAttempts) {
      this.maxAttempts = maxAttempts;
      set$0 |= (byte) 1;
      return this;
    }

    @Override
    public RetryPolicyBuilder setInitialBackoff(Duration initialBackoff) {
      if (initialBackoff == null) {
        throw new NullPointerException("Null initialBackoff");
      }
      this.initialBackoff = initialBackoff;
      return this;
    }

    @Override
    public RetryPolicyBuilder setMaxBackoff(Duration maxBackoff) {
      if (maxBackoff == null) {
        throw new NullPointerException("Null maxBackoff");
      }
      this.maxBackoff = maxBackoff;
      return this;
    }

    @Override
    public RetryPolicyBuilder setBackoffMultiplier(double backoffMultiplier) {
      this.backoffMultiplier = backoffMultiplier;
      set$0 |= (byte) 2;
      return this;
    }

    @Override
    RetryPolicy autoBuild() {
      if (set$0 != 3
        || this.initialBackoff == null
        || this.maxBackoff == null) {
        StringBuilder missing = new StringBuilder();
        if ((set$0 & 1) == 0) {
          missing.append(" maxAttempts");
        }
        if (this.initialBackoff == null) {
          missing.append(" initialBackoff");
        }
        if (this.maxBackoff == null) {
          missing.append(" maxBackoff");
        }
        if ((set$0 & 2) == 0) {
          missing.append(" backoffMultiplier");
        }
        throw new IllegalStateException("Missing required properties:" + missing);
      }
      return new AutoValue_RetryPolicy(
        this.maxAttempts,
        this.initialBackoff,
        this.maxBackoff,
        this.backoffMultiplier);
    }
  }

}
