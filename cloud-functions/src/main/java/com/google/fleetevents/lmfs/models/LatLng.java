package com.google.fleetevents.lmfs.models;

import java.util.Objects;

public class LatLng {
  private Double latitude;

  private Double longitude;

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof LatLng that) {
      return Objects.equals(that.latitude, this.latitude)
          && Objects.equals(that.longitude, this.longitude);
    }
    return false;
  }

  @Override
  public String toString() {
    return "LatLng{" + "latitute=" + latitude + ", longitude=" + longitude + '}';
  }

  public static final class Builder {
    private Double latitude;
    private Double longitude;

    public Builder setLatitude(Double latitude) {
      this.latitude = latitude;
      return this;
    }

    public Builder setLongitude(Double longitude) {
      this.longitude = longitude;
      return this;
    }

    public LatLng build() {
      LatLng latLng = new LatLng();
      latLng.setLatitude(latitude);
      latLng.setLongitude(longitude);
      return latLng;
    }
  }
}
