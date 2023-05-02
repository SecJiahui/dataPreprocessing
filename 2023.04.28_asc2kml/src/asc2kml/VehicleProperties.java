/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asc2kml;

/**
 *
 * @author Q262579
 */
public class VehicleProperties {
    public VehicleProperties() {
        doorDRD = LIDSTATE.INVALID;
        doorPSD = LIDSTATE.INVALID;
        doorDRDR = LIDSTATE.INVALID;
        doorPSDR = LIDSTATE.INVALID;

        windowDRD = LIDSTATE.INVALID;
        windowPSD = LIDSTATE.INVALID;
        windowDRDR = LIDSTATE.INVALID;
        windowPSDR = LIDSTATE.INVALID;

        bonnet = LIDSTATE.INVALID;
        trunk = LIDSTATE.INVALID;
        
        reltime = -1;
        gpsCoord = new GPSCoordinate(2147483647, 2147483647, reltime);
        pswfState = PSWF.INVALID;
    }
    public VehicleProperties(VehicleProperties vp) {
        this.doorDRD = vp.doorDRD;
        this.doorPSD = vp.doorPSD;
        this.doorDRDR = vp.doorDRDR;
        this.doorPSDR = vp.doorPSDR;

        this.windowDRD = vp.windowDRD;
        this.windowPSD = vp.windowPSD;
        this.windowDRDR = vp.windowDRDR;
        this.windowPSDR = vp.windowPSDR;

        this.bonnet = vp.bonnet;
        this.trunk = vp.trunk;
        
        this.gpsCoord = new GPSCoordinate(vp.gpsCoord);
        this.reltime = vp.reltime;
        this.pswfState = vp.pswfState;
    }
    public String listVehProp_old() {
        return "t[" + Long.toString(reltime) + "] " + pswfState.label + "\n" +
               bonnet() +
               frontDoors() +
               frontWindows() +
               rearDoors() +
               rearWindows() +
               trunk();               
    }
    public String listVehProp() {
        return "t[" + Long.toString(reltime) + "] " + pswfState.label + "\n" +
               bonnet() +
               frontRow() +               
               rearRow() +               
               trunk();               
    }
    private String bonnet() { return " +" + ((bonnet == LIDSTATE.OPEN) ? "/--\\" : "----") + "+\n" ; }
    private String frontDoors()  { return ((doorDRD == LIDSTATE.OPEN) ? "/ " : " |") + "*--*" + ((doorPSD == LIDSTATE.OPEN) ? " \\" : "|") + "\n" ; }
    private String frontWindows()  { return ((windowDRD == LIDSTATE.OPEN || windowDRD == LIDSTATE.INTERMEDIATE) ? " O" : " |") + "*--*" + ((windowPSD == LIDSTATE.OPEN || windowPSD == LIDSTATE.INTERMEDIATE) ? "O" : "|") + "\n" ; }
    private String rearDoors()   { return ((doorDRDR == LIDSTATE.OPEN) ? "/ " : " |") + "*--*" + ((doorPSDR == LIDSTATE.OPEN) ? " \\" : "|") + "\n" ; }
    private String rearWindows()   { return ((windowDRDR == LIDSTATE.OPEN || windowDRDR == LIDSTATE.INTERMEDIATE) ? " O" : " |") + "*--*" + ((windowPSDR == LIDSTATE.OPEN || windowPSDR == LIDSTATE.INTERMEDIATE) ? "O" : "|") + "\n" ; }
    private String trunk()  { return " +" + ((trunk == LIDSTATE.OPEN) ? "\\--/" : "----") + "+\n" ; }
    
