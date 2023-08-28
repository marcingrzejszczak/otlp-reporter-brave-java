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

import java.lang.reflect.Field;
import java.util.Map;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 *
 * <p>Allow users of OTLP-OkHttp exporters to add support for authentication.
 */
public interface Authenticator {

  /**
   * Method called by the exporter to get headers to be used on a request that requires
   * authentication.
   *
   * @return Headers to add to the request
   */
  Map<String, String> getHeaders();

  /**
   * Reflectively access {@link HttpExporterBuilder} instance in
   * field called "delegate" of the instance, and set the {@link Authenticator}.
   *
   * @param builder export builder to modify
   * @param authenticator authenticator to set on builder
   * @throws IllegalArgumentException if the instance does not contain a field called "delegate" of
   *     a supported type.
   */
  static void setAuthenticatorOnDelegate(Object builder, Authenticator authenticator) {
    try {
      Field field = builder.getClass().getDeclaredField("delegate");
      field.setAccessible(true);
      Object value = field.get(builder);
      if (value instanceof HttpExporterBuilder) {
        ((HttpExporterBuilder) value).setAuthenticator(authenticator);
      }
      else {
        throw new IllegalArgumentException(
          "Delegate field is not of supported type.");
      }
    }
    catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalArgumentException("Unable to access delegate reflectively.", e);
    }
  }
}
