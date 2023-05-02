/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asc2kml;

/**
 *
 * @author Q262579
 */
public class GPSCoordinate {
    public GPSCoordinate(double lon, double lat, long rt) {
        longitude = lon;
        latitude = lat;
        reltime = rt;
    }
    public GPSCoordinate(GPSCoordinate gps) {
        this.isValid = gps.isValid;
        this.Longitude = gps.Longitude;
        this.Latitude = gps.Latitude;
        this.longitude = gps.longitude;
        this.latitude = gps.latitude;
        this.reltime = gps.reltime;
    }
    public GPSCoordinate(long Lon, long Lat, long rt) {
        Longitude = Lon;
        Latitude = Lat;
        
        reltime = rt;
        
        isValid = !(Longitude == 2147483647 || Latitude == 2147483647); // 2147483647
        
        longitude = (double)(Longitude * 180.0 / Integer.MAX_VALUE);
        longitude = longitude > 180 ? longitude - 360 : longitude;
        
        latitude = (double)(Latitude * 180.0 / Integer.MAX_VALUE);
        latitude = latitude > 180 ? latitude - 360 : latitude;
    }
    public static double distance(GPSCoordinate gps_1, GPSCoordinate gps_2) {
        if (gps_1.isValid && gps_2.isValid)
            return Math.sqrt(Math.pow((gps_1.latitude - gps_2.latitude), 2) + Math.pow((gps_1.longitude - gps_2.longitude), 2));
        return 0.0;
    }
    public boolean isValid;
    public long reltime;
    public long Longitude;
    public long Latitude;
    public double longitude;
    public double latitude;
}
