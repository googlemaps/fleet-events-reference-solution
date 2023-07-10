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

package com.google.fleetevents.util;

/** Returns task ids from formal name. */
public class NameFormatter {

  // Get Id from Name, which is formatted as "/providers/{provider}/task/{taskId}
  public static String getIdFromName(String name) {
    String[] splitString = name.split("/");
    if (splitString.length != 4) {
      throw new IllegalArgumentException("unexpected name " + name);
    } else {
      return splitString[3];
    }
  }
}
