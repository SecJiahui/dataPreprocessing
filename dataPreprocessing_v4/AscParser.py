import zipfile
import datetime
from ADASSegment import ADASSegment
from ExitTrigger import ExitTrigger
from GPSCoordinate import GPSCoordinate
from PSWF import PSWFStateChange, PSWF
from StreetType import StreetType
from VehicleProperties import VehicleProperties


class AscParser:
    def __init__(self, k_w):
        self.kw = k_w
        self.current_gps = GPSCoordinate(2147483647, 2147483647, 0)
        self.current_vehicle_properties = VehicleProperties()
        self.gps_coordinates = []
        self.current_segment = None
        self.adas_segments = []
        self.pswf_state_changes = []
        self.vehicle_properties = []
        self.ADAS_HISTORY_SIZE = 10
        self.ADAS_HISTORY_POSITION = 0
        self.adas_history = [None] * self.ADAS_HISTORY_SIZE
        self.exit_triggers = []
        self.lastIdx = -1
        self.last_pswf = PSWF.INVALID
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
        last_time = None
        for read in file:
            read = read.rstrip('\n')
            tokens = read.split(" ")

            for i in range(len(tokens) - 1):
                if tokens[i].upper() == '34A':
                    # GPS
                    self.parse_NAV_GPS1(tokens, last_time)
                elif tokens[i].upper() == '2F8':
                    # time
                    last_time = self.parse_time(tokens)
                elif tokens[i].upper() == '43D':
                    # ADAS Segment
                    self.parse_NAVGRPH_2_PRES_SEG(tokens, last_time)
                elif tokens[i].upper() == '3C':
                    # PWSF condition
                    if i < len(tokens) - 2:
                        if tokens[i + 1].upper() == 'RX':
                            self.parse_CON_VEH(tokens, last_time)
                elif tokens[i].upper() == '2FC':
                    # STAT_ZV_KLAPPEN
                    self.parse_stat_zv_klappen(tokens)
                elif tokens[i] == '3B6':
                    # driver door window
                    self.parse_ST_PO_WRG_ALL(tokens, 'DR')
                elif tokens[i] == '3B7':
                    self.parse_ST_PO_WRG_ALL(tokens, 'DRR')
                elif tokens[i] == '3B8':
                    self.parse_ST_PO_WRG_ALL(tokens, 'PS')
                elif tokens[i] == '3B9':
                    self.parse_ST_PO_WRG_ALL(tokens, 'PSR')

    def write_gps(self, folderName, folderDescription):
        self.kw.write_folder_header(folderName, folderDescription)
        for gps in self.gps_coordinates:
            self.kw.write_gps_placemark(str(self.num), "time[" + str(gps.time) + "] ", gps)
            self.num += 1
        self.kw.write_folder_footer()

    def write_pres_seg(self, folder_name, folder_description):
        self.kw.write_folder_header(folder_name, folder_description)
        for seg in self.adas_segments:
            self.kw.write_pres_seg_placemark(str(seg.idx), seg.print_data(), seg.gps_coord)
        self.kw.write_folder_footer()

    def write_segment_path(self, folderName, folderDescription):
        self.kw.write_folder_header(folderName, folderDescription)
        self.kw.write_line_string_header("1", "relativeToGround", "1")
        for seg in self.adas_segments:
            self.kw.write_line_string_coordinate(seg.gps_coord.longitude, seg.gps_coord.latitude)
        self.kw.write_line_string_footer()
        self.kw.write_folder_footer()

    def write_entry_exit(self, folder_name, folder_description):
        self.kw.write_folder_header(folder_name, folder_description)
        for pswf in self.pswf_state_changes:
            # check if we are in an entry scenario
            if pswf.old_PSWF == PSWF.PARKEN and pswf.new_PSWF == PSWF.STDFKT:
                self.kw.write_entry_exit_placemark("Enter",
                                                   f"{pswf.old_PSWF.value}->{pswf.new_PSWF.value} at {pswf.change_time}",
                                                   "Entry",
                                                   pswf.gps_coord)
            # check if we are in an exit scenario
            if pswf.old_PSWF == PSWF.STDFKT and pswf.new_PSWF == PSWF.PARKEN:
                self.kw.write_entry_exit_placemark("Exit",
                                                   f"{pswf.old_PSWF.value}->{pswf.new_PSWF.value} at {pswf.change_time}",
                                                   "Exit",
                                                   pswf.gps_coord)
        self.kw.write_folder_footer()

    def write_exit_history(self, folder_name, folder_description):
        self.kw.write_folder_header(folder_name, folder_description)
        for etr in self.exit_triggers:
            self.kw.write_exit_history_placemark("Exit",
                                            etr.pswf_change.old_PSWF.value + "->" + etr.pswf_change.new_PSWF.value + "\n" + etr.pos_fingerprint.list_segment_history(),
                                            etr.pswf_change.gps_coord)
        self.kw.write_folder_footer()

    def write_con_veh(self, folderName, folderDescription):
        self.kw.write_folder_header(folderName, folderDescription)
        for pswf in self.pswf_state_changes:
            self.kw.write_pswf_placemark(pswf.new_PSWF, pswf.new_PSWF.value,
                                         f"{pswf.old_PSWF.value}->{pswf.new_PSWF.value} at {pswf.change_time}", pswf.gps_coord)
        self.kw.write_folder_footer()

    def write_lids_flaps(self, folder_name, folder_description):
        self.kw.write_folder_header(folder_name, folder_description)
        for prop in self.vehicle_properties:
            self.kw.write_lids_flaps_placemark("L/F", prop.get_list_veh_prop(), prop.gps_coord)
            print(prop.get_list_veh_prop())
        self.kw.write_folder_footer()

    def parse_NAV_GPS1(self, all_tokens, last_time):
        '''
        * NAV_GPS1 -> 34A, ST_LONG_NAVI=bytes0-3, ST_LAT_NAVI=bytes4-7
        '''
        stringLong = all_tokens[9] + all_tokens[8] + all_tokens[7] + all_tokens[6]
        decimalLong = int(stringLong, 16)
        stringLat = all_tokens[13] + all_tokens[12] + all_tokens[11] + all_tokens[10]
        decimalLat = int(stringLat, 16)

        gps = GPSCoordinate(decimalLong, decimalLat, last_time)
        dist = GPSCoordinate.distance(gps, self.current_gps)
        #print("dist ", dist)
        self.current_gps = gps
        #print(gps.latitude)

        if gps.is_valid and dist > 0.0001:
            self.gps_coordinates.append(gps)
            # kw.writeGPSPlacemark(str(num), "", gpsCoord)

    def parse_NAVGRPH_2_PRES_SEG(self, all_tokens, last_time):
        """
        NAVGRPH_2_PRES_SEG -> 43D, IDX_PRES_SEG_NAVGRPH_2=byte0, L_SEG_NAVGRPH_2=byte5
        """
        string_val = all_tokens[6]
        decimal_idx = int(string_val, 16)

        string_val = all_tokens[8]
        decimal_dir_ch_angle_p1 = 15 & int(string_val, 16)
        decimal_dir_ch_angle_p1 <<= 8
        string_val = all_tokens[7]
        decimal_dir_ch_angle_p2 = int(string_val, 16)
        decimal_dir_ch_angle_p2 |= decimal_dir_ch_angle_p1

        string_val = all_tokens[8]
        decimal_rad_p1 = 240 & int(string_val, 16)
        decimal_rad_p1 >>= 4
        string_val = all_tokens[9]
        decimal_rad_p2 = int(string_val, 16)
        decimal_rad_p2 <<= 4
        decimal_rad_p2 |= decimal_rad_p1

        string_val = all_tokens[10]
        decimal_lanes = int(string_val, 16)
        decimal_lanes &= 7

        string_val = all_tokens[10]
        decimal_street_type = int(string_val, 16)
        decimal_street_type &= 248
        decimal_street_type >>= 3
        st = StreetType.from_integer(decimal_street_type)

        string_val = all_tokens[11]
        decimal_length = int(string_val, 16)

        string_val = all_tokens[13]
        decimal_lim_v = int(string_val, 16)
        decimal_lim_v >>= 3
        decimal_lim_v &= 31
        decimal_lim_v *= 5

        if decimal_idx != self.lastIdx:
            self.lastIdx = decimal_idx
            if self.current_gps.is_valid:  # Assuming your GPSCoordinate class has a method named is_valid
                self.current_segment = ADASSegment(decimal_idx, decimal_dir_ch_angle_p2, decimal_rad_p2, decimal_lanes,
                                                   st, decimal_length, decimal_lim_v, last_time, self.current_gps)
                self.adas_segments.append(self.current_segment)
                self.adas_history[self.ADAS_HISTORY_POSITION % self.ADAS_HISTORY_SIZE] = self.current_segment
                self.ADAS_HISTORY_POSITION += 1
                # print(adas_history.List())
                # self.kw.writePRES_SEGPlacemark(str(decimal_idx), f"idx[{decimal_idx}] len[{decimal_length}] ", self.current_gps)

    def parse_CON_VEH(self, all_tokens, last_time):
        """
        CON_VEH -> 3C, ST_CON_VEH=byte6, bits0-3
        """
        string_con_veh = all_tokens[12]
        decimal_con_veh = int(string_con_veh, 16)
        # mask out the highest 4 bits
        decimal_con_veh &= 15

        pswf = PSWF.INVALID
        if decimal_con_veh in (1, 2):
            pswf = PSWF.PARKEN
        elif decimal_con_veh == 3:
            pswf = PSWF.STDFKT
        elif decimal_con_veh == 5:
            pswf = PSWF.WOHNEN
        elif decimal_con_veh == 7:
            pswf = PSWF.PAD
        elif decimal_con_veh in (8, 10, 12):
            pswf = PSWF.FAHREN

        if pswf != self.last_pswf and self.current_gps.is_valid:
            pswf_change = PSWFStateChange()
            pswf_change.change_time = last_time
            pswf_change.gps_coord = self.current_gps
            pswf_change.old_PSWF = self.last_pswf
            pswf_change.new_PSWF = pswf

            # check if we are in an entry scenario PARKEN -> STANDFUNKTIONEN
            if pswf_change.old_PSWF == PSWF.PARKEN and pswf_change.new_PSWF == PSWF.STDFKT:
                # do we want to have a "forward directed history"?
                pass

            # check if we are in an exit scenario STANDFUNKTIONEN -> PARKEN
            if pswf_change.old_PSWF == PSWF.STDFKT and pswf_change.new_PSWF == PSWF.PARKEN:

                exit_trigger = ExitTrigger(self.ADAS_HISTORY_SIZE, 0, 0)
                exit_trigger.pswf_change = pswf_change
                print("history size", len(self.adas_history))
                for aH in self.adas_history:
                    if aH is not None:
                        exit_trigger.pos_fingerprint.add_segment(aH)
                self.exit_triggers.append(exit_trigger)

                print("exit trace")
                print(exit_trigger.pos_fingerprint.list_segment_history())

                # reset adas history ringbuffer position
                self.ADAS_HISTORY_POSITION = 0

            self.last_pswf = pswf
            self.pswf_state_changes.append(pswf_change)
            # kw.writePSWFPlacemark(pswf, pswf.label, "", current_gps)

    def parse_ST_PO_WRG_ALL(self, all_tokens, win_type):
        """
        POSITION_FH_FAT -> 3B6, ST_PO_WRG_DRD=byte1, bits0-1
        POSITION_FH_FATH -> 3B7, ST_PO_WRG_DVDR=byte1, bits0-1
        POSITION_FH_BFT -> 3B8, ST_PO_WRG_PSD=byte1, bits0-1
        POSITION_FH_BFTH -> 3B9, ST_PO_WRG_PSDR=byte1, bits0-1
        """
        prop = VehicleProperties(self.current_vehicle_properties)

        string_stat_win = all_tokens[7]
        decimal_stat_win = int(string_stat_win, 16)
        # mask out the highest 6 bits
        decimal_stat_win &= 3
        if win_type == 'DR':
            prop.window_DRD = VehicleProperties.set_window_state(decimal_stat_win, 'DR')
        elif win_type == 'DRR':
            prop.window_DRDR = VehicleProperties.set_window_state(decimal_stat_win, 'DRR')
        elif win_type == 'PS':
            prop.window_PSD = VehicleProperties.set_window_state(decimal_stat_win, 'PS')
        elif win_type == 'PSR':
            prop.window_PSDR = VehicleProperties.set_window_state(decimal_stat_win, 'PSR')
        else:
            print("no win type specified")

        prop.gps_coord = self.current_gps

        if not VehicleProperties.is_equal(prop, self.current_vehicle_properties):
            self.current_vehicle_properties = prop
            self.vehicle_properties.append(prop)
            print(prop.list_veh_prop())
            print("__________")

    def parse_stat_zv_klappen(self, all_tokens):
        """
        * STAT_ZV_KLAPPEN -> 2FC, ST_CT_BTL=byte2, bits0-1
        """
        prop = VehicleProperties(self.current_vehicle_properties)

        string_stat_btl = all_tokens[8]
        decimal_stat_btl = int(string_stat_btl, 16)
        # mask out the highest 6 bits
        decimal_stat_btl &= 3
        prop.trunk = VehicleProperties.set_bonnet_door_trunk_state(decimal_stat_btl, 'trunk')

        string_stat_bon = all_tokens[8]
        decimal_stat_bon = int(string_stat_bon, 16)
        # shift 2 bits to right and mask out the highest 6 bits
        decimal_stat_bon >>= 2
        decimal_stat_bon &= 3
        prop.bonnet = VehicleProperties.set_bonnet_door_trunk_state(decimal_stat_bon, 'bonnet')

        string_stat_doors = all_tokens[7]
        decimal_stat_doors = int(string_stat_doors, 16)
        # shift the right amount of bits to right and mask out the rest accordingly
        prop.door_DRD = VehicleProperties.set_bonnet_door_trunk_state(decimal_stat_doors & 3, 'DRD')
        prop.door_PSD = VehicleProperties.set_bonnet_door_trunk_state((decimal_stat_doors >> 2) & 3, 'PSD')
        prop.door_DRDR = VehicleProperties.set_bonnet_door_trunk_state((decimal_stat_doors >> 4) & 3, 'DRDR')
        prop.door_PSDR = VehicleProperties.set_bonnet_door_trunk_state((decimal_stat_doors >> 6) & 3, 'PSDR')

        prop.gps_coord = self.current_gps

        if not VehicleProperties.is_equal(prop, self.current_vehicle_properties):
            self.current_vehicle_properties = prop
            self.vehicle_properties.append(prop)

            # print(prop.list_veh_prop())
            # print("__________")

    def parse_time(self, all_tokens):
        '''
        * UHRZEIT_DATUM -> 2F8h, DISP_HR=byte0, DISP_MN=byte1, DISP_SEC=byte2, DISP_DATE_DAY=byte3,
        DISP_DATE_WDAY=Byte3, Bits0-3, DISP_DATE_MON=Byte3, Bits4-7, DISP_DATE_YR=Bytes5-6
        2023.04.20, Thursday, 13:21
        '''
        string_HR = all_tokens[6]
        decimal_HR = int(string_HR, 16)

        string_MN = all_tokens[7]
        decimal_MN = int(string_MN, 16)

        string_SEC = all_tokens[8]
        decimal_SEC = int(string_SEC, 16)

        string_DAY = all_tokens[9]
        decimal_DAY = int(string_DAY, 16)

        string_WDAY = all_tokens[10]
        decimal_WDAY = int(string_WDAY, 16)
        decimal_WDAY &= 15

        string_MON = all_tokens[10]
        decimal_MON = int(string_MON, 16)
        decimal_MON = decimal_MON >> 4
        decimal_MON &= 15

        string_YR = all_tokens[12] + all_tokens[11]
        decimal_YR = int(string_YR, 16)

        dt = datetime.datetime(decimal_YR, decimal_MON, decimal_DAY, decimal_HR, decimal_MN, decimal_SEC)

        # Use the strftime() method to format the datetime object as a string in the specified format
        formatted_string = dt.strftime("%Y.%m.%d, %A, %H:%M:%S")
        return formatted_string
        # print(formatted_string) output format：2023.04.20, Thursday, 13:15.
