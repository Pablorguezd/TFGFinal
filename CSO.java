package jmetal.problems.UDN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.lang.Double;
import java.util.Random;
import java.util.TreeMap;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.variable.Binary;
import jmetal.problems.UDN.model.Point;
import jmetal.problems.UDN.model.UDN;
import jmetal.problems.UDN.model.UDN.CellType;
import static jmetal.problems.UDN.model.UDN.CellType.FEMTO;
import jmetal.problems.UDN.model.cells.BTS;
import jmetal.problems.UDN.model.cells.Cell;
import jmetal.problems.UDN.model.users.User;
import jmetal.util.JMException;

/**
 * Class representing problem ZDT1
 */
public abstract class CSO extends Problem {

    //The underlying UDN
    UDN udn_;

    //The seed to generate the instance
    int run_;
    
    public int getTotalNumberOfActivableCells() {
        return udn_.getTotalNumberOfActivableCells();
    }

   

    int pointsWithStatsComputed() {
        return udn_.pointsWithStatsComputed();
    }

    double powerConsumptionBasedOnTransmittedPower() {
        double sum = 0.0;

        for (List<Cell> cells : udn_.cells_.values()) {
            for (Cell c : cells) {
                if (c.isActive()) {
                    sum += 4.7 * c.getBTS().getTransmittedPower();
                } else {
                    //residual consuption in sleep mode (mW)
                    sum += 160;
                }
            }
        }
        //mW -> W -> kW -> MW
        sum /= 1000000000;

        return sum;
    }
    
    /**
     * Calculates the power consumption taking into account the total traffic demand
     * @return 
     */
    double powerConsumptionPiovesan() {
        double sum = 0.0;

        for (List<Cell> cells : udn_.cells_.values()) {
            for (Cell c : cells) {
                BTS b = c.getBTS();
                if (c.isActive()) {
                    //sum += c.getBTS().getBaseConsumedPower() * 4.7 * c.getBTS().getTransmittedPower();
//                    double td = c.getTrafficDemand();
                    sum += b.getTransmittedPower() * b.getAlfa() + 
                           b.getBeta() + b.getDelta() * c.getTrafficDemand() + 10;
                } else {
                    //residual consuption in sleep mode (mW)
                    sum += b.getTransmittedPower()*0.01;
                }
            }
        }
        //mW -> W -> kW -> MW
        sum /= 1000000000;

        return sum;
    }


    
    double[][] loadH(BTS bts){
        double [][] H = null;
        
        return H;
    }
    
    void saveCellStatus(Solution s) {
        Binary cso = ((Binary) s.getDecisionVariables()[0]);

        //map the activation to the udn
        udn_.copyCellActivation(cso);
    }

    /**
     * Max capacity of the 5G network. At each point, it returns the best SINR
     * for each of the different operating frequencies.
     *
     * @return
     */
    double networkCapacity(Solution solution) {
        /**
         * For the dynamic problem addressing
         */
        List<Integer> assignment = new ArrayList<Integer>();

        double capacity = 0.0;

        //0.- Reset number of users assigned to cells
        udn_.resetNumberOfUsersAssignedToCells();

        //1.- Assign users to cells, to compute the BW allocated to them
        for (User u : this.udn_.getUsers()) {
            Point p = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());
            
            Cell c = p.getCellWithHigherSINR();
 
            c.addUserAssigned();

            u.setServingCell(c);

            //dynamic
            assignment.add(c.getID());
        }

        //save the assignment into the solution
        solution.setUEsToCellAssignment(assignment);

        //1.- computes the Mbps allocated to each user
        for (User u : this.udn_.getUsers()) {
            double allocatedBW = u.getServingCell().getSharedBWForAssignedUsers();

            //computes the Mbps
            double c = u.capacity(this.udn_, allocatedBW);
            //double c = u.capacityMIMO(this.udn_, allocatedBW);
            capacity += c / 1000.0;
        }

