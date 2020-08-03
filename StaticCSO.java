package jmetal.problems.UDN;

import java.util.ArrayList;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.problems.UDN.model.StaticUDN;
import jmetal.util.JMException;

/**
 * Class representing problem ZDT1
 */
public class StaticCSO extends CSO {

    
    /**
     * Creates an instance of the Static CSO problem
     *
     */
    public StaticCSO(String mainConfig,
            int run) throws ClassNotFoundException {

        //Create the UDN model
        udn_ = new StaticUDN(mainConfig, run);

        numberOfVariables_ = 1;
        numberOfObjectives_ = 2;
        numberOfConstraints_ = 0;
        problemName_ = "StaticCSO";
        run_ = run;

        solutionType_ = new BinarySolutionType(this);

        length_ = new int[numberOfVariables_];
        length_[0] = udn_.getTotalNumberOfActivableCells();

        //udn_.printXVoronoi();
        //System.exit(-1);
    }


    /**
     * Evaluates a solution.
     *
     * @param solution The solution to evaluate.
     * @throws JMException
     */
    @Override
    public void evaluate(Solution solution) throws JMException {
        Binary cso = ((Binary) solution.getDecisionVariables()[0]);

        //map the activation to the udn
        udn_.setCellActivation(cso);

        //update the avera
        udn_.computeSignaling();

        double capacity = networkCapacity(solution);
        double powerConsumption = powerConsumptionPiovesan();
        solution.setObjective(0, powerConsumption);
        solution.setObjective(1, -capacity);
        
        
        AN AccessNetwork = new AN(udn_, (int)5);
        System. out. println("Se ha hecho llamada a AN");
        ArrayList<AccessRouter> router_list= AccessNetwork.getRoutersList();
        
        for (AccessRouter router: router_list){
            System. out. println("");
            System. out. println("RouterID: " + router.getId() + " Level: " +router.getLevel()); 
            System. out. println("BTS: " + router.connectedBTS.size());
            System. out. println("Sons: " + router.connectedChild.size());
            System. out. println("Father: " + router.connectedFather.size());
            System. out. println("Brothers: " + router.connectedBrothers.size());          
            System. out. println("Functions: "+ router.functionList.size());
            System. out. println("Links: " + router.linkList.size());
            System. out. println("");
            
        } 


    } // evaluate

    
} // Planning UDN
