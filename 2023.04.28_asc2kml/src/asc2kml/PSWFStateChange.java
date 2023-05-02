/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asc2kml;

/**
 *
 * @author Q262579
 */
public class PSWFStateChange {
    public PSWFStateChange(PSWFStateChange psc) {
        this.gpsCoord = new GPSCoordinate(psc.gpsCoord);
        this.oldPSWF = psc.oldPSWF;
        this.newPSWF = psc.newPSWF;
        this.reltime = psc.reltime;
    }
    public PSWFStateChange(GPSCoordinate gps, PSWF ol, PSWF ne, long rt) {
        gpsCoord = new GPSCoordinate(gps);
        oldPSWF = ol;
        newPSWF = ne;
        reltime = rt;
    }
    public long reltime;
    public GPSCoordinate gpsCoord;
    public PSWF oldPSWF;
    public PSWF newPSWF;                
}
