// Copyright 2023 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.fleetevents.beam.config;

import java.io.Serializable;

public class DataflowJobConfig implements Serializable {
  private String datastoreProjectId;
  private String databaseId;
  private Integer windowSize;
  private Integer gapSize;

  public DataflowJobConfig() {}

  public String getDatastoreProjectId() {
    return datastoreProjectId;
  }

  public String getDatabaseId() {
    return databaseId;
  }

  public Integer getWindowSize() {
    return windowSize;
  }

  public Integer getGapSize() {
    return gapSize;
  }

  public static final class Builder {
    private String datastoreProjectId;
    private String databaseId;
    private Integer windowSize;
    private Integer gapSize;

    private Builder() {}

    public static Builder newBuilder() {
      return new Builder();
    }

    public Builder setDatastoreProjectId(String datastoreProjectId) {
      this.datastoreProjectId = datastoreProjectId;
      return this;
    }

    public Builder setDatabaseId(String databaseId) {
      this.databaseId = databaseId;
      return this;
    }

    public Builder setWindowSize(Integer windowSize) {
      this.windowSize = windowSize;
      return this;
    }

    public Builder setGapSize(Integer gapSize) {
      this.gapSize = gapSize;
      return this;
    }

    public DataflowJobConfig build() {
      DataflowJobConfig dataflowJobConfig = new DataflowJobConfig();
      dataflowJobConfig.windowSize = this.windowSize;
      dataflowJobConfig.gapSize = this.gapSize;
      dataflowJobConfig.datastoreProjectId = this.datastoreProjectId;
      dataflowJobConfig.databaseId = this.databaseId;
      return dataflowJobConfig;
    }
  }
}
