from LIDSTATE import LidState


class VehicleProperties:
    def __init__(self, vp=None):
        if vp is None:
            self.door_DRD = LidState.INVALID
            self.door_PSD = LidState.INVALID
            self.door_DRDR = LidState.INVALID
            self.door_PSDR = LidState.INVALID

            self.window_DRD = LidState.INVALID
            self.window_PSD = LidState.INVALID
            self.window_DRDR = LidState.INVALID
            self.window_PSDR = LidState.INVALID

            self.bonnet = LidState.INVALID
            self.trunk = LidState.INVALID

            self.gps_coord = None
        else:
            self.door_DRD = vp.door_DRD
            self.door_PSD = vp.door_PSD
            self.door_DRDR = vp.door_DRDR
            self.door_PSDR = vp.door_PSDR

            self.window_DRD = vp.window_DRD
            self.window_PSD = vp.window_PSD
            self.window_DRDR = vp.window_DRDR
            self.window_PSDR = vp.window_PSDR

            self.bonnet = vp.bonnet
            self.trunk = vp.trunk

            self.gps_coord = vp.gps_coord

    def get_list_veh_prop(self):
        return self.get_bonnet() + self.get_front_row() + self.get_rear_row() + self.get_trunk()

    def get_bonnet(self):
        return " +" + ("/--\\" if self.bonnet == LidState.OPEN else "----") + "+\n"

    def get_front_doors(self):
        return ("/ " if self.door_DRD == LidState.OPEN else " |") + "*--*" + (
            " \\" if self.door_PSD == LidState.OPEN else "|") + "\n"

    def get_front_windows(self):
        return (
            " O" if self.window_DRD == LidState.OPEN or self.window_DRD == LidState.INTERMEDIATE else " |") + "*--*" + (
            "O" if self.window_PSD == LidState.OPEN or self.window_PSD == LidState.INTERMEDIATE else "|") + "\n"

    def get_rear_doors(self):
        return ("/ " if self.door_DRDR == LidState.OPEN else " |") + "*--*" + (
            " \\" if self.door_PSDR == LidState.OPEN else "|") + "\n"

    def get_rear_windows(self):
        return (
            " O" if self.window_DRDR == LidState.OPEN or self.window_DRDR == LidState.INTERMEDIATE else " |") + "*--*" + (
            "O" if self.window_PSDR == LidState.OPEN or self.window_PSDR == LidState.INTERMEDIATE else "|") + "\n"

    def get_trunk(self):
        return " +" + ("\\--/" if self.trunk == LidState.OPEN else "----") + "+\n"

    def get_front_row(self):
        return self.get_row(self.door_DRD == LidState.OPEN,
                            self.window_DRD == LidState.OPEN or self.window_DRD == LidState.INTERMEDIATE,
                            self.window_DRD == LidState.INVALID,
                            self.door_PSD == LidState.OPEN,
                            self.window_PSD == LidState.OPEN or self.window_PSD == LidState.INTERMEDIATE,
                            self.window_PSD == LidState.INVALID)

    def get_rear_row(self):
        return self.get_row(self.door_DRDR == LidState.OPEN,
                            self.window_DRDR == LidState.OPEN or self.window_DRDR == LidState.INTERMEDIATE,
                            self.window_DRDR == LidState.INVALID,
                            self.door_PSDR == LidState.OPEN,
                            self.window_PSDR == LidState.OPEN or self.window_PSDR == LidState.INTERMEDIATE,
                            self.window_PSDR == LidState.INVALID)

    def get_row(self, dDR_o, wDR_o, wDR_u, dPS_o, wPS_o, wPS_u):
        result = ""
        if not dDR_o and not wDR_o:
            if wDR_u:  # show '?' for unknown window state
                result += "?|" + "*--*"
            else:
                result += " |" + "*--*"
        if dDR_o and not wDR_o:
            if wDR_u:  # show '?' for unknown window state
                result += "/?" + "*--*"
            else:
                result += "/ " + "*--*"
        if not dDR_o and wDR_o:
            result += " O" + "*--*"
        if dDR_o and wDR_o:
            result += "( " + "*--*"

        if not dPS_o and not wPS_o:
            if wPS_u:  # show '?' for unknown window state
                return result + "|?" + "\n"
            else:
                return result + "|" + "\n"
        if dPS_o and not wPS_o:
            if wPS_u:  # show '?' for unknown window state
                return result + "?\\" + "\n"
            else:
                return result + " \\" + "\n"
        if not dPS_o and wPS_o:
            return result + "O" + "\n"
        if dPS_o and wPS_o:
            return result + " )" + "\n"

        return " |*--*|\n"

    @staticmethod
    def set_bonnet_door_trunk_state(signal, lid_flap):
        if signal == 0:
            # print(lid_flap + " (lid/flap) closed")
            return LidState.CLOSED
        if signal == 1:
            # print(lid_flap + " (lid/flap) open")
            return LidState.OPEN
        # print(lid_flap + " unknown")
        return LidState.INVALID

    @staticmethod
    def set_window_state(signal, win):
        if signal == 0:
            # print(win + " window closed")
            return LidState.CLOSED
        if signal == 1:
            # print(win + " window intermediate")
            return LidState.INTERMEDIATE
        if signal == 2:
            # print(win + " window open")
            return LidState.OPEN
        # print(win + " window unknown")
        return LidState.INVALID

    @staticmethod
    def is_equal(vp1, vp2):
        return (vp1.door_DRD == vp2.door_DRD and
                vp1.door_PSD == vp2.door_PSD and
                vp1.door_DRDR == vp2.door_DRDR and
                vp1.door_PSDR == vp2.door_PSDR and
                vp1.window_DRD == vp2.window_DRD and
                vp1.window_PSD == vp2.window_PSD and
                vp1.window_DRDR == vp2.window_DRDR and
                vp1.window_PSDR == vp2.window_PSDR and
                vp1.bonnet == vp2.bonnet and
                vp1.trunk == vp2.trunk)
