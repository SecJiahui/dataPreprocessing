/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asc2kml;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Q262579
 */
public class AscParser {
    public AscParser(KmlWriter k_w) {
        kw = k_w;
        
        currentFASRelativeTime = -1;
        currentIUKRelativeTime = -1;
        
        currentGPS = new GPSCoordinate(2147483647, 2147483647, currentIUKRelativeTime);
        currentVehicleProperties = new VehicleProperties();
        System.out.println("Initial VehProp\n" + currentVehicleProperties.listVehProp());
        gpsCoordinates = new ArrayList<>();
        adasSegments = new ArrayList<>();
        pswfStateChanges = new ArrayList<>();
        vehicleProperties = new ArrayList<>();
        
        ADAS_HISTORY_SIZE = 9;        
        adasHistory = new ADASSegment[ADAS_HISTORY_SIZE];
        
        LID_FLAP_HISTORY_SIZE = 20;
        LID_FLAP_HISTORY_LOOKBACK_TIME = 60;
        LID_FLAP_HISTORY_LOOKAHEAD_TIME = 60;
        MAX_TIME_BETWEEN_W_S_AND_S_P = 900;
        
        exitTriggers = new ArrayList<>();
        
        VVEH_HISTORY_SIZE = 1000;
        vehicleSpeedHistory = new double[VVEH_HISTORY_SIZE];
        vehicleSpeedAvgBuf = new AveragedBuffer<>(Double.class, VVEH_HISTORY_SIZE);
        
        YAWVEH_HISTORY_SIZE = 1000;
        vehicleYawrateAvgBuf = new AveragedBuffer<>(Double.class, VVEH_HISTORY_SIZE);
        
        STEER_DRV_HISTORY_SIZE = 1000;
        vehicleSteeringAvgBuf = new AveragedBuffer<>(Double.class, STEER_DRV_HISTORY_SIZE);
        
        ACCELX_HISTORY_SIZE = 1000;
        vehicleAccelXAvgBuf = new AveragedBuffer<>(Double.class, ACCELX_HISTORY_SIZE);
        
        ACCELY_HISTORY_SIZE = 1000;
        vehicleAccelYAvgBuf = new AveragedBuffer<>(Double.class, ACCELY_HISTORY_SIZE);
    }
    
