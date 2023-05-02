/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asc2kml;

/**
 *
 * @author Q262579
 */
public class PositionFingerprint {
    public PositionFingerprint(int seg, int landm, int odo) {
        numSegments = seg;
        segmentHistory = new ADASSegment[numSegments];
        for (ADASSegment s : segmentHistory)
            s = null;
    }
    
    public void addSegment(ADASSegment segment) {
        if (pos < numSegments)
            segmentHistory[pos++] = new ADASSegment(segment);
    }
    
    public String listSegmentHistorySimple() {
        String result = new String();
        for (ADASSegment s : segmentHistory)
            if (s != null)
                result += s.listIndex() + "\n";
        return result;
    }
    
    public String listSegmentHistoryDetailed() {
        String result = new String();
        for (ADASSegment s : segmentHistory)
            if (s != null)
                result += s.listAllData() + "\n";
        return result;
    }

    private int pos = 0;
    private final int numSegments;
    // the history contains the trace of segments lading to the final (parking) position
    public ADASSegment[] segmentHistory;
    
    public Landmarks[]  landmarks;
    public VehicleOdometry[] odometries;
}
