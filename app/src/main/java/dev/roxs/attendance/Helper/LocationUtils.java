package dev.roxs.attendance.Helper;

import android.content.Context;
import android.location.LocationManager;

public class LocationUtils {

        public static boolean isLocationEnabled(Context context) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                return gpsEnabled || networkEnabled;
            }
            return false;
        }
}
