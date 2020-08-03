/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.problems.UDN;

import jmetal.core.Solution;

import jmetal.util.JMException;

/**
 *
 * @author paco
 */
public class Main {

    public static void main(String args[]) throws ClassNotFoundException, JMException {

        
        //UDN udn = new UDN("main.conf", "cells.conf", "users.conf");
        /*PlanningUDN p = new PlanningUDN("main.conf", "cells.conf", "socialAttractors.conf", "users.conf");
        
        //udn.printPropagationRegion();

        //udn.printVoronoi();
        Solution planning = new Solution(p);
        p.setBasicPlanning(planning);
        p.evaluate(planning);
        p.setHigherCapacityPlanning(planning);
        p.evaluate(planning);
//        Simulation sim = new Simulation(udn,60,1.0);
        //udn.printUsers(); */
        int simTime = 0;
        StaticCSO cso = new StaticCSO("main.conf", 0);
        
        Solution s = new Solution(cso);
        cso.evaluate(s);
        
        System.out.println("s = " + s);
        System.out.println("visited points = " + cso.pointsWithStatsComputed());
        
        cso.intelligentSwitchOff(s);
     
        //cso.evaluate(s);
        
        System.out.println("s = " + s);
        System.out.println("visited points = " + cso.pointsWithStatsComputed());
    }

}
