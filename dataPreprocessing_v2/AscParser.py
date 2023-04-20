import zipfile
import LIDSTATE
from ADASSegment import ADASSegment
from GPSCoordinate import GPSCoordinate
from PSWF import PSWFStateChange, PSWF
from VehicleProperties import VehicleProperties


class AscParser:
    def __init__(self, k_w):
        self.kw = k_w
        self.currentGPS = GPSCoordinate(2147483647, 2147483647)
        self.currentVehicleProperties = VehicleProperties()
        self.gpsCoordinates = []
        self.adasSegments = []
        self.pswfStateChanges = []
        self.vehicleProperties = []
        self.lastIdx = -1
        self.lastPswf = PSWF.INVALID
        self.num = 0

    def parseAscFile(self, filename):
        with open(filename, "r") as file:
            self.processFile(file)

    def parseZipAscFile(self, filename):
        with zipfile.ZipFile(filename, 'r') as zip_ref:
            for file in zip_ref.namelist():
                with zip_ref.open(file) as file:
                    self.processFile(file)

    def processFile(self, file):
        for read in file:
            read = read.rstrip('\n')
            # print(read)
            tokens = read.split(" ")
            # print(tokens)
            for i in range(len(tokens) - 1):
                if tokens[i].upper() == '34A':
                    self.parse_NAV_GPS1(tokens)
                elif tokens[i].upper() == '43D':
                    self.parse_NAVGRPH_2_PRES_SEG(tokens)
                elif tokens[i].upper() == '3C':
                    if i < len(tokens) - 2:
                        if tokens[i + 1].upper() == 'RX':
                            self.parse_CON_VEH(tokens)
                elif tokens[i].upper() == '2FC':
                    self.parse_STAT_ZV_KLAPPEN(tokens)

    def writeGPS(self, folderName, folderDescription):
        self.kw.write_folder_header(folderName, folderDescription)
        for gps in self.gpsCoordinates:
            self.kw.write_gps_placemark(str(self.num), "", gps)
            self.num += 1
        self.kw.write_folder_footer()

    def writePRES_SEG(self, folderName, folderDescription):
        self.kw.write_folder_header(folderName, folderDescription)
        for seg in self.adasSegments:
            self.kw.write_pres_seg_placemark(str(seg.idx),
                                           "idx[" + str(seg.idx) + "] " +
                                           "len[" + str(seg.length) + "] " +
                                           "limv[" + str(seg.lim_v) + "] ",
                                           seg.gps_coord)
        self.kw.write_folder_footer()

    def writeSegmentPath(self, folderName, folderDescription):
        self.kw.write_folder_header(folderName, folderDescription)
        self.kw.write_line_string_header("1", "relativeToGround", "1")
        for seg in self.adasSegments:
            self.kw.write_line_string_coordinate(seg.gps_coord.longitude, seg.gps_coord.latitude)
        self.kw.write_line_string_footer()
        self.kw.write_folder_footer()

    def writeCON_VEH(self, folderName, folderDescription):
        self.kw.write_folder_header(folderName, folderDescription)
        for pswf in self.pswfStateChanges:
            self.kw.write_pswf_placemark(pswf.new_pswf,pswf.new_pswf.value,
                                         pswf.old_pswf.value + "->" + pswf.new_pswf.value, pswf.gps_coord)
        self.kw.write_folder_footer()

    def parse_NAV_GPS1(self, all_tokens):
        '''
        * NAV_GPS1 -> 34A, ST_LONG_NAVI=bytes0-3, ST_LAT_NAVI=bytes4-7
        '''
        stringLong = all_tokens[9] + all_tokens[8] + all_tokens[7] + all_tokens[6]
        decimalLong = int(stringLong, 16)
        stringLat = all_tokens[13] + all_tokens[12] + all_tokens[11] + all_tokens[10]
        decimalLat = int(stringLat, 16)

        gps = GPSCoordinate(decimalLong, decimalLat)
        dist = GPSCoordinate.distance(gps, self.currentGPS)
        #print("dist ", dist)
        self.currentGPS = gps
        #print(gps.latitude)

        if gps.is_valid and dist > 0.0001:
            self.gpsCoordinates.append(gps)
            # kw.writeGPSPlacemark(str(num), "", gpsCoord)

    def parse_NAVGRPH_2_PRES_SEG(self, all_tokens):
        '''
        * NAVGRPH_2_PRES_SEG -> 43D, IDX_PRES_SEG_NAVGRPH_2=byte0, L_SEG_NAVGRPH_2=byte5
        '''
        stringIdx = all_tokens[6]
        decimalIdx = int(stringIdx, 16)

        stringLen = all_tokens[11]
        decimalLen = int(stringLen, 16)

        stringLimV = all_tokens[13]
        decimalLimV = int(stringLimV, 16)
        decimalLimV = decimalLimV >> 3
        decimalLimV &= 31
        decimalLimV *= 5

        if decimalIdx != self.lastIdx:
            self.lastIdx = decimalIdx
            if self.currentGPS.is_valid:
                self.adasSegments.append(ADASSegment(decimalIdx, decimalLen, decimalLimV, self.currentGPS))
                # kw.writePRES_SEGPlacemark(str(decimalIdx), "idx[" + str(decimalIdx) + "] " + "len[" + str(decimalLen) + "] ", currentGPS)

    def parse_CON_VEH(self, all_tokens):
        '''
        * CON_VEH -> 3C, ST_CON_VEH=byte6, bits0-3
        '''
        stringConVeh = all_tokens[12]
        decimalConVeh = int(stringConVeh, 16)
        # mask out the highest 4 bits
        decimalConVeh &= 15

        pswf = PSWF.INVALID
        if decimalConVeh in [1, 2]:
            pswf = PSWF.PARKEN
        elif decimalConVeh == 3:
            pswf = PSWF.STDFKT
        elif decimalConVeh == 5:
            pswf = PSWF.WOHNEN
        elif decimalConVeh == 7:
            pswf = PSWF.PAD
        elif decimalConVeh in [8, 10, 12]:
            pswf = PSWF.FAHREN

        if pswf != self.lastPswf and self.currentGPS.is_valid:
            pswfChange = PSWFStateChange()
            pswfChange.gps_coord = self.currentGPS
            pswfChange.old_pswf = self.lastPswf
            pswfChange.new_pswf = pswf
            self.lastPswf = pswf
            self.pswfStateChanges.append(pswfChange)
            # kw.writePSWFPlacemark(pswf, pswf.label, "", currentGPS);

    def parse_STAT_ZV_KLAPPEN(self, all_tokens):
        '''
        * STAT_ZV_KLAPPEN -> 2FC, ST_CT_BTL=byte2, bits0-1
        '''
        stringStatBtl = all_tokens[8]
        decimalStatBtl = int(stringStatBtl, 16)

        # mask out the highest 4 bits
        decimalStatBtl &= 3

        prop = VehicleProperties()
        if decimalStatBtl == 0:
            # print("heckklappe zu")
            self.currentVehicleProperties.trunk = LIDSTATE.LidState.CLOSED
        if decimalStatBtl == 1:
            # print("heckklappe auf")
            pass
