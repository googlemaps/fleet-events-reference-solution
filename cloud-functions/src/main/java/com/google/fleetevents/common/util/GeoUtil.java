package com.google.fleetevents.common.util;

import com.google.cloud.firestore.GeoPoint;
import com.google.type.LatLng;

/** Utilities for dealing with location data and formats. */
public class GeoUtil {

  public static GeoPoint latLngToGeoPoint(LatLng latLng) {
    if (latLng == null) {
      return null;
    }
    return new GeoPoint(latLng.getLatitude(), latLng.getLongitude());
  }
}
