/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tingbacke.wearmaps;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Constants used in this sample.
 */
public final class Constants {

    private Constants() {
    }

    public static final String PACKAGE_NAME = "com.google.android.gms.location.Geofence";

    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";

    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 300; // 1 mile, 1.6 km

    /**
     * Map for storing information about HOME and SCHOOL in MALMÖ
     */
    public static final HashMap<String, LatLng> MALMO_LANDMARKS = new HashMap<String, LatLng>();
    static {
        // San Francisco International Airport.
        MALMO_LANDMARKS.put("HOME", new LatLng(55.587151, 12.9797758));

        // SCHOOL
        MALMO_LANDMARKS.put("Kranen K3", new LatLng(55.6156, 12.9857 ));

        MALMO_LANDMARKS.put("Kronprinsen", new LatLng(55.5986, 12.9836));
        MALMO_LANDMARKS.put("Kockum fritid", new LatLng(55.6086, 12.9774));
        MALMO_LANDMARKS.put("Media Evolution City", new LatLng(55.6125, 12.9914));
        MALMO_LANDMARKS.put("Orkanen, Malmö Högskola", new LatLng(55.6108, 12.9949));
        MALMO_LANDMARKS.put("Pildammsparken Tallriken", new LatLng(55.5901, 12.9887));
        //MALMO_LANDMARKS.put("", new LatLng());

    }
}
