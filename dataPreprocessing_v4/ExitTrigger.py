from PSWF import PSWFStateChange
from PositionFingerprint import PositionFingerprint


class ExitTrigger:
    def __init__(self, seg, landm, odo):
        self.pswf_change = PSWFStateChange()
        self.pos_fingerprint = PositionFingerprint(seg, landm, odo)