        //udn_.validateUserAssigned();
        return capacity;
    }

    public int getRun() {
        return this.run_;
    }

    private double numberOfActiveCells() {
        int count = 0;

        for (List<Cell> cells : udn_.cells_.values()) {
            for (Cell c : cells) {
                    if (c.isActive()) {
                    count++;
                }

            }
        }

        return count;
    }

    /**
     * In this function operatos are applied in order to improve the sinr of
     * certain problematic points in the network by switching off some BTS
     */
    public void intelligentSwitchOff(Solution solution) throws JMException {
        Map<Double, List<Point>> worsePoints = new TreeMap<>();
        double sinr_limit = 12;

        for (double op_frequency : this.udn_.cells_.keySet()) {
            List<Point> l = new ArrayList<>();
            for (User u : this.udn_.getUsers()) {
                Cell c = u.getServingCell();
                double f = c.getBTS().getWorkingFrequency();
                if (Double.compare(f, op_frequency) == 0) {
                    Point p = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());
                    if (p.computeSINR(c) < sinr_limit) {
                        l.add(p);
                    }
                }
            }
            if (!l.isEmpty()) {
                l = sortList(l);
                worsePoints.put(op_frequency, l);
            }
        }

        //apply operators
        //noUsersOp();
        //macro1Op(worsePoints);
        //macro2Op(worsePoints);
        //tooManyUsersOp();
        //priorizeFemtoOp();
        modifySolution(solution);

    }

    /**
     * Sort a given list of Points by it SINR, being the worse the first
     *
     * @param l : list to sort
     * @return sorted list
     */
    public List<Point> sortList(List<Point> l) {
        double[] sinr_list = new double[l.size()];
        List<Point> sortedList = new ArrayList<Point>();
        double min_sinr = 5;

        for (int i = 0; i < l.size(); i++) {
            Point p = l.get(i);
            Cell c = p.getCellWithHigherSINR();
            double sinr = p.computeSINR(c);
            sinr_list[i] = sinr;

        }
        Arrays.sort(sinr_list);
        int index = 0;
        for (int i = 0; i < l.size(); i++) {
            for (int j = 0; j < l.size(); j++) {
                Point p_ = l.get(j);
                Cell c_ = p_.getCellWithHigherSINR();
                double sinr_ = p_.computeSINR(c_);
                if (Double.compare(sinr_, sinr_list[i]) == 0) {
                    index = j;
                    break;
                }
            }
            sortedList.add(i, l.get(index));
        }
        return sortedList;
    }

    /**
     * Cells with no users assigned are switched off.
     * @param solution The solution to be modified.
     */
    public void noUsersOp(Solution solution) {

        int count = 0;
        for (double frequency : this.udn_.cells_.keySet()) {
            if (udn_.cells_.containsKey(frequency)) {
                List<Cell> l = udn_.cells_.get(frequency);
                for (int j = 0; j < l.size(); j++) {
                    Cell c = l.get(j);
                    if (c.getAssignedUsers() == 0) {
                        c.setActivation(false);
                        count++;
                    }
                }
            }

        }
        
        modifySolution(solution);
    }

    /**
     * Given a user attached to the macrocell, it can be assigned to other cell
     * in case the SINR received from the other is bigger than a certain
     * threshold value.
     *
     * @param points
     */
    public void macro1Op(Map<Double, List<Point>> points) {
        double threshold = 2;
        double sinr_limit = 4;

        //get the macrocell
        double macro_f = 2000;
        Cell macro;
        macro = udn_.cells_.get(macro_f).get(0);
        List<User> macro_users = new ArrayList<>();

        //get the users assigned to the macrocell
        for (User u : this.udn_.getUsers()) {
            if (u.getServingCell() == macro) {
                Point p = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());
                if (p.computeSINR(macro) < sinr_limit) {
                    macro_users.add(u);
                }
            }
        }
        //apply the operator for these users

        int count = 0;

        //System.out.println("There are "+macro_users.size()+" users attached to the macrocell");
        if (!(macro_users.isEmpty())) {
            for (int i = 0; i < macro_users.size(); i++) {
                User u = macro_users.get(i);
                Point p_ = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());
                double sinr_macro = p_.computeSINR(macro);
                Cell other = p_.getCellWithHigherSINRButMacro();
                double sinr_other = p_.computeSINR(other);
                int nuser = i + 1;
                //System.out.println("User: "+nuser);
                //System.out.println("SINR_macro: "+sinr_macro);
                //System.out.println("SINR_alternative: "+sinr_other);
                //System.out.println("La alternativa es una celda "+ other.getType().toString() + " con SINR: "+ sinr_other);
                if (sinr_other > threshold) {
                    u.setServingCell(other);
                    count++;
                }
            }
        }
        System.out.println("The operator macro1 has been applied " + count + " times");

    }

    /**
     * Given a user attached to the macrocell, it can be assigned to other cell
     * in case a certain condition is fulfilled.
     *
     * @param points
     */
    public void macro2Op(Map<Double, List<Point>> points) {
        double threshold = 6;
        double sinr_limit = 12;

        //get the macrocell
        double macro_f = 2000;
        Cell macro;
        macro = udn_.cells_.get(macro_f).get(0);
        List<User> macro_users = new ArrayList<>();

        //get the users assigned to the macrocell
        for (User u : this.udn_.getUsers()) {
            if (u.getServingCell() == macro) {
                Point p = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());
                if (p.computeSINR(macro) < sinr_limit) {
                    macro_users.add(u);
                }
            }
        }
        //apply the operator for these users

        int count = 0;

        if (!(macro_users.isEmpty())) {
            for (int i = 0; i < macro_users.size(); i++) {
                User u = macro_users.get(i);
                Point p_ = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());
                double sinr_macro = p_.computeSINR(macro);
                Cell other = p_.getCellWithHigherSINRButMacro();
                double sinr_other = p_.computeSINR(other);
                //Here comes the condition
                if ((sinr_macro - sinr_other) < threshold) {
                    u.setServingCell(other);
                    count++;
                }
            }
        }
        System.out.println("The operator macro2 has been applied " + count + " times");

    }

    /**
     * If more than a certain amount of users are connected to a cell, one of
     * them will be switched to the next better one
     */
    public void tooManyUsersOp() {

        int count = 0;
        int threshold = 3; //calcular la media de todos y que haya como máximo 2 veces la media
        Cell alternative = null;
        Point user_location;
        Map<Double, Cell> bestCells = new TreeMap<Double, Cell>();

        for (User u : this.udn_.getUsers()) {
            user_location = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());
            if (u.getServingCell().getAssignedUsers() >= threshold) {

                bestCells = user_location.getCellsWithBestSINRs();
                //get the 2nd best cell
                int i = 1;
                for (Map.Entry<Double, Cell> actualEntry : bestCells.entrySet()) {
                    if (i == 2) {
                        alternative = actualEntry.getValue();
                        break;
                    } else {
                        i++;
                    }
                }
                u.setServingCell(alternative);
                count++;
            }
        }
        System.out.println("The operator tooManyUsers has been applied " + count + " times");
    }

    /**
     * Switch on those femtocells that can serve UEs.
     * @param solution 
     */
    public void priorizeFemtoOp(Solution solution) {
        Binary cso = ((Binary) solution.getDecisionVariables()[0]);

        //map the activation to the udn
        udn_.setCellActivation(cso);

        //recompute the signaling
        udn_.computeSignaling();
        
        //reset the UEs assigned to celss
        udn_.resetNumberOfUsersAssignedToCells();

        //Assign users to cells, to compute the BW allocated to them
        for (User u : this.udn_.getUsers()) {
            Point p = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());

            Cell c = p.getCellWithHigherSINR();

            c.addUserAssigned();

            u.setServingCell(c);
        }
        
        //Look for the the candidate femtocells
        double threshold = 6; //6 y 9 podrían valer: depende del tipo de celda origen: 6 dB por cada salto
        Cell alternative = null;
        Cell current = null;
        Point user_location;
        Map <Double, Cell> bestCells = new TreeMap <Double, Cell> ();
        
        for (User u : this.udn_.getUsers()){
            if (u.getServingCell().getType()!=FEMTO){
                current = u.getServingCell();
                user_location = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());
                bestCells= user_location.getCellsWithBestSINRs();
                for(Map.Entry<Double,Cell> actualEntry: bestCells.entrySet()){
                    alternative = actualEntry.getValue();
                    if(user_location.computeSINR(alternative)>threshold){
                        if(alternative.getType()==FEMTO){
                            u.setServingCell(alternative);  
                            alternative.addUserAssigned();
                            current.removeUserAssigned();
                            if (current.getAssignedUsers() == 0)
                                current.setActivation(false);
                            alternative.setActivation(true);
                            break;
                        }
                    } 
                }
            }//IF
        }//FOR
        
        //apply CSO -> switch off the remaining cells not serving any UE
        for (double frequency : this.udn_.cells_.keySet()){
            if(udn_.cells_.containsKey(frequency)){
                List<Cell> l = udn_.cells_.get(frequency);
                for(int j=0;j<l.size();j++){
                    Cell c = l.get(j);
                    if(c.getAssignedUsers()==0){
                        c.setActivation(false);
                    }
                }
            }
            
        }
        
        //Copy the modifications to the solution
        modifySolution(solution);

    }

    /**
     * Activates/deactivates BTSs in the solution according to the information
     * enclosed in the modified network of the problem
     *
     * @param solution
     */
    public void modifySolution(Solution solution) {
        Binary cso = ((Binary) solution.getDecisionVariables()[0]);
        int bts = 0;

        for (List<Cell> cells : udn_.cells_.values()) {
            for (Cell c : cells) {
                if (c.getType() != CellType.MACRO) {
                    if (c.isActive()) {
                        cso.setIth(bts, true);
                    } else {
                        cso.setIth(bts, false);
                    }
                    bts++;
                }
            }
        }

    }


} // Planning UDN
