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

package com.google.fleetevents.common.models;

import java.util.Objects;

/**
 * Pair class for serializing in Firestore.
 *
 * @param <K> First value
 * @param <V> Second value
 */
public class Pair<K, V> {

  private final K key;

  private final V value;

  public Pair(K key, V value) {
    this.key = key;
    this.value = value;
  }

  public K getKey() {
    return key;
  }

  public V getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Pair<?, ?> pair)) {
      return false;
    }
    return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
  }

  @Override
  public String toString() {
    return "Pair{" + "key=" + key + ", value=" + value + '}';
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }
}
