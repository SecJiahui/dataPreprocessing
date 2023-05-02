/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package asc2kml;

import java.io.File;
import java.io.IOException;
import static java.lang.System.exit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Q262579
 */
public class Asc2Kml {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
                
        //String outputKmlFilename = "C:\\Temp\\test";        
        String outputKmlFilename = "C:\\Temp\\CAN\\2023-03-21_V718684\\appended";
        
        KmlWriter kmlwrt = new KmlWriter(outputKmlFilename);        
        kmlwrt.writeKmlHeader();
        kmlwrt.writeKmlNameDescription("DEMO", "Show GPS positions and ADAS segments");
        kmlwrt.writeStyleIDs();
        
        AscParser ascprsr = new AscParser(kmlwrt);
        
        // all files (.zip OR .asc) in this directory will be processed
        //String absolutePathname = "C:\\Temp\\test";
        //String absolutePathname = "C:\\Temp\\CAN\\2023-03-21_V718684\\IuK\\ASC";
        String absolutePathname = "C:\\Temp\\CAN\\2023-03-21_V718684\\IuK\\ZIP";
        File[] filesInPath = new File(absolutePathname).listFiles();
        ascprsr.processFilesInPath(filesInPath, "IuK");
        
        absolutePathname = "C:\\Temp\\CAN\\2023-03-21_V718684\\FAS\\ZIP";
        filesInPath = new File(absolutePathname).listFiles();
        ascprsr.processFilesInPath(filesInPath, "FAS");
        
        // do some post-processing after parsing; looking forward/backward in parsed data is now possible
        ascprsr.processExitHistory();        
        
        // write KML entities to file
        ascprsr.writeCON_VEH("PSWF", ascprsr.pswfStateChanges.size() + " PWF states", false, false);
        ascprsr.writeEntryExit("Entry/Exit", "Potential vehicle entries/exits", false, false);
        ascprsr.writeExitHistory("Exit History", ascprsr.exitTriggers.size() + " Potential vehicle exits", false, false);
        // no need for GPS positions output, since the ADSA segments and the path implicitely contain them
        //ascprsr.writeGPS("GPS", ascprsr.gpsCoordinates.size() + " GPS coordinates", false, false);
        ascprsr.writePRES_SEG("ADAS", ascprsr.adasSegments.size() + " ADAS segments", false, false);
        ascprsr.writeSegmentPath("PATH", ascprsr.adasSegments.size() + " ADAS segments path", false, false);
        ascprsr.writeLidsFlaps("LID(s)-FLAP(s)", ascprsr.vehicleProperties.size() + " LID-FLAP interactions", false, false);
        kmlwrt.writeKmlFooter();                
    }    
}
