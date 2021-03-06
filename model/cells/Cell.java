/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.problems.UDN.model.cells;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLNumericArray;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.problems.UDN.model.UDN;
import jmetal.problems.UDN.model.users.User;

/**
 *
 * @author paco
 */
public abstract class Cell {

    protected static int uniqueId_ = 0;

    //Cell parameters
    int id_;
    UDN.CellType type_;
    String name_;
    UDN udn_;
    BTS bts_;
    boolean active_;
    double cost_;
    private double totalBW_;
    private int usersAssigned_;
    
    //Radiation diagrama of the antennas
    protected static transient MLNumericArray antennaArray_;

 

    /**
     * Creates a new instance of the class, but in each subclass
     */
    public static Cell newInstance(Cell c) {
        return c.newInstance();
    }

    abstract Cell newInstance();

    /**
     *
     * @param cellType
     * @param x
     * @param y
     * @param z
     * @param numAntennasTX
     * @param transmittedPower
     * @param consumedPower
     * @param transmitterGain
     * @param receptorGain
     * @param workingFrequency
     * @return
     */
    public static Cell newInstance(
            String cellType, String cellName, UDN udn, int x, int y, int z,
            int numAntennasTX,
            double transmittedPower,
            double alfa, 
            double beta, 
            double delta,
            double transmitterGain,
            double receptorGain,
            double workingFrequency,
            double coverageRadius,
            String radiationPatternFile) {
        Cell cell = null;
        
        
        if (cellType.equalsIgnoreCase("femto")) {
            cell = new FemtoCell(
                    udn,
                    cellName,
                    x,
                    y,
                    z,
                    numAntennasTX,
                    transmittedPower,
                    alfa, 
                    beta, 
                    delta,
                    transmitterGain,
                    receptorGain,
                    workingFrequency,
                    coverageRadius,
                    radiationPatternFile
            );
        } else if (cellType.equalsIgnoreCase("pico")) {
            cell = new PicoCell(
                    udn,
                    cellName,
                    x,
                    y,
                    z,
                    numAntennasTX,
                    transmittedPower,
                    alfa, 
                    beta, 
                    delta,
                    transmitterGain,
                    receptorGain,
                    workingFrequency,
                    coverageRadius,
                    radiationPatternFile
            );
        } else if (cellType.equalsIgnoreCase("micro")) {
            cell = new MicroCell(
                    udn,
                    cellName,
                    x,
                    y,
                    z,
                    numAntennasTX,
                    transmittedPower,
                    alfa, 
                    beta, 
                    delta,
                    transmitterGain,
                    receptorGain,
                    workingFrequency,
                    radiationPatternFile
            );
        } else if (cellType.equalsIgnoreCase("macro")) {
            cell = new MacroCell(
                    udn,
                    cellName,
                    x,
                    y,
                    z,
                    numAntennasTX,
                    transmittedPower,
                    alfa, 
                    beta, 
                    delta,
                    transmitterGain,
                    receptorGain,
                    workingFrequency,
                    radiationPatternFile
            );
        } else {
            System.out.println("Unknown cell type: " + cellType);
            System.exit(-1);
        }

        return cell;
    }

    /**
     * Empty constructor
     */
    public Cell() {
        id_ = this.uniqueId_;
        bts_ = null;
    }

    public Cell(UDN udn,
            String cellName,
            int x, int y, int z,
            int numAntennasTX,
            double transmittedPower,
            double alfa, 
            double beta, 
            double delta,
            double transmitterGain,
            double receptorGain,
            double workingFrequency,
            String radiationPatternFile) {
        this.udn_ = udn;
        this.id_ = this.uniqueId_;
        this.uniqueId_++;
        this.name_ = cellName;
        //the bandwidth is 10% of the working frequency
        this.totalBW_ = workingFrequency * 0.05;
        this.usersAssigned_ = 0;

        //BTS configuration
        double c = 3e8;
        double wavelenght = c / (workingFrequency*1000000);

        this.bts_ = new BTS(
                x,
                y,
                z,
                numAntennasTX,
                transmittedPower,
                alfa, 
                beta,
                delta,
                transmitterGain,
                receptorGain,
                workingFrequency,
                wavelenght,
                radiationPatternFile
        );

    }
    
    /**
     * Get total demanded capacity
     * 
     * @return 
     */
    public double getTrafficDemand(){
        int count = 0;
        double totalDemand = 0;
        double totalCapacity= 0;
        double sum = 0;
        int satisfied=0;
        int unsatisfied = 0;
        double satisfactionRate = 0;
        
        for(User u : this.udn_.getUsers()){
            if (u.getServingCell()==this){
                double userCapacity = 0;
                double userDemand = 0;
                //userCapacity += u.capacityMIMO(this.udn_, this.getSharedBWForAssignedUsers());
                userCapacity = u.capacity(this.udn_, this.getSharedBWForAssignedUsers());
                userDemand = u.getTrafficDemand()/1000;

                //If the user demand is satisfied (capacity>demand), the user will "consume" its demand
                //if not satisfied, the upper bound is the capcity of the link
                sum += Math.min(userCapacity, userDemand);
                if(Math.min(userCapacity, userDemand) == userCapacity){
                    //System.out.println("Unsatisfied");                    
                    unsatisfied++;
                }
                else {
                    //System.out.println("Satisfied");
                    satisfied++;
                }
                //demand += u.capacityMIMO(this.udn_, this.getSharedBWForAssignedUsers());
                count++;
                }
            }
            
            
        
        if (count !=0){
            satisfactionRate = (double)satisfied/count;
          //  System.out.println("Satisfaction Rate: " + satisfactionRate + " Users connected: " + this.getAssignedUsers() + " Type: " + this.getType().toString());
 
        }
   
        return sum;
    }
    /**
     * Getter and setters
     */
    public BTS getBTS() {
        return bts_;
    }

    public int getID() {
        return this.id_;
    }

    public void setActivation(boolean b) {
        this.active_ = b;
    }

    public boolean isActive() {
        return this.active_;
    }

    public double getCost() {
        return this.cost_;
    }

    public UDN.CellType getType() {
        return this.type_;
    }

   
    public double getTotalBW() {
        return totalBW_;
    }
    

   
   
   
    @Override
    public String toString() {
        String type = "";
        switch (this.type_) {
            case MACRO:
                type = "M";
                break;
            case MICRO:
                type = "m";
                break;
            case PICO:
                type = "p";
                break;
            case FEMTO:
                type = "f";
                break;
        }
        
        return "[" + type + "," + bts_.x_ + "," + bts_.y_ + "," + bts_.z_ + "," + active_ + "," + usersAssigned_+ "]";
    }

    public void addUserAssigned() {
        this.usersAssigned_ ++;
    }
    
    public void removeUserAssigned() {
        this.usersAssigned_ --;
    }
    
    public int getAssignedUsers() {
        return this.usersAssigned_ ;
    }

    public void setNumbersOfUsersAssigned(int v) {
        this.usersAssigned_ = v;
    }

    public double getSharedBWForAssignedUsers() {
        return this.totalBW_ / this.usersAssigned_;
    }

}