    private String frontRow() {
        return getRow(doorDRD == LIDSTATE.OPEN, windowDRD == LIDSTATE.OPEN || windowDRD == LIDSTATE.INTERMEDIATE, windowDRD == LIDSTATE.INVALID,
                      doorPSD == LIDSTATE.OPEN, windowPSD == LIDSTATE.OPEN || windowPSD == LIDSTATE.INTERMEDIATE, windowPSD == LIDSTATE.INVALID);
    }
    private String rearRow() {
        return getRow(doorDRDR == LIDSTATE.OPEN, windowDRDR == LIDSTATE.OPEN || windowDRDR == LIDSTATE.INTERMEDIATE, windowDRDR == LIDSTATE.INVALID,
                      doorPSDR == LIDSTATE.OPEN, windowPSDR == LIDSTATE.OPEN || windowPSDR == LIDSTATE.INTERMEDIATE, windowPSDR == LIDSTATE.INVALID);
    }
    private String getRow(boolean dDR_o, boolean wDR_o, boolean wDR_u, boolean dPS_o, boolean wPS_o, boolean wPS_u) {
        String result = "";
        if (!dDR_o && !wDR_o) {
            if (wDR_u) // show '?' for unknown window state
                result += "?|" + "*--*";
            else
                result += " |" + "*--*";
        }
        if (dDR_o && !wDR_o) {
            if (wDR_u) // show '?' for unknown window state
                result += "/?" + "*--*";
            else
                result += "/ " + "*--*";
        }
        if (!dDR_o && wDR_o)
            result += " O" + "*--*";
        if (dDR_o && wDR_o)
            result += "( " + "*--*";
        
        if (!dPS_o && !wPS_o) {
            if (wPS_u) // show '?' for unknown window state
                return result += "|?" + "\n";
            else
                return result += "|" + "\n";
        }
        if (dPS_o && !wPS_o) {
            if (wPS_u) // show '?' for unknown window state
                return result += "?\\" + "\n";
            else
                return result += " \\" + "\n";
        }
        if (!dPS_o && wPS_o)
            return result += "O" + "\n";
        if (dPS_o && wPS_o)
            return result += " )" + "\n";
        
        return " |*--*|\n";        
    }
    
    public static LIDSTATE getBonnetDoorTrunkState(int signal, String lid_flap) {
        if (signal == 0) {
            //System.out.println(lid_flap + " (lid/flap) closed");
            return LIDSTATE.CLOSED;
        }
        if (signal == 1) {
            //System.out.println(lid_flap + " (lid/flap) open");
            return LIDSTATE.OPEN;
        }
        //System.out.println(lid_flap + " unknown");
        return LIDSTATE.INVALID;
    }
    
    public static LIDSTATE getWindowState(int signal, String win) {
        if (signal == 0) {
            //System.out.println(win + " window closed");
            return LIDSTATE.CLOSED;
        }
        if (signal == 1) {
            //System.out.println(win + " window intermediate");
            return LIDSTATE.INTERMEDIATE;
        }
        if (signal == 2) {
            //System.out.println(win + " window open");
            return LIDSTATE.OPEN;
        }
        System.out.println(win + " window unknown");
        return LIDSTATE.INVALID;
    }        
    
    public static boolean isEqual(VehicleProperties vpNew, VehicleProperties vpOld) {
        if ((vpNew.doorDRD == vpOld.doorDRD) &&
            (vpNew.doorPSD == vpOld.doorPSD) &&
            (vpNew.doorDRDR == vpOld.doorDRDR) &&
            (vpNew.doorPSDR == vpOld.doorPSDR) &&
            (vpNew.windowDRD == vpOld.windowDRD) &&
            (vpNew.windowPSD == vpOld.windowPSD) &&
            (vpNew.windowDRDR == vpOld.windowDRDR) &&
            (vpNew.windowPSDR == vpOld.windowPSDR) &&
            (vpNew.bonnet == vpOld.bonnet) &&
            (vpNew.trunk == vpOld.trunk)) {
            /*
            System.out.println("EQUAL");            
            System.out.println(vpOld);
            System.out.println(vpOld.listVehProp());
            System.out.println(vpNew);
            System.out.println(vpNew.listVehProp());
            */
            return true;
        }
        else {
            /*
            System.out.println("DIFFERENT");            
            System.out.println(vpOld);
            System.out.println(vpOld.listVehProp());
            System.out.println(vpNew);
            System.out.println(vpNew.listVehProp());
            */
            return false;            
        }        
    }
    
    public GPSCoordinate gpsCoord;
    public PSWF pswfState;
    public long reltime;
    
    public LIDSTATE doorDRD;
    public LIDSTATE doorPSD;
    public LIDSTATE doorDRDR;
    public LIDSTATE doorPSDR;
    
    public LIDSTATE windowDRD;
    public LIDSTATE windowPSD;
    public LIDSTATE windowDRDR;
    public LIDSTATE windowPSDR;
    
    public LIDSTATE bonnet;
    public LIDSTATE trunk;
}
