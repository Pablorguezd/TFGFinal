/*
* To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.problems.UDN.model.users;

import Jama.Matrix;
import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLNumericArray;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.problems.UDN.model.Point;
import jmetal.problems.UDN.model.SocialAttractor;
import jmetal.problems.UDN.model.UDN;
import jmetal.problems.UDN.model.cells.Cell;
import jmetal.util.PPP;

/**
 *
 * @author paco
 */
public class User {

    //id
    int id_;
    //current position
    int x_;
    int y_;
    int z_;
    //initial position
    int x0_;
    int y0_;
    int z0_;

    //number of antennas depending on each technology
    int numFemtoAntennas_;
    int numPicoAntennas_;
    int numMicroAntennas_;
    int numMacroAntennas_;

    //traffic demand
    double trafficDemand_;
    double normalDemand_ = 500;
    double heavyDemand_ = 2000;
    double residualTraffic_= 10;
    
    //activity
    boolean active_;

    //Assignment info
    private Cell servingCell_;

    //typename
    String typename_;
    
    //PPP for demand distribution
    Random random_;
    PPP ppp_;
    double lambdaForPPP_ = 1000;

    public User(int id,
            int x, int y, int z,
            double demand,
            String typename,
            boolean isActive,
            int numFemtoAntennas,
            int numPicoAntennas,
            int numMicroAntennas,
            int numMacroAntennas) {
        this.id_ = id;
        this.x_ = x;
        this.y_ = y;
        this.z_ = z;
        this.active_ = isActive;
        if(isActive){  
            this.trafficDemand_ = demand;
        } else{
            this.trafficDemand_ = this.residualTraffic_;
        }
        this.typename_ = typename;
        this.numFemtoAntennas_ = numFemtoAntennas;
        this.numPicoAntennas_ = numPicoAntennas;
        this.numMicroAntennas_ = numMicroAntennas;
        this.numMacroAntennas_ = numMacroAntennas;
        this.servingCell_ = null;
        
        this.random_ = new Random();
        this.ppp_ = new PPP(this.random_);
        

    }

    @Override
    public String toString() {
        return "U(" + x_ + "," + y_ + ")";
    }

    public int getX() {
        return x_;
    }

    public int getY() {
        return y_;
    }
    
    public int getZ() {
        return z_;
    }
    
    public int getID(){
        return id_;
    }

    public String getUserTypename() {
        return this.typename_;
    }

    public void moveUserTowardsSA(SocialAttractor sa, UDN udn, double meanBeta) {
        double x1 = this.x_;
        double y1 = this.y_;
        double z1 = this.z_;
        double x2 = sa.getX();
        double y2 = sa.getY();
        double z2 = sa.getZ();

        //draw beta randomly
        double sigma = (0.5 - Math.abs(meanBeta - 0.5)) / 3.0;
        double beta = meanBeta + udn.getRandom().nextGaussian() * sigma;

        this.x_ = (int) (beta * x2 + (1 - beta) * x1);
        this.y_ = (int) (beta * y2 + (1 - beta) * y1);
        this.z_ = (int) (beta * z2 + (1 - beta) * z1);
    }

    public double getTrafficDemand() {
        double demand;
        if(this.active_){
            demand = trafficDemand_;
        }
        else{
            demand = this.residualTraffic_;
        }
        return demand;
    }
    


    public double userRequiredBWAtCell(UDN udn, Cell c) {
        double sinr, log2sinr, userBW;
        Point p = udn.getGridPoint(x_, y_, z_);

        //sinr in dB
        sinr = p.computeSINR(c);

        log2sinr = Math.log1p(sinr) / Math.log(2.0);

        userBW = this.trafficDemand_ / log2sinr;

        return userBW;
    }
    
    public double capacity(UDN udn, double bw) {
        Point p = udn.getGridPoint(x_, y_, z_);
        
        double sinr = p.computeSINR(this.servingCell_);
        double log2sinr = Math.log1p(sinr) / Math.log(2.0);
        double capacity = bw * log2sinr;
        
        return capacity;
    }
    
    
    public double capacityMIMO(UDN udn, double bw){
        double capacity=9;
        Matrix H = loadH();
        //System.out.println("E");
     //   Matrix num =  
      //  capacity = Math.abs(calc.det());
        
        return capacity;
    }
    
    public double capacityMIMOMatlab(UDN udn, double bw)  {
        Point p = udn.getGridPoint(x_, y_, z_);
        //number of transmitters and receivers of the antenna
        int nt, nr;
        //TODO: later extract these data from the config file (it's a property of the cell
        //or integrate it in the matlab function
        nt=7;
        nr=10;
        //double[][] H = loadH();
//        
//        MatlabEngine eng = MatlabEngine.startMatlab(); 
//        String path = "MIMO";
//        char[] rute= new char[path.length()];
//        for (int i =0; i<path.length(); i++){
//            rute[i]=path.charAt(i);
//        }
//        eng.feval("cd", rute);
//        eng.eval("pwd");
//        
//        eng.putVariable("Hjava", H);
//        double sinr = p.computeSINR(this.servingCell_);
        //BW*LOG2 (Nt+Sinr/nr*H*H)
        
       // double log2sinr = Math.log((getIdentity(nt) + sinr)/(nr*H*transposeMatrix(H))) / Math.log(2.0);
        //double capacity = bw * log2sinr;
        
        //return capacity;
        return 56;
    }

    /**
     * Get the channel gain from the channel matrix H, transmitter and receiver antennas given
     * @param tra
     * @param rec
     * @return 
     */
    public Matrix loadH(){
        //TODO: implent a function and load the gain directly from matlab    
        MLNumericArray H;
        MatFileReader reader = null;
        try {
            reader = new MatFileReader("H.mat");
        } catch (IOException ex) {
            Logger.getLogger(UDN.class.getName()).log(Level.SEVERE, null, ex);
        }

        H = (MLNumericArray) reader.getContent().get("H");
       // double[][] gain = new double[H.getM()][];
        Matrix gain = new Matrix(H.getM(), H.getN());
        
//        for (int i=0; i<H.getM(); i++){
//            for(int j=0; j<H.getN(); j++){
//                gain[i][j]= H.getReal(i, j).floatValue();
//            }
//        }
        for (int i=0; i<H.getM(); i++){
            for(int j=0; j<H.getN(); j++){
                gain.set(i,j,H.getReal(i,j).floatValue());
            }
        }
        return gain;
    }

    public void setServingCell(Cell c) {
        this.servingCell_ = c;
    }
    
    public void setX(int x){
        this.x_=x;
    }
    
    public void setY(int y){
        this.y_=y;
    }
    
    public void setZ(int z){
        this.z_=z;
    }

    public Cell getServingCell() {
        return this.servingCell_;
    }
    
    public boolean isActive(){
        boolean active;
        if(this.active_){
            active = true;
        }
        else{
            active = false;
        }
        
        return active;
    }
    
    public void activateUser(){
        this.active_ = true;
        if (this.typename_=="heavy"){
            this.trafficDemand_ = this.heavyDemand_;
        } else if (this.typename_=="normal"){
            this.trafficDemand_ = this.normalDemand_;
        }
        
    }
    
    public void deactivateUser(){
        this.active_ = false;
        this.trafficDemand_ = this.residualTraffic_;
    }
    public void updateDemand(){
        //TODO: use specific user associated demand for the density of the PPP
        this.trafficDemand_ = ppp_.getPoisson(this.trafficDemand_);
       // System.out.println("new demand: "+this.trafficDemand_);
    }
}
