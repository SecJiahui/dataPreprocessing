/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asc2kml;

import java.lang.reflect.Array;

/**
 *
 * @author Q262579
 */
class AveragedBuffer<T extends Number> {
    public AveragedBuffer(Class<T> clazz, int s) {
        size = s;
        position = 0;
        buffer = (T[])Array.newInstance(clazz, size);
    }
    private T add(T t1, T t2) {        
        if (t1 instanceof Double && t2 instanceof Double) {            
            return (T) Double.valueOf((t1.doubleValue() + t2.doubleValue()));
        }
        else if (t1 instanceof Float && t2 instanceof Float) {
            return (T) Float.valueOf(((t1.floatValue() + t2.floatValue())));
        }
        else if (t1 instanceof Integer && t2 instanceof Integer) {
            return (T) Integer.valueOf(((t1.intValue() + t2.intValue())));
        }
        // throw an exception if type unknown
        throw new IllegalArgumentException();
    }
    private T divide(T t1, T t2) {        
        if (t1 instanceof Double && t2 instanceof Double) {
            return (T) Double.valueOf((t1.doubleValue() / t2.doubleValue()));            
        }
        else if (t1 instanceof Float && t2 instanceof Float) {
            return (T) Float.valueOf(((t1.floatValue() / t2.floatValue())));
        }
        else if (t1 instanceof Integer && t2 instanceof Integer) {
            return (T) Integer.valueOf(((t1.intValue() / t2.intValue())));
        }
        // throw an exception if type unknown
        throw new IllegalArgumentException();
    }
    public T getAverage(T init, T divisor) {
        T average = init;
        for (int i = 0; i < size; i++) {
            average = add(average, buffer[i]);
        }        
        return divide(average, divisor);
    }
    public void printBuffer() {
        for (int i = 0; i < size; i++) {
            System.out.println("buf[" + i + "]=" + buffer[i]);
        }
        System.out.println();
    }
    public boolean pushedToFull(T val) {
        if (position < size) {
            //System.out.println(val + " accepted");
            buffer[position++] = val;            
            return false;
        }
        else {
            //System.out.println(val + " rejected");
            position = 0;
            return true;
        }
    }
    public final int size;
    private int position;    
    private final T[] buffer;
    
    /*
    public static void main(String[] args) {
        int BUF_SIZE = 5;
        AveragedBuffer<Double> doubleAB = new AveragedBuffer<>(Double.class, BUF_SIZE);
        Double adD = doubleAB.add(1.0, 1.1); System.out.println("adD = " + adD);
        Double dvD = doubleAB.divide(2.0, 0.5); System.out.println("dvD = " + dvD);
        for (int i = 0; i < 17; i++) {
            System.out.println("pushing " + i + " to buffer");
            if (doubleAB.pushedToFull(Double.valueOf(i))) {
                doubleAB.printBuffer();
                System.out.println("avg = " + doubleAB.getAverage(0.0, Double.valueOf(BUF_SIZE)));
                doubleAB.pushedToFull(Double.valueOf(i));                
            }            
        }
        doubleAB.printBuffer();
        
        AveragedBuffer<Integer> integerAB = new AveragedBuffer<>(Integer.class, BUF_SIZE);
        Integer adI = integerAB.add(1, 2); System.out.println("adI = " + adI);
        Integer dvI = integerAB.divide(4, 2); System.out.println("dvI = " + dvI);
        for (int i = 0; i < 17; i++) {
            System.out.println("pushing " + i + " to buffer");
            if (integerAB.pushedToFull(Integer.valueOf(i))) {
                integerAB.printBuffer();
                System.out.println("avg = " + integerAB.getAverage(0, Integer.valueOf(BUF_SIZE)));
                integerAB.pushedToFull(Integer.valueOf(i));                
            }            
        }
        integerAB.printBuffer();
    }
    */
}
