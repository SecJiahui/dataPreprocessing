import LIDSTATE


class VehicleProperties:
    def __init__(self):
        self.doorDRD = LIDSTATE.LidState.UNKNOWN
        self.doorPSD = LIDSTATE.LidState.UNKNOWN
        self.doorDRDR = LIDSTATE.LidState.UNKNOWN
        self.doorPSDR = LIDSTATE.LidState.UNKNOWN

        self.windowDRD = LIDSTATE.LidState.UNKNOWN
        self.windowPSD = LIDSTATE.LidState.UNKNOWN
        self.windowDRDR = LIDSTATE.LidState.UNKNOWN
        self.windowPSDR = LIDSTATE.LidState.UNKNOWN

        self.bonnet = LIDSTATE.LidState.UNKNOWN
        self.trunk = LIDSTATE.LidState.UNKNOWN
