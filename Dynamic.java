/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.problems.UDN;

import jmetal.core.Solution;
import jmetal.problems.UDN.model.DynamicUDN;
import jmetal.util.JMException;

/**
 *
 * @author pablo
 */
public class Dynamic {
    
    public static void main(String args[]) throws ClassNotFoundException, JMException, Exception {
        int SimTime = 100;
        int interval = 20; //minutes
        double [] up_st = new double [SimTime];
        double [] up_dt = new double [SimTime];
        double [] up_ct = new double [SimTime];
        //initializaition
        DynamicCSO cso = new DynamicCSO("main.conf", 0,SimTime);
        Solution s = new Solution(cso);
        cso.evaluate(s);    //*/
        System.out.println("s = " + s);
        System.out.println("visited points = " + cso.pointsWithStatsComputed());
        DynamicUDN udn = (DynamicUDN)cso.udn_;
        for(int i=1; i<=SimTime ;i++){
            //if(cso.udn_.updateUsersPositionZapata("user_mobility_matrix_adapted.mat", i)){
            if(udn.updateUsersPositionShanghai(i)){
            //if(cso.udn_.updateUsersPosition("user_mobility_matrix_adapted.mat", i)){
                udn.updateUsersDemand();
                udn.updateUsersActivation(i*interval);
                cso.evaluate(s);
                double[] provisioning = udn.computeProvisioning();
                up_st[i-1] = provisioning[0];
                up_dt[i-1] = provisioning[1];
                up_ct[i-1] = provisioning[2];
                
               
                System.out.println("ACTUAL TIME: " + ((i*interval)/60)%24);
                System.out.println("----------------------------------------------");
                System.out.println("Users connected to Macro:  " + cso.udn_.usersConnectedToMacro());
                System.out.println("Users connected to Micro:  " + cso.udn_.usersConnectedToMicro());
                System.out.println("Users connected to Pico:  " + cso.udn_.usersConnectedToPico());
                System.out.println("Users connected to Femto:  " + cso.udn_.usersConnectedToFemto());
                System.out.println("Number of active cells:  " + cso.udn_.getTotalNumberOfActiveCells());
                System.out.println("----------------------------------------------");


               // System.out.println("Num Active Users: " + cso.udn_.getNumberOfActiveUsers());
                double stats[] = udn.computeProvisioningStats(up_st, up_dt, up_ct, interval, i );
                
                
                
            }
      
        }

    }
}
