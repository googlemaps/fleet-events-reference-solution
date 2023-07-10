/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.fleetevents.models;

import java.util.Objects;

/**
 * Represents a pair of changes from old to new values.
 *
 * @param <T> The type of the change.
 */
public class Change<T> {

  public final T oldValue;
  public final T newValue;

  public Change(T oldValue, T newValue) {
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  public String toString() {
    return String.format("Change {\n\told: %s\n\tnew: %s\n}", oldValue, newValue);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Change that) {
      return Objects.equals(oldValue, that.oldValue) && Objects.equals(newValue, that.newValue);
    }
    return false;
  }
}
