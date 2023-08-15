package com.google.fleetevents.beam.config;

import java.io.Serializable;

public class DataflowJobConfig implements Serializable {
  private String datastoreProjectId;
  private Integer windowSize;
  private Integer gapSize;

  public DataflowJobConfig() {}

  public String getDatastoreProjectId() {
    return datastoreProjectId;
  }

  public Integer getWindowSize() {
    return windowSize;
  }

  public Integer getGapSize() {
    return gapSize;
  }

  public static final class Builder {
    private String datastoreProjectId;
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
      return dataflowJobConfig;
    }
  }
}
