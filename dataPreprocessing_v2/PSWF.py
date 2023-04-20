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
        self.old_pswf = None
        self.new_pswf = None
