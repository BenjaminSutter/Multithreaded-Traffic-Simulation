/*
 * File: Project3.java
 * Author: Ben Sutter
 * Date: May 4th, 2021
 * Purpose: GUI was generated using Netbean's GUI builder.
 * All methods (aside from init) were "handmade"
 * Creates a GUI that shows the traffic simulation.
 * Has controls to start, pause/resume, stop, add new a car or intersection.
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class Project3 extends javax.swing.JFrame {

    // Netbeans GUIVariables declaration - do not modify                     
    private javax.swing.JButton addCarButton;
    private javax.swing.JButton addIntersectionButton;
    private javax.swing.JButton pauseButton;
    private javax.swing.JButton stopButton;
    private javax.swing.JButton startButton;

    private javax.swing.JLabel infoLabel;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JLabel speedLabel;
    private javax.swing.JLabel timeLabel;

    private javax.swing.JPanel carsPanel;
    private javax.swing.JPanel intersectionsPanel;
    private javax.swing.JPanel startPanel;

    private javax.swing.JTextField locationTextField;//Textfield for new intersection location
    private javax.swing.JTextField speedTextField;//Textfield for speed of new car

    private javax.swing.JTable intersectionTable;//Displays intersections
    private javax.swing.JTable carTable;//Displays cars
    private javax.swing.JScrollPane carScrollPane;
    private javax.swing.JScrollPane intersectionScrollPane;
    // End of variables declaration | Start other variable declaration

    //Master thread that determines states (paused/stopped) also shows current time
    TimeThread time;
    ArrayList<IntersectionThread> intersectionThreads = new ArrayList();//Holds all IntersectionThreads
    ArrayList<CarThread> carThreads = new ArrayList();//Holds all CarThreads

    boolean hasStarted;
    boolean isPaused;//Used to determine GUI elements (whether pause or resume should be on a button)

    int totalNumberOfCars = 4;//Incremented when new cars are added used to assign cars unique numbers
    int totalNumberOfIntersections = 4;//Incremented when new intersections are added used to assign intersectionss unique numbers

    //Holds starting values of the car table. After a new car is added this doesn't matter anymore.
    Object[][] cars = new Object[][]{
        {"Car 1", 0, 60},
        {"Car 2", 0, 120},
        {"Car 3", 0, 180},};

    //Holds the header for the car table
    String[] carColumnNames = new String[]{
        "Car #", "X Pos", "Speed (KM/H)"
    };

    //Holds starting values of the intersection table. After a new intersection is added this doesn't matter anymore.
    Object[][] intersections = new Object[][]{
        {"Intersection 1", 500, ""},
        {"Intersection 2", 1000, ""},
        {"Intersection 3", 1500, ""}
    };

    //Holds the header for the intersection table
    String[] intersectionColumnNames = new String[]{
        "Intersection", "X Pos (meters)", "Light"
    };

    public Project3() {
        initializeIntersectionTable();
        initComponents();
        initializeStartingThreads();//Initialize the starting threads
    }

    //Creates the time thread, three car threads, and three intersection threads.
    public void initializeStartingThreads() {
        time = new TimeThread(timeLabel);
        //Create three car threads and three intersection threads to start the program
        for (int i = 0; i < 3; i++) {
            intersectionThreads.add(new IntersectionThread(intersectionTable, i, (i * 500) + 500));
            carThreads.add(new CarThread(carTable, i, (i * 60) + 60));
        }
    }

    //Creates the table so that cells are updated with color depending on the current light.
    //Method to change colors from: https://stackoverflow.com/questions/25080951/jtable-set-cell-color-at-specific-value
    public void initializeIntersectionTable() {

        intersectionTable = new javax.swing.JTable(intersections, intersectionColumnNames) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component comp = super.prepareRenderer(renderer, row, col);
                Object value = getModel().getValueAt(row, col);
                if (value.equals("Red")) {
                    comp.setBackground(Color.red);
                } else if (value.equals("Green")) {
                    comp.setBackground(Color.green);
                } else if (value.equals("Yellow")) {
                    comp.setBackground(Color.yellow);
                } else {
                    comp.setBackground(Color.white);
                }
                return comp;
            }
        };
    }

    //Method to "recreate"/add a new row in car table whenever a new car is added.
    public void updateCarTable() {
        //Creates the model based on cars and column names
        carTable.setModel(new javax.swing.table.DefaultTableModel(cars, carColumnNames));

        //Method to resize columns found from:
        //https://kodejava.org/how-do-i-set-or-change-jtable-column-width/#:~:text=Each%20column%20of%20a%20JTable,width%20of%20the%20column%20respectively.
        int[] columnsWidth = {50, 55, 100};

        int currentCar = 0;
        for (int width : columnsWidth) {
            TableColumn column = carTable.getColumnModel().getColumn(currentCar++);
            column.setMinWidth(width);
            column.setMaxWidth(width);
            column.setPreferredWidth(width);
        }
    }

    //Method to "recreate"/add a new row in intersection table whenever a new car is added.
    public void updateIntersectionTable() {

        intersectionTable.setModel(new javax.swing.table.DefaultTableModel(intersections, intersectionColumnNames));

        int[] columnsWidth = {92, 92, 53};

        int currentIntersection = 0;
        for (int width : columnsWidth) {
            TableColumn column = intersectionTable.getColumnModel().getColumn(currentIntersection++);
            column.setMinWidth(width);
            column.setMaxWidth(width);
            column.setPreferredWidth(width);
        }

    }

    //Display this JOptionpane whenever a field is missing input or has negative values
    public void incorrectFieldSyntax(String extra) {
        JOptionPane.showMessageDialog(null, "Invalid input in text field. " + extra,
                "Invalid Field Entry", JOptionPane.ERROR_MESSAGE);
    }

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {
        time.toggleActive();//Toggle booleans so that threads can preform the actions when the start button is pressed.
        hasStarted = true;//This is used to make sure new cars/intersections don't start automatically.
        infoLabel.setText("Simulation started ");
        time.start();
        //Can't use one for loop for both in case new cars/intersections were added
        for (IntersectionThread t : intersectionThreads) {
            t.start();
        }
        for (CarThread c : carThreads) {
            c.start();
        }

        //Hide button so it can't be used again
        startButton.setVisible(false);

    }

    private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {

        if (!isPaused) {
            //If is not paused then pause and change GUI so user can easily tell.
            infoLabel.setText("Simulation paused");
            timeLabel.setText(timeLabel.getText() + " (Paused)");
            pauseButton.setText("Resume");
            isPaused ^= true;
        } else {
            //If simulation is resumed then change GUI so user can easily tell.
            infoLabel.setText("Simulation resumed");
            pauseButton.setText("Pause"); //PROBABLY DONT NEED THIS BECAUES YOU CAN HIT START TO RESUME OR WHATEVE RJUST NEED A BOOLEAN TO TELL IF STARTED OR PAUSED
            isPaused ^= true;

        }
        //Toggle master thread so they no to pause/resume
        time.togglePause();
    }

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {
        time.toggleActive();//Toggle thread so the static variable will be false (all threads will stop)

        //Hide all of these elements so they can't be interacted with again
        pauseButton.setVisible(false);
        stopButton.setVisible(false);
        addCarButton.setVisible(false);
        addIntersectionButton.setVisible(false);
        speedTextField.setVisible(false);
        locationTextField.setVisible(false);
        locationLabel.setVisible(false);
        timeLabel.setVisible(false);
        //Update the panels to signify the program being stopped (keep tables as is)
        speedLabel.setText("Thanks for using the program!");
        infoLabel.setText("Simulation has been stopped.");
        //Interrupt all threads:

    }

    private void addCarButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            int speed = Integer.parseInt(speedTextField.getText());
            //Can't have a negative speed
            if (speed < 0) {
                throw new NumberFormatException();
            } else if (speed > 500) {
                incorrectFieldSyntax("That's a bit too fast for public roads.");
                //Reset the textfield for next time
                speedTextField.setText("");
                return;
            }
            //Creates a table one size larger to hold new car
            Object[][] newTable = new Object[cars.length + 1][];
            //Copy the current table to the new table
            for (int i = 0; i < cars.length; i++) {
                cars[i][2] = carTable.getModel().getValueAt(i, 2);//Keeps the value of speed so it doesn't hiccup
                newTable[i] = cars[i].clone();
            }
            //This is the new row for the table
            Object[] newCar = {"Car " + totalNumberOfCars, 0, speed};
            //Add the new row as the last row
            newTable[newTable.length - 1] = newCar;
            //Set cars to equal the new table
            cars = newTable;
            //Redraw the table with the new car
            updateCarTable();

            //Add the new thread to the array
            CarThread newThread = new CarThread(carTable, totalNumberOfCars - 1, speed);
            carThreads.add(newThread);
            //If simulation is running then start the thread
            if (hasStarted) {
                newThread.start();
            }

            //Update toal number of cars
            totalNumberOfCars++;
        } catch (NumberFormatException e) {
            //Catches invalid input
            incorrectFieldSyntax("Please ensure only postive integers are used.");
        }
        //Reset the textfield for next time
        speedTextField.setText("");

    }

    private void addIntersectionButtonActionPerformed(java.awt.event.ActionEvent evt) {

        try {
            int xPosition = Integer.parseInt(locationTextField.getText());
            //Can't have a negative position. Kinda cheating to just throw exception but whatever
            if (xPosition < 0) {
                throw new NumberFormatException();
            }
            //Creates a table one size larger to hold new intersection
            Object[][] newTable = new Object[intersections.length + 1][];
            //Copy the current table to the new table
            for (int i = 0; i < intersections.length; i++) {
                intersections[i][2] = intersectionTable.getModel().getValueAt(i, 2);
                newTable[i] = intersections[i].clone();
            }
            //This is the new intersection for the table
            Object[] newIntersection = {"Intersection " + totalNumberOfIntersections, xPosition, ""};
            //Add the new row as the last row
            newTable[intersections.length] = newIntersection;
            //Set intersections to equal the new table
            intersections = newTable;
            //Redraw the table with the new intersection
            updateIntersectionTable();

            //Add the new thread to the array
            IntersectionThread newThread = new IntersectionThread(intersectionTable, totalNumberOfIntersections - 1, xPosition);
            intersectionThreads.add(newThread);
            //If simulation is running then start the thread
            if (hasStarted) {
                newThread.start();
            }

            //Update toal number of intersections
            totalNumberOfIntersections++;
        } catch (NumberFormatException e) {
            //Catches invalid input
            incorrectFieldSyntax("Please ensure only postive integers are used.");
        }
        //Reset the textfield for next time
        locationTextField.setText("");

    }

    //This holds code generated by Netbeans GUI builder.
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        carsPanel = new javax.swing.JPanel();
        carScrollPane = new javax.swing.JScrollPane();
        carTable = new javax.swing.JTable();
        startPanel = new javax.swing.JPanel();
        infoLabel = new javax.swing.JLabel();
        startButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        timeLabel = new javax.swing.JLabel();
        intersectionsPanel = new javax.swing.JPanel();
        speedLabel = new javax.swing.JLabel();
        speedTextField = new javax.swing.JTextField();
        addCarButton = new javax.swing.JButton();
        locationLabel = new javax.swing.JLabel();
        locationTextField = new javax.swing.JTextField();
        addIntersectionButton = new javax.swing.JButton();
        intersectionScrollPane = new javax.swing.JScrollPane();
        setTitle("Final - Sutter");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        updateCarTable();

        carScrollPane.setViewportView(carTable);

        startPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        infoLabel.setText("Press start to begin the simulation");

        startButton.setText("Start");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        pauseButton.setText("Pause");
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });

        stopButton.setText("Stop");
        stopButton.setIconTextGap(2);
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout startPanelLayout = new javax.swing.GroupLayout(startPanel);
        startPanel.setLayout(startPanelLayout);
        startPanelLayout.setHorizontalGroup(
                startPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(startPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(startPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(startPanelLayout.createSequentialGroup()
                                                .addComponent(startButton, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(pauseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(stopButton, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(timeLabel)
                                        .addComponent(infoLabel))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        startPanelLayout.setVerticalGroup(
                startPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(startPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(infoLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(startPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(startButton)
                                        .addComponent(pauseButton)
                                        .addComponent(stopButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(timeLabel)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        intersectionsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        speedLabel.setText("Speed:");

        addCarButton.setText("Add Car");
        addCarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCarButtonActionPerformed(evt);
            }
        });

        addIntersectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addIntersectionButtonActionPerformed(evt);
            }
        });

        updateIntersectionTable();

        locationLabel.setText("Location:");

        addIntersectionButton.setText("Add Intersection");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(intersectionsPanel);
        intersectionsPanel.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGap(12, 12, 12)
                                                .addComponent(speedLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(speedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(locationLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(locationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(addCarButton, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(addIntersectionButton))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(speedLabel)
                                        .addComponent(speedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(addCarButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(locationLabel)
                                        .addComponent(locationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(addIntersectionButton))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        //updateIntersectionTable();
        intersectionScrollPane.setViewportView(intersectionTable);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(carsPanel);
        carsPanel.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(carScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(intersectionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(startPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(intersectionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(startPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(intersectionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(carScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                                        .addComponent(intersectionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(carsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(carsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        startPanel.setPreferredSize(new Dimension(212, 86));
        intersectionsPanel.setPreferredSize(new Dimension(246, 86));

        pack();
    }// </editor-fold>    

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Project3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Project3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Project3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Project3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Project3().setVisible(true);

            }
        });
    }

}
