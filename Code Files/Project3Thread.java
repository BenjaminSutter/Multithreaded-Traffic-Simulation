/*
 * File: Project3Thread.java
 * Author: Ben Sutter
 * Date: May 4th, 2021
 * Purpose: Hold static variables that will be used in Car, Time, and Intersection threads.
 */

import java.util.ArrayList;
import java.util.Hashtable;

abstract class Project3Thread extends Thread {

    static volatile boolean isPaused;//Notifies all threads that the simulation is paused
    static boolean programIsActive;//Signals start/end of threads
    static int numberOfIntersections;//Increased whenever an intersection is created

    /* This hashtable uses the x position of each intersection as a key.
    The value stored is the current light (Green/Red). Yellow is irrelevant.*/
    static Hashtable<Integer, String> intersectionValues = new Hashtable<Integer, String>();

    /*This array list is so that the CarThreads can reference what intersection is next */
    static ArrayList<Integer> sortedIntersections = new ArrayList<Integer>();

}
