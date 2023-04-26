from RingBufferHistory import RingbufferHistory
import ADASSegment

class PositionFingerprint:
    def __init__(self, seg, landm, odo):
        self.numSegments = seg
        self.segmentHistory = [None] * self.numSegments
        self.pos = 0

    def add_segment(self, segment):
        if self.pos < self.numSegments:
            self.segmentHistory[self.pos] = segment
            self.pos += 1

    def list_segment_history(self):
        result = ""
        for s in self.segmentHistory:
            if s != None:
                result += s.print_data() + "\n"
        return result
