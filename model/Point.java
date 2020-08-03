/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.problems.UDN.model;

import java.util.Comparator;
import jmetal.problems.UDN.model.cells.Cell;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.lang.*;
import java.util.ArrayList;
import jmetal.problems.UDN.model.cells.BTS;

/**
 *
 * @author paco
 */
public class Point {

    //Reference to the problem
    UDN udn_;

    //Point coordinates
    int x_;
    int y_;
    int z_;

    boolean hasBTSInstalled_;
    Cell installedCell_ = null;

    //Propagation region
    Region propagationRegion_;

    //SINR
    Map<Double, Double> totalReceivedPower_;
    Map<Double, Cell> sinr_;

    //Used to precompute stats only when needed_
    boolean statsComputed_;

    //Function<Cell, Double> function_;

    Map<Integer, Double> signalPowerMap_ = new HashMap<>();
    Map<Integer, Double> sinrMap_ = new HashMap<>();

    /**
     * Constructor
     *
     * @param x Coordinate x
     * @param y Coordinate y
     * @param z Coordinate z
     */
    Point(UDN udn, int x, int y, int z) {
        udn_ = udn;
        x_ = x;
        y_ = y;
        z_ = z;

        hasBTSInstalled_ = false;
        installedCell_ = null;
        propagationRegion_ = null;
        totalReceivedPower_ = null;
        statsComputed_ = false;
       

    }

    void setPropagationRegion(Region r) {
        propagationRegion_ = r;
    }

    Region getPropagationRegion() {
        return this.propagationRegion_;
    }

    /**
     * Computes the signal power received at this grid point from the BTS b
     *
     * @param c The serving Cell
     * @return The received power
     */
    public double computeSignalPower(Cell c) {
        double powerDB;

        //create a new map after X insertions to bound the memory used
        if (signalPowerMap_.get(c.getID()) == null) {

            BTS b = c.getBTS();
            double pathloss = this.propagationRegion_.pathloss_;
            double receptorGain = Math.pow(10.0, b.getReceptorGain() / 10.0);
            double trasmitterGain = Math.pow(10.0, b.getTransmitterGain() / 10.0);
            double wavelenth = b.getWavelenght();
            double transmiterPower = b.getTransmittedPower();
            double distance = this.udn_.distance(this.x_, this.y_, this.z_, b.getX(), b.getY(), b.getZ());
            double power;
            int angles[] = this.udn_.calculateAngles(this, b);
            double attenuationFactor = b.getAttenuationFactor(angles[0], angles[1]);
            //double attenuationFactor = 1.0;

            power = receptorGain * trasmitterGain * transmiterPower
                    * Math.pow((wavelenth / (4.0 * Math.PI * distance)), pathloss) * attenuationFactor;//*añadir factor
            //

            powerDB = 10.0 * Math.log10(power * 1000.0);
            signalPowerMap_.put(c.getID(), powerDB);
//            System.out.println(signalPowerMap_.size());
        } else {
            powerDB = signalPowerMap_.get(c.getID());
        }

        return powerDB;
    }


    /**
     * Returns the closest social attractor to this point
     *
     * @param sas
     * @return
     */
    SocialAttractor getClosestSA(UDN udn) {

        SocialAttractor sa = null;
        double minDistance = Double.MAX_VALUE;
        double d;

        for (SocialAttractor s : udn.socialAttractors_) {
            d = udn.distance(this.x_, this.y_, this.z_, s.x_, s.y_, s.z_);
            if (d < minDistance) {
                minDistance = d;
                sa = s;
            }
        }

        return sa;
    }

    /**
     * Distance to the BTS of the given cell.
     *
     * @param c
     * @return The Euclidean distance from this point to the Cell BTS
     */
    private double distanceToBTS(Cell c) {
        return this.udn_.distance(this.x_, this.y_, this.z_,
                c.getBTS().getX(), c.getBTS().getY(), c.getBTS().getZ());

    }

        public double computeSINR(Cell c) {
        double sinr = 0.0;

        if (sinrMap_.get(c.getID()) == null) {
            //get the bandwidth of the BTS and its working frequency
            double btsBW = c.getTotalBW();
            double frequency = c.getBTS().getWorkingFrequency();

            //compute the noise
            double pn = -174 + 10.0 * Math.log10(btsBW * 1000000);

            //get the averaged power received at the BTS working frequency
            double totalPower = this.totalReceivedPower_.get(frequency);

            //compute the power received at this point from BTS b
            double power = this.computeSignalPower(c);

            //double distance = this.udn_.distance(this.x_, this.y_, c.getBTS().getX(), c.getBTS().getY());
            //compute the SINR
            //dB -> mW
            pn = Math.pow(10.0, pn / 10);
            power = Math.pow(10.0, power / 10);
            sinr = power / (totalPower - power + pn);
            // System.out.println("POTENCIA: "+power + "TOTAL: "+totalPower);
            sinrMap_.put(c.getID(), sinr);
        } else {
            sinr = sinrMap_.get(c.getID());
        }
        
        return sinr;
    }

