
class ADASSegment:
    def __init__(self, i, dca, rad, lan, st, len, limv, time, gps):
        self.idx = i
        self.raw_dir_ch_angle = dca
        self.raw_rad = rad
        self.lanes = lan
        self.street_type = st
        self.length = len
        self.lim_v = limv
        self.time = time
        self.gps_coord = gps

    def __str__(self):
        return self.print_data()

    def print_data(self):
        return f"idx[{self.print_fena_ffinv(self.idx)}] " \
               f"dca[{self.print_dca(self.raw_dir_ch_angle)}] " \
               f"rad[{self.print_rad(self.raw_rad)}] " \
               f"lan[{self.print_lanes(self.lanes)}] " \
               f"st[{self.street_type}] " \
               f"len[{self.print_fena_ffinv(self.length)}] " \
               f"limv[{self.print_lim_v(self.lim_v)}] " \
               f"time[{self.time}]"

    @staticmethod
    def print_fena_ffinv(val):
        if val == 254:
            return "n.a."
        if val == 255:
            return "inv."
        return str(val)

    def print_dca(self, dca):
        if dca == 4094:
            self.dir_ch_angle = None
            return "n.a."
        if dca == 4095:
            self.dir_ch_angle = None
            return "inv."
        self.dir_ch_angle = float(dca) * 0.08793356 - 180.0
        return f"{self.dir_ch_angle:.1f}"

    def print_rad(self, rad):
        if rad == 4093:
            self.rad = None
            return "straight"
        if rad == 4094:
            self.rad = None
            return "n.a."
        if rad == 4095:
            self.dir_ch_angle = None
            return "inv."
        self.rad = rad * 5 - 10230
        return str(self.rad)

    @staticmethod
    def print_lanes(lan):
        if lan == 0:
            return ">5"
        if lan == 6:
            return "n.a."
        if lan == 7:
            return "inv."
        return str(lan)

    @staticmethod
    def print_lim_v(limv):
        if limv == 0:
            return "unlim"
        if limv == 150:
            return "n.a."
        if limv == 155:
            return "inv."
        return str(limv)
