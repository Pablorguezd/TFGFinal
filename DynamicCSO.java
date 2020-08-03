package jmetal.problems.UDN;

import java.util.List;
import java.lang.String;
import java.util.Random;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.problems.UDN.model.DynamicUDN;
import jmetal.problems.UDN.model.Point;
import jmetal.problems.UDN.model.cells.BTS;
import jmetal.problems.UDN.model.cells.Cell;
import jmetal.util.JMException;

/**
 * Class representing problem ZDT1
 */
public class DynamicCSO extends CSO {

    int evaluations_ = 0;
    int epochs_;
    int currentEpoch_ = 0;
    
    //for updating the user positions
    boolean shanghai_ = false;

    /**
     * Creates an instance of the UDN planning problems.
     *
     */
    public DynamicCSO(String mainConfig,
            int run,
            int epochs) throws ClassNotFoundException {

        //Create the UDN model
        udn_ = new DynamicUDN(mainConfig, run);

        numberOfVariables_ = 1;
        numberOfObjectives_ = 3;
        numberOfConstraints_ = 0;
        problemName_ = "DynamicCSO";
        run_ = run;
        epochs_ = epochs;

        solutionType_ = new BinarySolutionType(this);

        length_ = new int[numberOfVariables_];
        length_[0] = udn_.getTotalNumberOfActivableCells();
        
        //get mobility info for updating the users position
        if (udn_.getMobilityType().equalsIgnoreCase("shanghai")) {
            shanghai_ = true;
        } 
    }

    /**
     * Evaluates a solution.
     *
     * @param solution The solution to evaluate.
     * @throws JMException
     */
    public void evaluate(Solution solution) throws JMException {
        Binary cso = ((Binary) solution.getDecisionVariables()[0]);

        //map the activation to the udn
        udn_.setCellActivation(cso);

        //update the avera
        udn_.computeSignaling();

        //It has to be called before as it stores the current
        // UEs to Cell assignment and stores it in the solution
        double capacity = networkCapacity(solution);
        int handovers = incurredHandovers(solution);
        double powerConsumption = powerConsumptionPiovesan();

        solution.setObjective(0, powerConsumption);
        solution.setObjective(1, -capacity);
        solution.setObjective(2, handovers);

 
    } // evaluate


    

    public int getNumberOfEpochs() {
        return this.epochs_;
    }

    public void nextEpoch() {
        this.currentEpoch_++;
        if (shanghai_ == true)
            ((DynamicUDN)this.udn_).updateUsersPositionShanghai(this.currentEpoch_);
        else 
            ((DynamicUDN)this.udn_).updateUsersPositionFromMatrix(this.currentEpoch_);
                    
        //update the accumulated evaluations on each epoch
        evaluations_ = 0;
        
        //saving memory: recompute only interesiting points for the new epoch
        this.udn_.emptyMapsAtPoints();
    }

    private int incurredHandovers(Solution solution) {

        int handovers = 0;

        List<Integer> previous = solution.getPreviousUesToCellAssignment();
        if ((currentEpoch_ > 0) && (previous != null)) {

            List<Integer> current = solution.getCurrentUesToCellAssignment();

            for (int i = 0; i < current.size(); i++) {
                if (current.get(i).intValue() != previous.get(i).intValue()) {
                    handovers++;
                }
            }
        } else {
            handovers = this.udn_.getUsers().size();
        }
        return handovers;
    }

    public void adjacentBtsRestart(Solution s, int length, double rate) {
        Binary cso = ((Binary) s.getDecisionVariables()[0]);

        //map the activation to the udn
        udn_.setCellActivation(cso);

        //invoke network capacity to allocate users to cells
        udn_.computeSignaling();
        networkCapacity(s);
        
        //forget previous assignent
        s.forgetUEsToCellAssignment();

        //go through all the cells which does have users connected, and
        //restart adyacent BTSs
        Random r = new Random();
        for (List<Cell> cells : udn_.cells_.values()) {
            for (Cell c : cells) {
                if (c.getAssignedUsers() > 0) {
                    BTS b = c.getBTS();
                    int x = b.getX();
                    int y = b.getY();
                    int z = b.getZ();

                    //activate BTSs within "length" points distances
                    for (int l = 1; l <= length; l++) {
                        for (int deltaX = -l; deltaX <= l; deltaX++) {
                            //avoid activating the current BTS
                            for (int deltaY = -l; deltaY <= l; deltaY++) {
                                for (int deltaZ = -l; deltaZ <= l; deltaZ++) {
                                    if ((deltaX != 0) || (deltaY != 0) || (deltaZ != 0)) {
                                        //randomize the decision
                                        if (r.nextDouble() < rate) {
                                            //check array indexses
                                            int locX = x + deltaX;
                                            int locY = y + deltaY;
                                            int locZ = z + deltaZ;
                                            if (checkLimitX(locX)
                                                    && checkLimitY(locY)
                                                    && checkLimitZ(locZ)) {
                                                Point p = udn_.getGridPoint(locX, locY, locZ);
                                                if (p.hasBTSInstalled()) {
                                                    p.getInstalledCell().setActivation(true);
                                                }
                                            }
                                        }

                                    }
                                }
                            }

                        }
                    }
                }

            }
        }

        modifySolution(s);

    }

    private boolean checkLimitX(int v) {
        if ((v < 0) || (v >= udn_.gridPointsX_)) {
            return false;
        }
        return true;
    }

    private boolean checkLimitY(int v) {
        if ((v < 0) || (v >= udn_.gridPointsY_)) {
            return false;
        }
        return true;
    }

    private boolean checkLimitZ(int v) {
        if ((v < 0) || (v >= udn_.gridPointsZ_)) {
            return false;
        }
        return true;
    }

} // Planning UDN
