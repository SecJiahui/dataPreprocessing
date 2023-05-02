/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asc2kml;

import static java.lang.Double.NaN;

/**
 *
 * @author Q262579
 */
public class ADASSegment {

    public GPSCoordinate gpsCoord;
    public long reltime;
    public int Idx;
    public int RawDirChAngle;
    public double DirChAngle;
    public int RawRad;
    public int Rad;
    public int Lanes;
    public STREETTYPE StreetType;
    public int Length;
    public int MapProps;
    public boolean urban;
    public boolean crossing;
    public boolean roundabout;
    public boolean parking;
    public boolean ramp;
    public boolean trafficlight;
    public boolean urbanlimit;
    public boolean signlimit;
    public boolean righthanddrive;
    public boolean generallimit;
    public int LimV;


    public ADASSegment(int i, int dca, int rad, int lan, STREETTYPE st, int len, int propm, int limv, GPSCoordinate gps, long rt) {
        Idx = i;
        RawDirChAngle = dca;
        RawRad = rad;
        Lanes = lan;
        StreetType = st;
        Length = len;
        MapProps = propm;
        setMapProps(MapProps);        
        LimV = limv;
        gpsCoord = gps;
        reltime = rt;
    }
    public ADASSegment(ADASSegment as) {
        this.Idx = as.Idx;
        this.DirChAngle = as.DirChAngle;
        this.RawDirChAngle = as.RawDirChAngle;
        this.Rad = as.Rad;
        this.RawRad = as.RawRad;
        this.Lanes = as.Lanes;
        this.StreetType = as.StreetType;
        this.Length = as.Length;
        this.MapProps = as.MapProps;
        this.setMapProps(as.MapProps);
        this.LimV = as.LimV;
        this.gpsCoord = new GPSCoordinate(as.gpsCoord);
        this.reltime = as.reltime;
    }
    @Override
    public String toString() {
        return listAllData();
    }
    public String listIndex() {
        return "t[" + reltime + "] " +
               "idx[" + printFEna_FFinv(Idx) + "]";
    }
    public String listAllData() {
        return "t[" + reltime + "] " +
               "idx[" + printFEna_FFinv(Idx) + "] " +
               "dca[" + printDCA(RawDirChAngle) + "] " +
               "rad[" + printRAD(RawRad) + "] " +
               "lan[" + printLanes(Lanes) + "] " +
               "st[" + StreetType.toString() + "] " +
               "len[" + printFEna_FFinv(Length) + "] " +
               "map[" +  printMapProps() + "] " +
               "limv[" + printLimV(LimV) + "] ";
    }
    private boolean getBit(int val, int mask) {        
        return (val & mask) == mask;
    }
    private void setMapProps(int mp) {        
        urban = getBit(mp, 1);
        crossing = getBit(mp, 2);
        roundabout = getBit(mp, 4);
        parking = getBit(mp, 8);
        ramp = getBit(mp, 16);
        trafficlight = getBit(mp, 32);
        urbanlimit = getBit(mp, 64);
        signlimit = getBit(mp, 128);
        righthanddrive = getBit(mp, 512);
        generallimit = getBit(mp, 1024);        
    }
    private String printMapProps() {
        String mp = "";
        
        if (MapProps == 2046) // not available
            return "//////////";        
        if (MapProps == 2047) // invalid
            return "!!!!!!!!!!";
        
        mp += urban ? "U" : "-";
        mp += crossing ? "X" : "-";
        mp += roundabout ? "O" : "-";
        mp += parking ? "P" : "-";
        mp += ramp ? "R" : "-";
        mp += trafficlight ? "L" : "-";
        mp += urbanlimit ? "u" : "-";
        mp += signlimit ? "s" : "-";
        mp += righthanddrive ? "r" : "-";
        mp += generallimit ? "g" : "-";
        
        return mp;
    }
    private String printFEna_FFinv(int val) {        
        if (val == 254)
            return "na";
        if (val == 255)
            return "inv";
        return Integer.toString(val);
    }
    private String printDCA(int dca) {
        if (dca == 4094) {
            DirChAngle = NaN;
            return "na";
        }
        if (dca == 4095) {
            DirChAngle = NaN;
            return "inv";
        }
        DirChAngle = (double)dca * 0.08793356 - 180.0;
        return String.format("%.1f",DirChAngle);
    }
    private String printRAD(int rad) {
        if (rad == 4093) {
            Rad = (int) NaN;
            return "straight";
        }
        if (rad == 4094) {
            Rad = (int) NaN;
            return "na";
        }
        if (rad == 4095) {
            DirChAngle = (int) NaN;
            return "inv";
        }
        Rad = rad * 5 - 10230;
        return Integer.toString(Rad);
    }
    private String printLanes(int lan) {
        if (lan == 0)
            return ">5";
        if (lan == 6)
            return "na";
        if (lan == 7)
            return "inv";
        return Integer.toString(lan);
    }
    private String printLimV(int limv) {
        if (limv == 0)
            return "unlim";
        if (limv == 150)
            return "na";
        if (limv == 155)
            return "inv";
        return Integer.toString(limv);
    }
}
