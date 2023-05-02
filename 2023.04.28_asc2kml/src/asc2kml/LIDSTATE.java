/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asc2kml;

/**
 *
 * @author Q262579
 */
public enum LIDSTATE {
    OPEN("Open"),
    CLOSED("Closed"),
    INTERMEDIATE("Intermediate"),
    INVALID("Invalid");

    public final String label;

    LIDSTATE(String label) {
        this.label = label;
    }
}
