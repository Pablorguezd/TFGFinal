/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.problems.UDN.model.users;

import jmetal.problems.UDN.model.UDN;

/**
 *
 * @author paco
 */
class RandomWaypoint extends MobilityModel {
    
    //velocity in m/s
    double minV_ = 0.01;
    double maxV_ = 2.5;

    public RandomWaypoint(double minV_, double maxV_) {
        this.minV_ = minV_;
        this.maxV_ = maxV_;
    }
    
    
    @Override
    public int[] move(int[] current, double tics) {
        int[]     coordinate = new int[3];
        double[]  velocity   = new double[3];
        
        //compute a random velocity for each dimension
        for (int i = 0; i < 3; i++) {
            velocity[i] = this.minV_ + this.maxV_*UDN.random_.nextDouble();
        }
        
        //compute the new point in the grid
        for (int i = 0; i < 3; i++) {
            coordinate[i] = (int) (current[i] + velocity[i]*tics);
        }
        
        return coordinate;
    }
    
}
