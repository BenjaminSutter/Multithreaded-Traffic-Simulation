/*
 * File: TimeThread.java
 * Author: Ben Sutter
 * Date: May 4th, 2021
 * Purpose: While the thread is running/not paused display the current time in seconds.
 */

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;

public class TimeThread extends Project3Thread {

    private JLabel label;//Thread will update this label every second

    //Store the date format for easy use
    private SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss a");

    //The thread takes the label it will update each second as a parameter
    public TimeThread(JLabel label) {
        this.label = label;
    }

    //If true then false, if false then true
    public void togglePause() {
        isPaused ^= true;
    }

    //If true then false, if false then true
    public void toggleActive() {
        programIsActive ^= true;
    }

    @Override
    public void run() {
        //While running, constantly update current time
        while (programIsActive) {
            try {
                //If thread is not paused and the program is running then update the label
                while (!isPaused && programIsActive) {
                    Date date = new Date(System.currentTimeMillis());
                    label.setText("Current time: " + time.format(date));
                    Thread.sleep(1000);//Sleeps every second so it only increments in seconds
                }
            } catch (InterruptedException e) {
            }
        }
    }

}
