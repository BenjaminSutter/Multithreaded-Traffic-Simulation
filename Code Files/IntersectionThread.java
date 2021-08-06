/*
 * File: IntersectionThread.java
 * Author: Ben Sutter
 * Date: May 4th, 2021
 * Purpose: Holds methods to imitate a traffic light at an interesction
 * By using threads, multiple intersections can be active at once.
 */

import java.util.Collections;
import java.util.Random;
import javax.swing.JTable;

public class IntersectionThread extends Project3Thread {

    int tableIndex;//Determines which row should be updated
    int xPos;//Position of the light
    int timer;//Timer counts down until the next light change
    boolean needNextTimer = true;//Assigns new timer when true
    JTable table;//Where the updates should be reflected
    //Starts the program as red because it will be updated to green right away in run()
    String currentLightColor = "Red";
    Random random = new Random();//Adding randomness to red and green light duration

    public IntersectionThread(JTable table, int tableIndex, int xPos) {
        this.table = table;
        this.tableIndex = tableIndex;
        this.xPos = xPos;
        numberOfIntersections++;//Increase the total number of intersections
        table.setValueAt(xPos, tableIndex, 1);
        sortedIntersections.add(xPos);
        Collections.sort(sortedIntersections);
    }

    private void lightCountdown() {
        try {
            /*Decreases the time every half second.I could have done every second and then halved 
            the light timers. But I wanted the timers to retain more time during pauses.*/
            timer--;
            //When timer reaches 0, request new timer
            if (timer == 0) {
                needNextTimer = true;
            }
            sleep(500);//Sleep for half a second
        } catch (InterruptedException e) {

        }
    }

    private void updateNewLightTimer() {

        //Since the countdown happens every half second, these values are halved
        if (currentLightColor.equals("Green")) {
            //For green light, set timer to 8-15 seconds.
            timer = random.nextInt(30 + 1 - 16) + 16;
        } else if (currentLightColor.equals("Yellow")) {
            //For yellow light, set timer to a flat 6 seconds
            timer = 6;
        } else if (currentLightColor.equals("Red")) {
            //For red light, set timer to 8-10 seconds. (Longer to make stopping easier to follow)
            timer = random.nextInt(20 + 1 - 16) + 16;
        }
        needNextTimer = false;//No need for new timer since it was just set

    }

    @Override
    public void run() {

        while (programIsActive) {

            while (!isPaused && programIsActive) {

                if (!needNextTimer) {
                    lightCountdown();//If no new timer is needed then count down
                } else {
                    //If the countdown reaches zero, determine next light
                    if (currentLightColor.equals("Green")) {
                        currentLightColor = "Yellow";
                    } else if (currentLightColor.equals("Yellow")) {
                        currentLightColor = "Red";
                    } else if (currentLightColor.equals("Red")) {
                        currentLightColor = "Green";
                    }
                    //Update light in hastable
                    intersectionValues.put(xPos, currentLightColor);
                    //Update current light in the table
                    table.setValueAt(currentLightColor, tableIndex, 2);
                    //Assign a new timer
                    updateNewLightTimer();
                }

            }

        }
    }

}