    /**
     * Precomputes the averaged SINR at each grid point, for each
     */
    void computeTotalReceivedPower() {
        //allocat memory at this point
        totalReceivedPower_ = new TreeMap<>();

        //por tipo de BTS
        double sum, power;

        for (double frequency : this.udn_.cells_.keySet()) {
            sum = 0.0;

            for (Cell c : this.udn_.cells_.get(frequency)) {
                if (c.isActive()) {

                    power = computeSignalPower(c);
                    //dB -> mW
                    power = Math.pow(10.0, power / 10);
                    sum += power;
                }
            }

            this.totalReceivedPower_.put(frequency, sum);
        }
    }

    /**
     * Return the closest BTS in terms of the received signal power. Required by
     * M. Mirahsan, R. Schoenen, and H. Yanikomeroglu, “HetHetNets:
     * Heterogeneous Traffic Distribution in Heterogeneous Wireless Cellular
     * Networks,” IEEE J. Sel. Areas Commun., vol. 33, no. 10, pp. 2252–2265,
     * 2015.
     *
     * @return The closest BTS
     */
    Cell getCellWithHigherReceivingPower() {
        double power;
        double maxPower = Double.NEGATIVE_INFINITY;
        Cell closest = null;

        for (Double frequency : this.udn_.cells_.keySet()) {
            for (Cell c : udn_.cells_.get(frequency)) {
                power = this.computeSignalPower(c);
                if (power > maxPower) {
                    maxPower = power;
                    closest = c;
                }
            }
        }

        return closest; //

    }

    /**
     * Returns the cell that serves with the best SINR, regardless of its
     * operating frequency.
     *
     * @return
     */
    public Cell getCellWithHigherSINR() {
        double sinr;
        Map<Double, Double> maxSINR = new TreeMap<>();
        Map<Double, Cell> servingBTSs = new TreeMap<>();
        Cell servingCell = null;

        for (Double frequency : this.udn_.cells_.keySet()) {
            maxSINR.put(frequency, Double.NEGATIVE_INFINITY);
        }

        for (Double frequency : this.udn_.cells_.keySet()) {
            for (Cell c : udn_.cells_.get(frequency)) {
                if (c.isActive()) {
                    sinr = this.computeSINR(c);

                    //quality, regardless of the cell activation
                    if (sinr > maxSINR.get(frequency)) {
                        maxSINR.put(frequency, sinr);
                        servingBTSs.put(frequency, c);
                    }
                }
            }
        }

        //retrieve the best among the precomputed values
        double maxValue = Double.NEGATIVE_INFINITY;
        for (Double f : servingBTSs.keySet()) {
            sinr = maxSINR.get(f);
            if (sinr > maxValue) {
                maxValue = sinr;
                servingCell = servingBTSs.get(f);
            }
        }

        return servingCell;

    }

    /**
     * Returns the cell that serves with the best SINR, discarding macrocells
     * and regardless of its operating frequency.
     *
     * @return
     */
    public Cell getCellWithHigherSINRButMacro() {
        double sinr;
        Map<Double, Double> maxSINR = new TreeMap<>();
        Map<Double, Cell> servingBTSs = new TreeMap<>();
        Cell servingCell = null;

        for (Double frequency : this.udn_.cells_.keySet()) {
            maxSINR.put(frequency, Double.NEGATIVE_INFINITY);
        }

        for (Double frequency : this.udn_.cells_.keySet()) {
            for (Cell c : udn_.cells_.get(frequency)) {

                if (c.isActive() && !(c.getType().toString().equalsIgnoreCase("MACRO"))) {
                    sinr = this.computeSINR(c);

                    //quality, regardless of the cell activation
                    if (sinr > maxSINR.get(frequency)) {
                        maxSINR.put(frequency, sinr);
                        servingBTSs.put(frequency, c);
                    }
                }
            }
        }//for

        //retrieve the best among the precomputed values
        double maxValue = Double.NEGATIVE_INFINITY;
        for (Double f : servingBTSs.keySet()) {
            sinr = maxSINR.get(f);
            if (sinr > maxValue) {
                maxValue = sinr;
                servingCell = servingBTSs.get(f);
            }
        }

        return servingCell;

    }

    /**
     * Return a sorted list of Cells with the best serving SINR, regardless of
     * their operation frequency and type
     *
     * @return
     */
    public SortedMap<Double, Cell> getCellsWithBestSINRs() {
        Point p = this;
        //create the comparator for the sortedlist
        Comparator<Double> cellSINRComparator = new Comparator<Double>() {
            @Override
            public int compare(Double sinr1, Double sinr2) {

                return Double.compare(sinr2, sinr1);
            }
        };

        SortedMap<Double, Cell> sortedCells = new TreeMap<Double, Cell>(cellSINRComparator);

        for (Double frequency : this.udn_.cells_.keySet()) {
            for (Cell c : udn_.cells_.get(frequency)) {
                double sinr = this.computeSINR(c);
                sortedCells.put(sinr, c);
            }
        }

        return sortedCells;
    }

    Map<Double, Double> getTotalReceivedPower() {
        return this.totalReceivedPower_;
    }

    public boolean hasBTSInstalled() {
        return hasBTSInstalled_;
    }

    //Has BTS?
    public void setHasBTSInstalled(boolean activation) {
        this.hasBTSInstalled_ = activation;
    }

    public void setInstalledCell(Cell cell) {
        this.installedCell_ = cell;
    }

    public Cell getInstalledCell() {
        return this.installedCell_;
    }

}
