/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asc2kml;

/**
 *
 * @author Q262579
 */
public enum PSWF {
    PARKEN("Parken"),
    STDFKT("Standfunktionen"),        
    WOHNEN("Wohnen"),
    PAD("PruefenAnDi"),
    FAHREN("Fahren"),
    INVALID("Invalid");

    public final String label;

    PSWF(String label) {
        this.label = label;
    }
}
