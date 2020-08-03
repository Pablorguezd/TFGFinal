/**
 * @author PabloRodriguez
 */

package jmetal.problems.UDN;

public class Function{

   int id;
   int cpu;
   int ram;
   int disco;

    Function(){
        id = 0;
        cpu = 0;
        ram = 0;
        disco = 0;
    }

    Function(int id_f, int cpu_f, int ram_f, int disco_f){
        id = id_f;
        cpu = cpu_f;
        ram = ram_f;
        disco = disco_f;
    }

}