    public void processFilesInPath(File[] filesInPath, String busIdentifier) {
        for (File file : filesInPath) {
            if (file.isFile()) {
                //String fileName = file.getName();
                String fileName = file.getAbsolutePath();
                System.out.println(fileName);
            }
        }
        for (File file : filesInPath) {
            if (file.isFile()) {
                try {                    
                    String fileName = file.getAbsolutePath();
                    int index = fileName.lastIndexOf('.');
                    if(index > 0) {
                        String extension = fileName.substring(index + 1);
                        if (extension.equalsIgnoreCase("zip")) {
                            //System.out.println("processing " + fileName);
                            parseZipAscFile(fileName, busIdentifier);
                        }
                        if (extension.equalsIgnoreCase("asc")) {
                            //System.out.println("processing " + fileName);
                            parseAscFile(fileName, busIdentifier);
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Asc2Kml.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public void parseAscFile(String filename, String busIdentifier) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(filename);
        InputStreamReader is = new InputStreamReader(fis);            
        BufferedReader br = new BufferedReader(is);                                    
        if (busIdentifier.equals("IuK"))
            processIUKCANFile(br);
        if (busIdentifier.equals("FAS"))
            processFASCANFile(br);
        
    }
    
    public void parseZipAscFile(String filename, String busIdentifier) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(filename);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ZipInputStream zis = new ZipInputStream(bis);
        while ((zis.getNextEntry()) != null) {
            // actually there shall be only one file in this zip
            InputStreamReader is = new InputStreamReader(zis);            
            BufferedReader br = new BufferedReader(is);                        
            if (busIdentifier.equals("IuK"))
                processIUKCANFile(br);
            if (busIdentifier.equals("FAS"))
                processFASCANFile(br);
        }
    }
    
    private void processFASCANFile(BufferedReader br) throws IOException {                
        // tokens[11] is the first valid data payload
        String read = br.readLine();                
        
        while(read != null) {            
            String[] tokens = read.split(" ");
            for (int i = 0; i < tokens.length - 1; i++) {
                if (tokens[i].equalsIgnoreCase("1A1")) {                    
                    //System.out.println(read);
                    parse_FASCAN_V_VEH(tokens);
                }
                if (tokens[i].equalsIgnoreCase("18E")) {                    
                    //System.out.println(read);
                    parseACLNX_MASSCNTR(tokens);
                }
                if (tokens[i].equalsIgnoreCase("18F")) {
                    //System.out.println(read);
                    parseACLNY_MASSCNTR(tokens);
                }
                if (tokens[i].equalsIgnoreCase("442")) {
                    // since a 3 digit numerical identifier can also occur as data,
                    // also check if the previous token is 'Rx' (or 'Tx'?)
                    if (i > 3) {
                        if (tokens[i-1].equalsIgnoreCase("Rx")) {
                            //System.out.println(read);
                            parse_FASCAN_RELATIVZEIT(tokens);
                        }
                    }                                        
                }
                if (tokens[i].equalsIgnoreCase("195")) {
                    // since a 3 digit numerical identifier can also occur as data,
                    // also check if the previous token is 'Rx' (or 'Tx'?)
                    if (i > 3) {
                        if (tokens[i-1].equalsIgnoreCase("Rx")) {
                            //System.out.println(read);
                            parse_VYAW_VEH(tokens);
                        }
                    }                                        
                }
                if (tokens[i].equalsIgnoreCase("198")) {
                    // since a 3 digit numerical identifier can also occur as data,
                    // also check if the previous token is 'Rx' (or 'Tx'?)
                    if (i > 3) {
                        if (tokens[i-1].equalsIgnoreCase("Rx")) {
                            //System.out.println(read);
                            parse_AVL_STEA_DV(tokens);
                        }
                    }                                        
                }
            }                
            read = br.readLine();
        }
    }
    
    private void processIUKCANFile(BufferedReader br) throws IOException {                
        // tokens[6] is the first valid data payload
        String read = br.readLine();
        
        while(read != null) {                
            String[] tokens = read.split(" ");
            for (int i = 0; i < tokens.length - 1; i++) {
                if (tokens[i].equalsIgnoreCase("328")) {
                    //System.out.println(read);
                    parse_IUKCAN_RELATIVZEIT(tokens);
                }
                if (tokens[i].equalsIgnoreCase("34A")) {
                    //System.out.println(read);
                    parse_NAV_GPS1(tokens);
                }
                if (tokens[i].equalsIgnoreCase("43D")) {
                    //System.out.println(read);
                    parse_NAVGRPH_2_PRES_SEG(tokens);
                }
                if (tokens[i].equalsIgnoreCase("3C")) {
                    // since a two character CAN identifier can also match with a payload byte,
                    // also check if the next token is 'Rx' (or 'Tx')
                    if (i < tokens.length - 2) {
                        if (tokens[i+1].equalsIgnoreCase("Rx")) {
                            //System.out.println(read);
                            parse_CON_VEH(tokens);
                        }
                    }
                }
                if (tokens[i].equalsIgnoreCase("2FC")) {
                    //System.out.println(read);
                    parse_STAT_ZV_KLAPPEN(tokens);
                }
                if (tokens[i].equalsIgnoreCase("3B6")) {
                    //System.out.println(read);
                    parse_ST_PO_WRG_ALL(tokens, "DR");
                }
                if (tokens[i].equalsIgnoreCase("3B7")) {
                    //System.out.println(read);
                    parse_ST_PO_WRG_ALL(tokens, "DRR");
                }
                if (tokens[i].equalsIgnoreCase("3B8")) {
                    //System.out.println(read);
                    parse_ST_PO_WRG_ALL(tokens, "PS");
                }
                if (tokens[i].equalsIgnoreCase("3B9")) {
                    //System.out.println(read);
                    parse_ST_PO_WRG_ALL(tokens, "PSR");
                }
            }                
            read = br.readLine();
        }
    }
    
    public void writeGPS(String folderName, String folderDescription, boolean visibility, boolean open) {
        kw.writeFolderHeader(folderName, folderDescription, visibility, open);
        for (GPSCoordinate gps : gpsCoordinates) {
            kw.writeGPSPlacemark(Integer.toString(gpsCounter++), "", gps);
        }
        kw.writeFolderFooter();
    }
    
    public void writePRES_SEG(String folderName, String folderDescription, boolean visibility, boolean open) {
        kw.writeFolderHeader(folderName, folderDescription, visibility, open);
        for (ADASSegment seg : adasSegments) {
            kw.writePRES_SEGPlacemark(Integer.toString(seg.Idx), seg.listAllData(), seg.gpsCoord);
        }
        kw.writeFolderFooter();
    }
    
    public void writeSegmentPath(String folderName, String folderDescription, boolean visibility, boolean open) {
        kw.writeFolderHeader(folderName, folderDescription, visibility, open);
        kw.writeLineStringHeader("1", "relativeToGround", "1");
        for (ADASSegment seg : adasSegments) {
            kw.writeLineStringCoordinate(seg.gpsCoord.longitude, seg.gpsCoord.latitude);
        }
        kw.writeLineStringFooter();
        kw.writeFolderFooter();
    }
    
    public void writeEntryExit(String folderName, String folderDescription, boolean visibility, boolean open) {
        kw.writeFolderHeader(folderName, folderDescription, visibility, open);
        for (PSWFStateChange pswf : pswfStateChanges) {
            // check if we are in an entry scenario
            if (pswf.oldPSWF == PSWF.PARKEN && pswf.newPSWF == PSWF.STDFKT) {
                kw.writeEntryExitPlacemark("Enter", "t[" + pswf.reltime + "] " + pswf.oldPSWF.label + "->" + pswf.newPSWF.label, "Entry", pswf.gpsCoord);
            }
            // check if we are in an exit scenario
            if (pswf.oldPSWF == PSWF.STDFKT && pswf.newPSWF == PSWF.PARKEN) {
                kw.writeEntryExitPlacemark("Exit", "t[" + pswf.reltime + "] " + pswf.oldPSWF.label + "->" + pswf.newPSWF.label, "Exit", pswf.gpsCoord);                
            }            
        }
        kw.writeFolderFooter();
    }
    
    public void writeExitHistory(String folderName, String folderDescription, boolean visibility, boolean open) {
        kw.writeFolderHeader(folderName, folderDescription, visibility, open);
        for (ExitTrigger etr : exitTriggers) {            
            kw.writeExitHistoryPlacemark("Exit",
                    "t[" + etr.pswfChange.reltime + "] " + etr.pswfChange.oldPSWF.label + "->" + etr.pswfChange.newPSWF.label + "\n" +
                    etr.posFingerprint.listSegmentHistorySimple() + "\n" + etr.listVehicleProperties(), etr.pswfChange.gpsCoord);                            
        }
        kw.writeFolderFooter();
    }
    
    public void writeCON_VEH(String folderName, String folderDescription, boolean visibility, boolean open) {
        kw.writeFolderHeader(folderName, folderDescription, visibility, open);
        for (PSWFStateChange pswf : pswfStateChanges) {
            kw.writePSWFPlacemark(pswf.newPSWF.label, "t[" + pswf.reltime + "] " + pswf.oldPSWF.label + "->" + pswf.newPSWF.label, pswf.newPSWF, pswf.gpsCoord);
        }
        kw.writeFolderFooter();
    }
    
    public void writeLidsFlaps(String folderName, String folderDescription, boolean visibility, boolean open) {
        kw.writeFolderHeader(folderName, folderDescription, visibility, open);
        for (VehicleProperties prop : vehicleProperties) {
            kw.writeLidsFlapsPlacemark("L/F", prop.listVehProp(), prop.gpsCoord);
        }
        kw.writeFolderFooter();
    }
    
    private boolean fasReltimeValid() {
        return currentFASRelativeTime != -1;
    }
    
    private void parse_FASCAN_RELATIVZEIT(String[] all_tokens) {
        /*
        * RELATIVZEIT -> 442, T_SEC_COU_REL=bytes0-3
        */
        
        String stringReltime = all_tokens[14]+all_tokens[13]+all_tokens[12]+all_tokens[11];
        currentFASRelativeTime = Long.parseLong(stringReltime, 16);        
    }
    
    private void parseACLNX_MASSCNTR(String[] all_tokens) {
        /*
        * ACLNX_MASSCNTR -> 18E, ACLNX_COG=bytes2-3
        */
        
        String stringAccelX = all_tokens[14]+all_tokens[13];
        int intAccelX = Integer.parseInt(stringAccelX, 16);
        double doubleAccelX = intAccelX * 0.002 - 65;
        
        if (vehicleAccelXAvgBuf.pushedToFull(doubleAccelX)) {            
            System.out.println("ACCELX avg = " + vehicleAccelXAvgBuf.getAverage(0.0, Double.valueOf(ACCELX_HISTORY_SIZE)));
            vehicleAccelXAvgBuf.pushedToFull(doubleAccelX);
        }
    }
    
    private void parseACLNY_MASSCNTR(String[] all_tokens) {
        /*
        * ACLNY_MASSCNTR -> 18F, ACLNY_COG=bytes2-3
        */
        
        String stringAccelY = all_tokens[14]+all_tokens[13];
        int intAccelY = Integer.parseInt(stringAccelY, 16);
        double doubleAccelY = intAccelY * 0.002 - 65;
        
        if (vehicleAccelYAvgBuf.pushedToFull(doubleAccelY)) {            
            System.out.println("ACCELY avg = " + vehicleAccelYAvgBuf.getAverage(0.0, Double.valueOf(ACCELY_HISTORY_SIZE)));
            vehicleAccelYAvgBuf.pushedToFull(doubleAccelY);
        }
    }
    
    private void parse_AVL_STEA_DV(String[] all_tokens) {
        /*
        * AVL_STEA_DV -> 198, AVL_STEA_DV=bytes2-3
        */
        
        String stringSteerDrv = all_tokens[14]+all_tokens[13];
        int intSteerDrv = Integer.parseInt(stringSteerDrv, 16);
        double doubleSteerDrv = intSteerDrv * 0.0439453125 -1439.9560546875;
        
        if (vehicleSteeringAvgBuf.pushedToFull(doubleSteerDrv)) {            
            System.out.println("STEER avg = " + vehicleSteeringAvgBuf.getAverage(0.0, Double.valueOf(STEER_DRV_HISTORY_SIZE)));
            vehicleSteeringAvgBuf.pushedToFull(doubleSteerDrv);
        }
    }
    
    private void parse_VYAW_VEH(String[] all_tokens) {
        /*
        * VYAW_VEH -> 195, VYAW_VEH=bytes2-3
        */
        
        String stringYveh = all_tokens[14]+all_tokens[13];
        int intYveh = Integer.parseInt(stringYveh, 16);
        double doubleYveh = intYveh * 0.005 - 163.84;
        
        if (vehicleYawrateAvgBuf.pushedToFull(doubleYveh)) {            
            System.out.println("YAW avg = " + vehicleYawrateAvgBuf.getAverage(0.0, Double.valueOf(YAWVEH_HISTORY_SIZE)));
            vehicleYawrateAvgBuf.pushedToFull(doubleYveh);
        }
    }
    
    private void parse_FASCAN_V_VEH(String[] all_tokens) {
        /*
        * V_VEH -> 1A1, V_VEH_COG=bytes2-3
        */
        
        String stringVveh = all_tokens[14]+all_tokens[13];
        int intVveh = Integer.parseInt(stringVveh, 16);
        double doubleVveh = intVveh * 0.015625;
        
        if (vehicleSpeedAvgBuf.pushedToFull(doubleVveh)) {            
            System.out.println("VVEH avg = " + vehicleSpeedAvgBuf.getAverage(0.0, Double.valueOf(VVEH_HISTORY_SIZE)));
            vehicleSpeedAvgBuf.pushedToFull(doubleVveh);
        }
        
        /*
        vehicleSpeedHistory[(VVEH_HISTORY_POS++) % VVEH_HISTORY_SIZE] = doubleVveh;        
        if (VVEH_HISTORY_POS % VVEH_HISTORY_SIZE == 0) {        
            double vVeh = 0;
            for (int i = 0; i < VVEH_HISTORY_SIZE; i++) {
                vVeh += vehicleSpeedHistory[i];                
            }
            vVeh /= VVEH_HISTORY_SIZE;
            System.out.println("t=" + currentFASRelativeTime + " V=" + vVeh);
        }
        */
    }
    
    private boolean iukReltimeValid() {
        return currentIUKRelativeTime != -1;
    }
    
    private void parse_IUKCAN_RELATIVZEIT(String[] all_tokens) {
        /*
        * RELATIVZEIT -> 328, T_SEC_COU_REL=bytes0-3
        */
        
        String stringReltime = all_tokens[9]+all_tokens[8]+all_tokens[7]+all_tokens[6];
        currentIUKRelativeTime = Long.parseLong(stringReltime, 16);        
    }
    
    private void parse_NAV_GPS1(String[] all_tokens) {
        /*
        * NAV_GPS1 -> 34A, ST_LONG_NAVI=bytes0-3, ST_LAT_NAVI=bytes4-7
        */
        
        String stringLong = all_tokens[9]+all_tokens[8]+all_tokens[7]+all_tokens[6];
        long decimalLong = Long.parseLong(stringLong, 16);                                    
        
        String stringLat = all_tokens[13]+all_tokens[12]+all_tokens[11]+all_tokens[10];
        long decimalLat = Long.parseLong(stringLat, 16);                                  
        
        GPSCoordinate gps = new GPSCoordinate(decimalLong, decimalLat, currentIUKRelativeTime);
        
        double dist = GPSCoordinate.distance(gps, currentGPS);
        //System.out.println("dist " + dist);
        
        if (gps.isValid && iukReltimeValid())
            currentGPS = gps;
        
        if (gps.isValid && dist > 0.0001 && iukReltimeValid()) {            
            gpsCoordinates.add(gps);
            //kw.writeGPSPlacemark(Integer.toString(gpsCounter++), "", gpsCoord);
        }
    }   
    
    private void parse_NAVGRPH_2_PRES_SEG(String[] all_tokens) {
        /*
        * NAVGRPH_2_PRES_SEG -> 43D, IDX_PRES_SEG_NAVGRPH_2=byte0, L_SEG_NAVGRPH_2=byte5
        */
        String stringVal = all_tokens[6];
        int decimalIdx = Integer.parseInt(stringVal, 16);
        
        stringVal = all_tokens[8];
        int decimalDirChAngle_p1 = 15 & Integer.parseInt(stringVal, 16);
        decimalDirChAngle_p1 <<= 8;
        stringVal = all_tokens[7];
        int decimalDirChAngle_p2 = Integer.parseInt(stringVal, 16);
        decimalDirChAngle_p2 |= decimalDirChAngle_p1;
        
        stringVal = all_tokens[8];
        int decimalRad_p1 = 240 & Integer.parseInt(stringVal, 16);
        decimalRad_p1 >>= 4;
        stringVal = all_tokens[9];
        int decimalRad_p2 = Integer.parseInt(stringVal, 16);
        decimalRad_p2 <<= 4; 
        decimalRad_p2 |= decimalRad_p1;
        
        stringVal = all_tokens[10];
        int decimalLanes = Integer.parseInt(stringVal, 16);
        decimalLanes &= 7;
        
        stringVal = all_tokens[10];
        int decimalStreetType = Integer.parseInt(stringVal, 16);
        decimalStreetType &= 248;
        decimalStreetType >>= 3;
        STREETTYPE st = STREETTYPE.fromInteger(decimalStreetType);
        
        stringVal = all_tokens[11];
        int decimalLength = Integer.parseInt(stringVal, 16);        
        
        stringVal = all_tokens[13];
        int decimalPropMap_p1 = 7 & Integer.parseInt(stringVal, 16);
        decimalPropMap_p1 <<= 8;
        stringVal = all_tokens[12];
        int decimalPropMap_p2 = Integer.parseInt(stringVal, 16);
        decimalPropMap_p2 |= decimalPropMap_p1;
        
        stringVal = all_tokens[13];
        int decimalLimV = Integer.parseInt(stringVal, 16);
        decimalLimV >>= 3;
        decimalLimV &= 31;
        decimalLimV *= 5;
        
        if (decimalIdx != lastIdx) {
            lastIdx = decimalIdx;
            if (currentGPS.isValid && iukReltimeValid()) {
                currentSegment = new ADASSegment(decimalIdx, decimalDirChAngle_p2, decimalRad_p2, decimalLanes, st, decimalLength, decimalPropMap_p2, decimalLimV, currentGPS, currentIUKRelativeTime);
                adasSegments.add(currentSegment);                                
                                
                //kw.writePRES_SEGPlacemark(Integer.toString(decimalIdx), "idx[" + Integer.toString(decimalIdx) + "] " + "len[" + Integer.toString(decimalLength) + "] ", currentGPS);
            }
        }
    }
    
    private void getADASHistory(ExitTrigger exitTrigger, long t_to) {
        int adasHistPos = 0;
        for (int count = 0; count < adasSegments.size(); count++) {
            ADASSegment adasSegm = adasSegments.get(count);
            if (adasSegm.reltime <= t_to) {
                int pos = (adasHistPos++) % ADAS_HISTORY_SIZE;
                adasHistory[pos] = adasSegm;
            }
        }
        // output history in fifo order
        for (int i = adasHistPos + ADAS_HISTORY_SIZE - 1; i >= adasHistPos; i--) {
            ADASSegment aH = adasHistory[i % ADAS_HISTORY_SIZE];
            if (aH != null) {                
                exitTrigger.posFingerprint.addSegment(aH);
                System.out.println("adding ADAS segment " + aH.listIndex());
            }
        }        
    }
    
    private void getVehiclePropertiesHistory(ExitTrigger exitTrigger, long t_from, long t_to) {        
        int addedVehProps = 0;
        for (int count = 0; count < vehicleProperties.size(); count++) {
            VehicleProperties vehProp = vehicleProperties.get(count);
            if (vehProp.reltime >= t_from && vehProp.reltime <= t_to) {
                // add always to the beginning of the list, so that we start with the most recent one
                exitTrigger.vehProperties.add(0, vehProp);
                System.out.println("adding vehicle property from t=" + vehProp.reltime);
                if (addedVehProps++ >= LID_FLAP_HISTORY_SIZE)
                    return;
            }
        }    
    }
    
    public void processExitHistory() {
        ExitTrigger activeExitTrigger = null;
        long t_WOHN_STAND = 0;
        long t_STAND_PARK = 0;
        
        // iterate over all PSWF state changes
        for (int pswfPos = 0; pswfPos < pswfStateChanges.size(); pswfPos++) {
            PSWFStateChange pswfST_W_S = pswfStateChanges.get(pswfPos);
            // check if we are at the beginning of an exit scenario: WOHNEN -> STANDFUNKTIONEN
            if (pswfST_W_S.oldPSWF == PSWF.WOHNEN && pswfST_W_S.newPSWF == PSWF.STDFKT) {
                
                t_WOHN_STAND = pswfST_W_S.reltime;
                
                // create exit trigger with the timestamp of the PSWF change
                activeExitTrigger = new ExitTrigger(pswfST_W_S, ADAS_HISTORY_SIZE, 0, 0, t_WOHN_STAND);
                System.out.println("FOUND EXIT AT " + t_WOHN_STAND);
                
                // add vehicle properties within interval ["exit trigger timestamp"-LID_FLAP_HISTORY_LOOKBACK_TIME .."exit trigger timestamp"+LID_FLAP_HISTORY_LOOKAHEAD_TIME]
                System.out.println("adding vehicle properties around W->S...");
                getVehiclePropertiesHistory(activeExitTrigger, t_WOHN_STAND - LID_FLAP_HISTORY_LOOKBACK_TIME, t_WOHN_STAND + LID_FLAP_HISTORY_LOOKAHEAD_TIME);                
                System.out.println("...done");
                
                // build up ADAS segment history up to "exit trigger timestamp"
                System.out.println("adding ADAS segment history...");
                getADASHistory(activeExitTrigger, t_WOHN_STAND);                
                System.out.println("...done");
                
                // if within MAX_TIME_BETWEEN_W_S_AND_S_P seconds the next PSWF change is STANDFUNKTIONEN -> PARKEN, we assume it belongs to this exit scenario
                PSWFStateChange pswfST_S_P = pswfStateChanges.get(pswfPos + 1);
                if (pswfST_S_P.oldPSWF == PSWF.STDFKT && pswfST_S_P.newPSWF == PSWF.PARKEN && pswfST_S_P.reltime <= t_WOHN_STAND + MAX_TIME_BETWEEN_W_S_AND_S_P) {
                    t_STAND_PARK = pswfST_S_P.reltime;
                    System.out.println("adding properties before final PARKEN at " + t_STAND_PARK + " (" + (t_STAND_PARK - t_WOHN_STAND) + "s after STDFKT)...");
                    getVehiclePropertiesHistory(activeExitTrigger, t_WOHN_STAND + LID_FLAP_HISTORY_LOOKAHEAD_TIME + 1, t_STAND_PARK);
                    System.out.println("...done");
                }
                
                System.out.println(activeExitTrigger.posFingerprint.listSegmentHistoryDetailed());
                System.out.println(activeExitTrigger.listVehicleProperties());
                
                exitTriggers.add(activeExitTrigger);
            }            
        }
    }
    
    private void parse_CON_VEH(String[] all_tokens) {
        /*
        * CON_VEH -> 3C, ST_CON_VEH=byte6, bits0-3
        */
        String stringConVeh = all_tokens[12];
        int decimalConVeh = Integer.parseInt(stringConVeh, 16);
        // mask out the highest 4 bits
        decimalConVeh &= 15;
        
        PSWF pswf = PSWF.INVALID;
        switch (decimalConVeh) {
            case 1, 2 -> pswf = PSWF.PARKEN;
            case 3 -> pswf = PSWF.STDFKT;
            case 5 -> pswf = PSWF.WOHNEN;
            case 7 -> pswf = PSWF.PAD;
            case 8, 10, 12 -> pswf = PSWF.FAHREN;
        }
        
        if (pswf != lastPswf && currentGPS.isValid && iukReltimeValid()) {            
            PSWFStateChange pswfChange = new PSWFStateChange(currentGPS, lastPswf, pswf, currentIUKRelativeTime);                        
            
            lastPswf = pswf;
            pswfStateChanges.add(pswfChange);
            //kw.writePSWFPlacemark(pswf, pswf.label, "", currentGPS);            
        }
    }
    
    private void parse_ST_PO_WRG_ALL(String[] all_tokens, String winType) {
        /*            
            POSITION_FH_FAT -> 3B6, ST_PO_WRG_DRD=byte1, bits0-1
            POSITION_FH_FATH -> 3B7, ST_PO_WRG_DVDR=byte1, bits0-1
            POSITION_FH_BFT -> 3B8, ST_PO_WRG_PSD=byte1, bits0-1
            POSITION_FH_BFTH -> 3B9, ST_PO_WRG_PSDR=byte1, bits0-1
        */
        VehicleProperties prop = new VehicleProperties(currentVehicleProperties);        
        
        String stringStatWin = all_tokens[7];
        int decimalStatWin = Integer.parseInt(stringStatWin, 16);
        // mask out the highest 6 bits
        decimalStatWin &= 3;
        boolean winInvalid = false;
        switch (winType) {
            case "DR" :
                prop.windowDRD = VehicleProperties.getWindowState(decimalStatWin, "DR");
                winInvalid = prop.windowDRD == LIDSTATE.INVALID;
                break;
            case "DRR" :
                prop.windowDRDR = VehicleProperties.getWindowState(decimalStatWin, "DRR");
                winInvalid = prop.windowDRDR == LIDSTATE.INVALID;
                break;
            case "PS" :
                prop.windowPSD = VehicleProperties.getWindowState(decimalStatWin, "PS");
                winInvalid = prop.windowPSD == LIDSTATE.INVALID;
                break;
            case "PSR" :
                prop.windowPSDR = VehicleProperties.getWindowState(decimalStatWin, "PSR");
                winInvalid = prop.windowPSDR == LIDSTATE.INVALID;
                break;
            default :
                System.out.println("no win type specified");
                break;
        }                
        
        if (currentGPS.isValid && iukReltimeValid()) {
            prop.gpsCoord = currentGPS;
            prop.reltime = currentIUKRelativeTime;
            prop.pswfState = lastPswf;
            if (!VehicleProperties.isEqual(prop, currentVehicleProperties) && !winInvalid) {
                currentVehicleProperties = new VehicleProperties(prop);                
                vehicleProperties.add(new VehicleProperties(prop));

                //System.out.println("__________");
                //System.out.println(prop.listVehProp());                
            }
        }
        else
            System.out.println("GPS or relative time invalid\n");
        prop = null;
    }
    
    private void parse_STAT_ZV_KLAPPEN(String[] all_tokens) {
        /*
        * STAT_ZV_KLAPPEN -> 2FC, ST_CT_BTL=byte2, bits0-1
        */
        VehicleProperties prop = new VehicleProperties(currentVehicleProperties);
        
        String stringStatBtl = all_tokens[8];
        int decimalStatBtl = Integer.parseInt(stringStatBtl, 16);
        // mask out the highest 6 bits
        decimalStatBtl &= 3;
        prop.trunk = VehicleProperties.getBonnetDoorTrunkState(decimalStatBtl, "trunk");
        
        String stringStatBon = all_tokens[8];
        int decimalStatBon = Integer.parseInt(stringStatBon, 16);
        // shift 2 bits to right and mask out the highest 6 bits
        decimalStatBon >>= 2;
        decimalStatBon &= 3;
        prop.bonnet = VehicleProperties.getBonnetDoorTrunkState(decimalStatBon, "bonnet");
        
        String stringStatDoors = all_tokens[7];
        int decimalStatDoors = Integer.parseInt(stringStatDoors, 16);
        // shift the right amount of bits to right and mask out the rest accordingly
        prop.doorDRD = VehicleProperties.getBonnetDoorTrunkState(decimalStatDoors & 3, "DRD");
        prop.doorPSD = VehicleProperties.getBonnetDoorTrunkState((decimalStatDoors >> 2) & 3, "PSD");
        prop.doorDRDR = VehicleProperties.getBonnetDoorTrunkState((decimalStatDoors >> 4) & 3, "DRDR");
        prop.doorPSDR = VehicleProperties.getBonnetDoorTrunkState((decimalStatDoors >> 6) & 3, "PSDR");
        
        if (currentGPS.isValid && iukReltimeValid()) {
            prop.gpsCoord = currentGPS;
            prop.reltime = currentIUKRelativeTime;
            prop.pswfState = lastPswf;
            if (!VehicleProperties.isEqual(prop, currentVehicleProperties)) {
                currentVehicleProperties = new VehicleProperties(prop);
                vehicleProperties.add(new VehicleProperties(prop));

                //System.out.println("__________");
                //System.out.println(prop.listVehProp());                
            }
        }
        prop = null;
    }
    
    public KmlWriter kw;
    
    public long currentFASRelativeTime;
    public long currentIUKRelativeTime;
    public GPSCoordinate currentGPS;
    public ArrayList<GPSCoordinate> gpsCoordinates;
    
    ADASSegment currentSegment;
    public ArrayList<ADASSegment> adasSegments;
    
    public ArrayList<PSWFStateChange> pswfStateChanges;
    
    public VehicleProperties currentVehicleProperties;
    public ArrayList<VehicleProperties> vehicleProperties;        
    
    public ADASSegment[] adasHistory;
    private final int ADAS_HISTORY_SIZE;
    
    private final int LID_FLAP_HISTORY_SIZE;
    private final int LID_FLAP_HISTORY_LOOKBACK_TIME;
    private final int LID_FLAP_HISTORY_LOOKAHEAD_TIME;
    private final int MAX_TIME_BETWEEN_W_S_AND_S_P;
    public ArrayList<ExitTrigger> exitTriggers;
    
    AveragedBuffer<Double> vehicleSpeedAvgBuf;
    private final int VVEH_HISTORY_SIZE;
    
    AveragedBuffer<Double> vehicleYawrateAvgBuf;
    private final int YAWVEH_HISTORY_SIZE;
    
    AveragedBuffer<Double> vehicleSteeringAvgBuf;
    private final int STEER_DRV_HISTORY_SIZE;
    
    AveragedBuffer<Double> vehicleAccelXAvgBuf;
    private final int ACCELX_HISTORY_SIZE;
    
    AveragedBuffer<Double> vehicleAccelYAvgBuf;
    private final int ACCELY_HISTORY_SIZE;
    
    public double vehicleSpeedHistory[];    
    private int VVEH_HISTORY_POS = 0;
    
    private int lastIdx = -1;
    private PSWF lastPswf = PSWF.INVALID;
    private static int gpsCounter = 0;
}
