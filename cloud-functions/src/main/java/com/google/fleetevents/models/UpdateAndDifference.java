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

import java.util.Map;

/**
 * Pair class to hold an updated object (task or vehicle) and the map of changes that were
 * processed.
 *
 * @param <T> The type of the object being updated.
 */
public class UpdateAndDifference<T> {

  public T updated;
  public Map<String, Change> differences;

  public UpdateAndDifference(T updated, Map<String, Change> differences) {
    this.updated = updated;
    this.differences = differences;
  }
}
