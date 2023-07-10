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

import java.sql.Timestamp;
import java.time.Duration;

/** Utility for time related conversion from protobufs as well as miscellaneous time operations. */
public class TimeUtil {

  public static final long MILLIS_TO_NANOS = 1000000;
  public static final long SECONDS_TO_MILLIS = 1000;

  public static final long ONE_HOUR_IN_SECONDS = 60 * 60;

  // returns epoch time in millis
  public static Long protobufToLong(com.google.protobuf.Timestamp timestamp) {
    return timestamp.getSeconds() * SECONDS_TO_MILLIS + timestamp.getNanos() / MILLIS_TO_NANOS;
  }

  // returns epoch time in millis
  public static Long protobufToLong(com.google.protobuf.Duration duration) {
    return duration.getSeconds() * SECONDS_TO_MILLIS + duration.getNanos() / MILLIS_TO_NANOS;
  }

  public static Duration addDuration(Duration d1, Duration d2) {
    return d1.plus(d2);
  }

  public static Timestamp incrementTimestamp(Timestamp t, Duration d) {
    return new Timestamp(t.getTime() + d.toMillis());
  }

  public static long timestampDifferenceMillis(Timestamp t1, Timestamp t2) {
    // getTime is in millis
    return t1.getTime() - t2.getTime();
  }

  public static com.google.cloud.Timestamp offsetFromNow(long offsetInSeconds) {
    var now = com.google.cloud.Timestamp.now();
    return com.google.cloud.Timestamp.ofTimeSecondsAndNanos(
        now.getSeconds() + offsetInSeconds, now.getNanos());
  }
}
