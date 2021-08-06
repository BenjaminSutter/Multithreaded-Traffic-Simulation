# Multithreaded Traffic Simulation
Class project for Object-Oriented and Concurrent Programming

The project is a GUI based application that primarily consists of three thread-based classes.
* TimeThread - Keeps track of the time and increments every second.
* IntersectionThread - Simulates a traffic light and toggles between green, yellow and red.
* CarThread - Travels at a speed specified in the constructor. If it ever reaches an intersection with a red light it immediately stops.

New cars and intersections can be added using the buttons in the GUI. The simulation can also be paused/resumed or stopped at any time if the corresponding buttons are pressed.