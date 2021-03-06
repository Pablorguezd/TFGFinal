/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.problems.UDN.model.cells;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLNumericArray;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author paco
 */
public class BTS {

    //location
    int x_;
    int y_;
    int z_;

    int numAntennasTX_;
    double transmittedPower_;
    double alfa_;
    double beta_;
    double delta_;
    double transmitterGain_;
    double receptorGain_;
    double workingFrequency_;
    double wavelenght_;
    double coverageRadius_;
    
    static transient MLNumericArray antennaArray_;
    static boolean patternFileLoaded_ = false;


    BTS(    int x, int y, int z,
            int numAntennasTX,
            double transmittedPower,
            double alfa, 
            double beta, 
            double delta,
            double transmitterGain,
            double receptorGain,
            double workingFrequency,
            double wavelenght,
            String radiationPatternFile) {
        
        this.x_ = x;
        this.y_ = y;
        this.z_ = z;
        this.numAntennasTX_    = numAntennasTX;
        this.transmittedPower_ = transmittedPower;
        this.alfa_ = alfa;
        this.beta_ = beta;
        this.delta_ = delta;
        this.transmitterGain_  = transmitterGain;
        this.receptorGain_     = receptorGain;
        this.workingFrequency_ = workingFrequency;
        this.wavelenght_       = wavelenght;
        this.coverageRadius_   = Double.MAX_VALUE;
        
        //Load propagation antenna only once
        if (patternFileLoaded_ == false) {
            MatFileReader reader = null;
            try {
                reader = new MatFileReader(radiationPatternFile);
            } catch (IOException ex) {
                Logger.getLogger(BTS.class.getName()).log(Level.SEVERE, null, ex);
            }
            antennaArray_ = (MLNumericArray) reader.getContent().get("antena1");
            patternFileLoaded_ = true;
        }

        
    }

    BTS(BTS b) {
        this.x_ = b.x_;
        this.y_ = b.y_;
        this.z_ = b.z_;
        this.numAntennasTX_    = b.numAntennasTX_;
        this.transmittedPower_ = b.transmittedPower_;
        this.transmitterGain_  = b.transmitterGain_;
        this.receptorGain_     = b.receptorGain_;
        this.workingFrequency_ = b.workingFrequency_;
        this.wavelenght_       = b.wavelenght_;
        this.coverageRadius_   = b.coverageRadius_;
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

    public int getNumAntennasTX() {
        return numAntennasTX_;
    }

    public double getTransmittedPower() {
        return transmittedPower_;
    }

    
    public double getAlfa() {
        return alfa_;
    }
    
    public double getBeta() {
        return beta_;
    }
    
    public double getDelta() {
        return delta_;
    }

    public double getTransmitterGain() {
        return transmitterGain_;
    }

    public double getReceptorGain() {
        return receptorGain_;
    }

    public double getWorkingFrequency() {
        return workingFrequency_;
    }

    public double getWavelenght() {
        return wavelenght_;
    }

    public double getCoverageRadius() {
        return coverageRadius_;
    }
    
    
    /**
     * Given a pair of angles, this function search the correspondence
     * attenuation factor in a given antenna matrix
     *
     * @param azi
     * @param occi
     * @return
     */
    public double getAttenuationFactor(int azi, int occi) {
        double factor = 0;

        if (azi == 0) {
            azi = 1;
        }
        if (occi == 0) {
            occi = 1;
        }
        factor = antennaArray_.getReal(azi - 1, occi - 1).floatValue();

        return factor;
    }
    
    
    

    public double getMaximumCoverage() {
        return this.coverageRadius_;
    }

    @Override
    public String toString() {
        return "BTS(" + x_ + ","+ y_ +')';
    }


    
    
}
