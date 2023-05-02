/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asc2kml;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Q262579
 */
public class KmlWriter {
    
    public KmlWriter(String bfn) {
        baseFilename = bfn;
        kmlFilename = baseFilename + kmlExtension;                
        
        try {
            fileWriter = new FileWriter(kmlFilename);
        } catch (IOException ex) {
            Logger.getLogger(KmlWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (fileWriter != null) {
            bW = new BufferedWriter(fileWriter);
        }
        
    }
    
    public boolean writeKmlNameDescription(String n, String d) {
        if (bW != null) {
            try {
                bW.write(t4 + "<name>" + n + "</name>"); bW.newLine();
                bW.write(t4 + "<open>1</open>"); bW.newLine();
                bW.write(t4 + "<description>" + d + "</description>"); bW.newLine();
                return true;
            } catch (IOException ex) {
                Logger.getLogger(KmlWriter.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }                        
        }
        return false;
    }
    
    public boolean writeFolderHeader(String name, String descr, boolean visibility, boolean open) {
        if (bW != null) {
            try {                                
                bW.write(t4 + "<Folder>"); bW.newLine();
                bW.write(t6 + "<name>" + name + "</name>"); bW.newLine();
                bW.write(t6 + "<description>" + descr + "</description>"); bW.newLine();
                bW.write(t6 + "<visibility>" + (visibility ? "1" : "0") + "</visibility>"); bW.newLine();
                bW.write(t6 + "<open>" + (open ? "1" : "0") + "</open>"); bW.newLine();
                
                return true;
            } catch (IOException ex) {
                Logger.getLogger(KmlWriter.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }                        
        }
        return false;
    }
    
    public boolean writeFolderFooter() {
        if (bW != null) {
            try {                                
                bW.write(t4 + "</Folder>"); bW.newLine();                
                
                return true;
            } catch (IOException ex) {
                Logger.getLogger(KmlWriter.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }                        
        }
        return false;
    }
    
    public boolean writeKmlHeader() {
        if (bW != null) {
            if (!headerWritten) {
                try {
                    bW.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); bW.newLine();
                    bW.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">"); bW.newLine();
                    bW.write(t2 + "<Document>"); bW.newLine();                    
                    headerWritten = true;
                    return true;
                } catch (IOException ex) {
                    Logger.getLogger(KmlWriter.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
        }
        return false;
    }
    
    public boolean writeKmlFooter() {
        if (bW != null) {
            if (headerWritten && !footerWritten) {                
                try {
                    bW.write(t2 + "</Document>"); bW.newLine();
                    bW.write("</kml>"); bW.newLine();
                    
                    bW.close();
                    
                    footerWritten = true;
                    return true;
                } catch (IOException ex) {
                    Logger.getLogger(KmlWriter.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
        }
        return false;
    }
    
    public boolean writeStyleIDs() {
        if (writeGPS_SID() &&
            writePSWF_SID() &&
            writeEntryExit_SID() &&
            writeExitHistory_SID() &&
            writePRES_SEG_SID() &&
            writeVehGpsPath_SID() &&
            writeVehProp_SID()
        ) {
            return true;
        }
        return false;
    }    
    
    private boolean writeLineStyle(String name, String color, String width) {
        if (bW != null) {
            try {                                
                    bW.write(t4 + "<Style id=\"" + name + "\">"); bW.newLine();
                    bW.write(t6 + "<LineStyle>"); bW.newLine();
                    bW.write(t8 + "<color>" + color + "</color>"); bW.newLine();
                    bW.write(t8 + "<width>" + width + "</width>"); bW.newLine();
                    bW.write(t6 + "</LineStyle>"); bW.newLine();
                    bW.write(t4 + "</Style>"); bW.newLine();                    
                
                    return true;
            } catch (IOException ex) {
                Logger.getLogger(KmlWriter.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }                        
        }
        return false;
    }
    
    private boolean writeIconStyle(String name, String url) {
        if (bW != null) {
            try {                                
                    bW.write(t4 + "<Style id=\"" + name + "\">"); bW.newLine();
                    bW.write(t6 + "<IconStyle>"); bW.newLine();
                    bW.write(t8 + "<Icon>"); bW.newLine();
                    bW.write(t10 + "<href>" + url + "</href>"); bW.newLine();
                    bW.write(t8 + "</Icon>"); bW.newLine();
                    bW.write(t6 + "</IconStyle>"); bW.newLine();
                    bW.write(t4 + "</Style>"); bW.newLine();
                
                    return true;
            } catch (IOException ex) {
                Logger.getLogger(KmlWriter.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }                        
        }
        return false;
    }
    
    public boolean writeLineStringHeader(String extrude, String altitude, String tessallate) {
        if (bW != null) {
            try {
                bW.write(t4 + "<Placemark>"); bW.newLine();
                bW.write(t4 + "<styleUrl>#VehGpsPathStyle</styleUrl>"); bW.newLine();
                bW.write(t4 + "<LineString>"); bW.newLine();
                bW.write(t6 + "<extrude>" + extrude + "</extrude>"); bW.newLine();
                bW.write(t6 + "<altitudeMode>" + altitude + "</altitudeMode>"); bW.newLine();     
                bW.write(t6 + "<tessallate>" + tessallate + "</tessallate>"); bW.newLine();                
                bW.write(t8 + "<coordinates>"); bW.newLine();                
                
                return true;
            } catch (IOException ex) {
                Logger.getLogger(KmlWriter.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }                        
        }
        return false;
    }
    
    public boolean writeLineStringCoordinate(double lon, double lat) {
        if (bW != null) {
            try {                                
                bW.write(t8 + lon + "," + lat + ",1"); bW.newLine();                
                
                return true;
            } catch (IOException ex) {
                Logger.getLogger(KmlWriter.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }                        
        }
        return false;
    }
    
    public boolean writeLineStringFooter() {
        if (bW != null) {
            try {                                
                bW.write(t8 + "</coordinates>"); bW.newLine();
                bW.write(t6 + "</LineString>"); bW.newLine();
                bW.write(t6 + "</Placemark>"); bW.newLine();
                
                return true;
            } catch (IOException ex) {
                Logger.getLogger(KmlWriter.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }                        
        }
        return false;
    }
    
    public boolean writePlacemark(String name, String descr, String iconStyle, GPSCoordinate gps) {
        if (bW != null) {
            try {                                
                bW.write(t4 + "<Placemark>"); bW.newLine();
                bW.write(t6 + "<name>" + name + "</name>"); bW.newLine();
                bW.write(t6 + "<description>" + descr + "</description>"); bW.newLine();     
                bW.write(t6 + "<styleUrl>#" + iconStyle + "</styleUrl>"); bW.newLine();
                //bW.write(t6 + "<font face=verdana>Verdana</font>"); bW.newLine();
                bW.write(t6 + "<Point>"); bW.newLine();
                bW.write(t8 + "<coordinates>" + gps.longitude + "," + gps.latitude + ",0</coordinates>"); bW.newLine();
                bW.write(t6 + "</Point>"); bW.newLine();
                bW.write(t4 + "</Placemark>"); bW.newLine();
                
                return true;
            } catch (IOException ex) {
                Logger.getLogger(KmlWriter.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }                        
        }
        return false;
    }
    
    public boolean writeGPSPlacemark(String name, String descr, GPSCoordinate gps) {
        if (writePlacemark(name, descr, "GPSIconStyle", gps)) {
            return true;
        }
        return false;
    }
    
    public boolean writePRES_SEGPlacemark(String name, String descr, GPSCoordinate gps) {
        if (writePlacemark(name, descr, "PresentSegmentIconStyle", gps)) {
            return true;
        }
        return false;
    }
    
    public boolean writePSWFPlacemark(String name, String descr, PSWF pswf, GPSCoordinate gps) {
        if (writePlacemark(name, descr, pswf.label + "IconStyle", gps)) {
            return true;
        }
        return false;        
    }
    
    public boolean writeEntryExitPlacemark(String name, String descr, String type, GPSCoordinate gps) {
        if (writePlacemark(name, descr, type + "IconStyle", gps)) {
            return true;
        }
        return false;        
    }
    
    public boolean writeExitHistoryPlacemark(String name, String descr, GPSCoordinate gps) {
        if (writePlacemark(name, descr, "ExitHistoryIconStyle", gps)) {
            return true;
        }
        return false;        
    }
    
    public boolean writeLidsFlapsPlacemark(String name, String descr, GPSCoordinate gps) {
        if (writePlacemark(name, descr, "VehiclePropertyIconStyle", gps)) {
            return true;
        }
        return false;        
    }
    
    private boolean writeGPS_SID() {
        if (writeIconStyle("GPSIconStyle", pal4 + "icon57.png"))
            return true;
        return false;
    }
    
    private boolean writePRES_SEG_SID() {
        if (writeIconStyle("PresentSegmentIconStyle", pal4 + "icon56.png"))
            return true;
        return false;
    }
    
    private boolean writePSWF_SID() {
        if (writeIconStyle("ParkenIconStyle", pal5 + "icon47.png") &&
            writeIconStyle("StandfunktionenIconStyle", pal5 + "icon26.png") &&
            writeIconStyle("WohnenIconStyle", pal5 + "icon30.png") &&
            writeIconStyle("PruefenAnDiIconStyle", pal5 + "icon47.png") &&
            writeIconStyle("FahrenIconStyle", pal5 + "icon61.png") &&
            writeIconStyle("InvalidIconStyle", pal5 + "icon40.png")
           )
            return true;
        return false;
    }        
    
    private boolean writeEntryExit_SID() {
        if (writeIconStyle("EntryIconStyle", local + "icon54(2).png") &&            
            writeIconStyle("ExitIconStyle", local + "icon7(2).png")
           )
            return true;
        return false;
    }
    
    private boolean writeExitHistory_SID() {
        if (writeIconStyle("ExitHistoryIconStyle", pal4 + "icon8.png"))
            return true;
        return false;
    }
    
    private boolean writeVehGpsPath_SID() {
        if (writeLineStyle("VehGpsPathStyle", "ffffffff", "2"))
            return true;
        return false;
    }
    
    private boolean writeVehProp_SID() {
        if (writeIconStyle("VehiclePropertyIconStyle", pal3 + "icon62.png"))
            return true;
        return false;
    }
    
    private String baseFilename = null;
    private String kmlFilename = null;
    private final String kmlExtension = ".kml";
    
    private final String t2 = "  ";
    private final String t4 = "    ";
    private final String t6 = "      ";
    private final String t8 = "        ";
    private final String t10 = "          ";
    
    private final String local = "C:\\Program Files\\GoogleEarthIcons\\";
    private final String pal2 = "http://maps.google.com/mapfiles/kml/pal2/";
    private final String pal3 = "http://maps.google.com/mapfiles/kml/pal3/";
    private final String pal4 = "http://maps.google.com/mapfiles/kml/pal4/";
    private final String pal5 = "http://maps.google.com/mapfiles/kml/pal5/";
    
    private boolean headerWritten = false;
    private boolean footerWritten = false;
    
    BufferedWriter bW = null;
    FileWriter fileWriter = null;
}
