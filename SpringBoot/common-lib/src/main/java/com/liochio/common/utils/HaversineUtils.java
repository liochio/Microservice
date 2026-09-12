package com.liochio.common.utils;

import java.time.Duration;
import java.time.Instant;

/**
 * ==============================================================================
 * Thuật Toán Phát Hiện Vị Trí Đăng Nhập Bất Thường (Impossible Travel Detection)
 * ==============================================================================
 */
public final class HaversineUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;
    public static final double DEFAULT_MAX_VELOCITY_KMH = 800.0;

    private HaversineUtils() {}

    public static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public static double calculateVelocityKmH(double lat1, double lon1, Instant time1,
                                             double lat2, double lon2, Instant time2) {
        if (time1 == null || time2 == null) return 0.0;
        long diffSeconds = Math.abs(Duration.between(time1, time2).getSeconds());
        if (diffSeconds < 1) diffSeconds = 1;

        double distanceKm = calculateDistanceKm(lat1, lon1, lat2, lon2);
        double hours = (double) diffSeconds / 3600.0;
        return distanceKm / hours;
    }

    public static boolean isImpossibleTravel(double lat1, double lon1, Instant time1,
                                            double lat2, double lon2, Instant time2,
                                            double thresholdKmH) {
        if (lat1 == 0.0 && lon1 == 0.0) return false;
        if (lat2 == 0.0 && lon2 == 0.0) return false;
        if (time1 == null || time2 == null) return false;

        double velocity = calculateVelocityKmH(lat1, lon1, time1, lat2, lon2, time2);
        return velocity > thresholdKmH;
    }
}
