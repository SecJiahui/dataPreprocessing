from enum import Enum


class PSWF(Enum):
    PARKEN = "Parken"
    STDFKT = "Standfunktionen"
    WOHNEN = "Wohnen"
    PAD = "PruefenAnDi"
    FAHREN = "Fahren"
    INVALID = "Invalid"


class PSWFStateChange:
    def __init__(self):
        self.gps_coord = None
        self.old_PSWF = None
        self.new_PSWF = None
        self.change_time = None
