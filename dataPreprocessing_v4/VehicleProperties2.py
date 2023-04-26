from enum import Enum

class LidState(Enum):
    INVALID = "?"
    CLOSED = "|"
    OPEN = "/"
    INTERMEDIATE = "O"


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

            self.gpsCoord = None
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

            self.gpsCoord = vp.gpsCoord

    def __str__(self):
        return self.get_bonnet() + self.get_row(self.door_DRD, self.window_DRD, self.door_PSD, self.window_PSD) + \
               self.get_row(self.door_DRDR, self.window_DRDR, self.door_PSDR, self.window_PSDR) + self.get_trunk()

    def get_bonnet(self):
        return " +" + ("/--\\" if self.bonnet == LidState.OPEN else "----") + "+\n"

    def get_trunk(self):
        return " +" + ("\\--/" if self.trunk == LidState.OPEN else "----") + "+\n"

    def get_row(self, door_left, window_left, door_right, window_right):
        row = ""
        row += " " if door_left == LidState.OPEN else door_left.value
        row += "O" if window_left == LidState.INTERMEDIATE else window_left.value
        row += "*--*"
        row += "O" if window_right == LidState.INTERMEDIATE else window_right.value
        row += " " if door_right == LidState.OPEN else door_right.value
        row += "\n"
        return row

    @staticmethod
    def get_bonnet_door_trunk_state(signal, lid_flap):
        if signal == 0:
            return LidState.CLOSED
        if signal == 1:
            return LidState.OPEN
        return LidState.INVALID

    @staticmethod
    def get_window_state(signal, win):
        if signal == 0:
            return LidState.CLOSED
        if signal == 1:
            return LidState.INTERMEDIATE
        if signal == 2:
            return LidState.OPEN
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
