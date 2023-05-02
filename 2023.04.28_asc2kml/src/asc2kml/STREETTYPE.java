/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asc2kml;

/**
 *
 * @author Q262579
 */
public enum STREETTYPE {
    TRCK("Track"),
    CALM2("Traffic calming zone bi-directional"),
    CALM1("Traffic calming zone one-way"),
    RES2("Residential area bi-directional"),
    RES1("Residential area one-way"),
    URBN2("Urban bi-directional"),
    URBN1("Urban one-way"),
    URBNPHDIV("Urban physical divider"),
    URBNMW("Urban motorway"),
    URBNMWPHDIV("Urban motorway physical divider"),
    RURAL("Rural"),
    RURALPHDIV("Rural physical divider"),
    HIWAY("Highway"),
    HIWAYPHDIV("Highway physical divider"),
    HIWAYENTRY("Highway entry"),
    HIWAYEXIT("Highway exit"),
    HIWAYENEX("Highway entry-exit"),
    MOWAY("Motorway"),
    MOWAYEN("Motorway entry"),
    MOWAYEX("Motorway exit"),
    MOWAYENEX("Motorway entry-exit"),
    FERRY("Ferry"),
    RACE("Racetrack"),
    NA("Not available"),    
    INV("Invalid");

    public final String label;
    
    public static STREETTYPE fromInteger(int x) {
        switch(x) {
            case 0 -> { return TRCK; }
            case 1 -> { return CALM2; }
            case 2 -> { return CALM1; }
            case 3 -> { return RES2; }
            case 4 -> { return RES1; }
            case 5 -> { return URBN2; }
            case 6 -> { return URBN1; }
            case 7 -> { return URBNPHDIV; }
            case 8 -> { return URBNMW; }
            case 9 -> { return URBNMWPHDIV; }
            case 10 -> { return RURAL; }
            case 11 -> { return RURALPHDIV; }
            case 12 -> { return HIWAY; }
            case 13 -> { return HIWAYPHDIV; }
            case 14 -> { return HIWAYENTRY; }
            case 15 -> { return HIWAYEXIT; }
            case 16 -> { return HIWAYENEX; }
            case 17 -> { return MOWAY; }
            case 18 -> { return MOWAYEN; }
            case 19 -> { return MOWAYEX; }
            case 20 -> { return MOWAYENEX; }
            case 21 -> { return FERRY; }
            case 22 -> { return RACE; }
            case 30 -> { return NA; }
            case 31 -> { return INV; }
        }
        return null;
    }
    
    STREETTYPE(String label) {
        this.label = label;
    }
}
