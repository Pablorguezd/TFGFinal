//https://es.mathworks.com/help/matlab/matlab_external/start-matlab-session-from-java.html
package jmetal.problems.UDN;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLNumericArray;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.problems.UDN.simulation.Simulation;
import jmetal.problems.UDN.model.UDN;

import jmetal.util.JMException;
import com.mathworks.engine.*; 
import java.io.IOException;
import java.util.Vector;

public class MatlabCapacity{     
    public static void main(String[] args) throws Exception{ 
        //String[] myEngine = {"myMatlabEngine"}; 
       // MatlabEngine eng = MatlabEngine.startMatlab(); 
        String path = "MIMO";
        char[] rute= new char[path.length()];
        for (int i =0; i<path.length(); i++){
            rute[i]=path.charAt(i);
        }
        //eng.feval("cd",rute);
        //double[][] capacity= eng.feval("Calcula_Capacidad_para_PABLO");
//        for(int i=0; i<capacity.length; i++){
//            for(int j=0; j<capacity[0].length; j++){
//                System.out.print(capacity[i][j] + " ");
//            }
//            System.out.println("");
//        }
        
        
        //System.out.println("LONG: "+engines.length +"     " +engines);
//        MatlabEngine eng = MatlabEngine.connectMatlab(engines[0]);
//        eng.putVariable("prueba", 100);
    
    }
}
