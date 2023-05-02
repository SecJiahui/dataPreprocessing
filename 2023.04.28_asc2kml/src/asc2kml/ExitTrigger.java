/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asc2kml;

import java.util.ArrayList;

/**
 *
 * @author Q262579
 */
public class ExitTrigger {
    public ExitTrigger(PSWFStateChange pswfsc, int seg, int landm, int odo, long rt) {
        pswfChange = new PSWFStateChange(pswfsc);
        posFingerprint = new PositionFingerprint(seg, landm, odo);
        vehProperties = new ArrayList<>();
        reltime = rt;
    }
    public String listVehicleProperties() {
        String result = "";
        for (VehicleProperties vP : vehProperties) {
            result += vP.listVehProp() + "\n";            
        }
        return result;
    }
    public long reltime;
    public PSWFStateChange pswfChange;
    public PositionFingerprint posFingerprint;
    public ArrayList<VehicleProperties> vehProperties;
}
