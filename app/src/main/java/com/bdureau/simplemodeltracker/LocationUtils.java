package com.bdureau.simplemodeltracker;

import org.osmdroid.util.GeoPoint;



public class LocationUtils {
    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * @returns Distance in Meters
     */
    public static double distanceBetweenCoordinate(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }
    public static double distanceBetweenCoordinate(double lat1, double lat2, double lon1,
                                                   double lon2) {
        return distanceBetweenCoordinate(lat1, lat2, lon1, lon2, 0, 0);
    }
    public static double distanceBetweenCoordinates (GeoPoint [] pathPoints) {
        double distance = 0.0;
        if(pathPoints !=null) {
            for (int i = 0; i < (pathPoints.length - 1); i++) {
                if(pathPoints[i] !=null & pathPoints[i + 1] != null) {
                    double currentDistance = distanceBetweenCoordinate(pathPoints[i].getLatitude(),
                            pathPoints[i + 1].getLatitude(),
                            pathPoints[i].getLongitude(),
                            pathPoints[i + 1].getLongitude());
                    if (currentDistance < 1000.0)
                        distance = distance + currentDistance;
                }

            }
        }
        return distance;
    }
}
