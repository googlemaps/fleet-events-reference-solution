/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package samples;

import com.google.protobuf.Timestamp;
import java.util.Map;

public class FleetEvents {

  /** represents ODRD/LMFS specific metadata extracted from Cloud Logging's LogEntry */
  public record FleetEventMessage(
      String event_name,
      Usecase usecase,
      Map<String, String> attributes,
      Timestamp eventtime,
      Timestamp logtime) {}

  public enum Usecase {
    ODRD,
    LMFS
  }

  public enum EVENTS_ODRD {
    create_vehicle,
    get_vehicle,
    update_vehicle,
  }

  public enum EVENTS_LMFS {
    create_task,
    get_task,
    list_task,
    update_task,
    search_task,
    batchCreate_task,
    create_delivery_vehicle,
    get_delivery_vehicle,
    list_delivery_vehicles,
    update_delivery_vehicle,
    search_delivery_vehicle
  }

  public enum LABELS {
    // ODRD
    vehicle_id,
    task_id,
    // LMFS
    delivery_vehicle_id,
    trip_id,
  }
}
