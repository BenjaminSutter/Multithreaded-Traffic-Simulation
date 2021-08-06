/*
 * File: CarThread.java
 * Author: Ben Sutter
 * Date: May 4th, 2021
 * Purpose: Holds methods to imitate a car in traffic.
 * By using threads, multiple cars can be active and perform their checks at once.
 */

import javax.swing.JTable;

public class CarThread extends Project3Thread {

    int speed;//How fast the car goes (Kilometers per hour)
    int currentDistance;//Current x position of the car
    int metersPerSecond;//Determines how often to increment the Jtable
    int nextIntersection;//Determines when the car should check for a red light
    int recognizedIntersections;//Helps keep track if new intersections are added.
    int tableIndex;//Determines which row should be updated
    JTable table;//Where the updates should be reflected
    private volatile boolean atRedLight;//Signifies when cars should stop
    private volatile boolean atGreenLight = true;//Help reduces number of table updates
    private volatile boolean passedLastLight = false;
    boolean needNextIntersection = false;//Used when a car passes an intersection

    public CarThread(JTable table, int tableIndex, int speed) {
        this.table = table;
        this.tableIndex = tableIndex;
        this.speed = speed;
        this.metersPerSecond = (int) (speed / 3.6);
        //The next intersection will always be the first (array gets sorted in IntersectionThread)
        nextIntersection = sortedIntersections.get(0);
        //Initializes number of recognized intersections
        recognizedIntersections = numberOfIntersections;
    }

    //Whenever thisis called, currentDistance is incremented. If intersection is passed then look for new one.
    public void increaseDistance() {
        currentDistance += 1;
        if (currentDistance > nextIntersection) {
            needNextIntersection = true;
        }
    }

    public void updateNextIntersection() {
        //If a new intersection is needed (car has passed previous) then find the next closes
        if (needNextIntersection) {
            //If a new intersection is needed, check all intersections for next
            for (int i : sortedIntersections) {
                if (currentDistance < i) {
                    nextIntersection = i;
                    needNextIntersection = false;
                    return;//If interesction is found no need to continue loop
                }
            }
            //If it makes it past the for loop that means that the car passed all intersections so it should stop
            table.setValueAt("Finished", tableIndex, 2);
            passedLastLight = true;
        } else if (recognizedIntersections < numberOfIntersections) {
            /*If there are less recognized intersections than total intersections (new intersection was added to array)
            Then see if the new intersection is closer than the current next closest intersection */
            for (int i : sortedIntersections) { //Check each intersection
                //If the intersection is a head of the car and the intersection is closer than the previous than update
                if (currentDistance < i && i < nextIntersection) {
                    nextIntersection = i;//Update next intersection
                    //Update recognized intersections to reflect total intersections
                    recognizedIntersections = numberOfIntersections;//Increase recognizedIntersections
                    return;//Break out early if needed
                }
            }
        }
    }

    public void currentLightCheck() {
        //If thread is paused then update table
        if (isPaused) {
            //Makes it easy to note that the car is paused
            table.setValueAt("Paused", tableIndex, 2);
        } else if (atGreenLight && intersectionValues.get(nextIntersection).equals("Red")
                && currentDistance > nextIntersection - 5 && currentDistance < nextIntersection) {
            //If car thinks current light is green light and the next intersection is red, update accordingly
            atRedLight = true;
            atGreenLight = false;
            //Make it easy to note that the car is stopped
            table.setValueAt("Stopped (red)", tableIndex, 2);
        } else if (atRedLight && intersectionValues.get(nextIntersection).equals("Green")) {
            //If the car thinks the light is red but it is really green then update
            atGreenLight = true;
            atRedLight = false;
        } else if (!isPaused && !atRedLight && programIsActive && !passedLastLight) {
            //Update the table with speed again (so it doesn't say paused or stopped (red))
            table.setValueAt(speed, tableIndex, 2);
        } else if (currentDistance > nextIntersection - 5 && currentDistance < nextIntersection &&
                !isPaused && programIsActive && !passedLastLight) {
            //This is only in effect when the program is resumed at a red light so it doesn't say "Paused"
            table.setValueAt("Stopped (red)", tableIndex, 2);
        }
    }

    @Override
    public void run() {
        //While running, constantly update current time
        while (programIsActive && !passedLastLight) {

            try {
                //If any of these booleans become true then break out of loop
                while (programIsActive && !isPaused && !atRedLight && !passedLastLight) {
                    updateNextIntersection();//Constantly check for new intersections
                    currentLightCheck();//See if the car should stop
                    increaseDistance();//Increase distance
                    //Update table with current distance
                    table.setValueAt(currentDistance, tableIndex, 1);
                    /*Sleeps to control how often the current distance is incremented
                    Math is as follows: If thread is going 20 meters a second, it should update 20 times a a second
                    This means 1000/20 = 50 so it should sleep every 50 nanoseconds*/
                    Thread.sleep(1000 / metersPerSecond);
                }
                //Need to call this outside of while loop so info gets updated after reds.
                currentLightCheck();

            } catch (InterruptedException e) {
                System.out.println("SHOULD BE DONE SON");
            }
        }
        System.out.println("Thread stopped");
    }

}
