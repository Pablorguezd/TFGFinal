package jmetal.problems.UDN;

import jmetal.problems.UDN.model.Point;

/**
 * @author PabloRodriguez
 */

public class Link{


    int id;
    int origin_level;
    int dest_level;
    int capacity;
    AccessRouter origin;
    AccessRouter destination;
    Point originBTS;

    //Para conectar dos Routers de la Red de Acceso entre ellos
    Link(int oLevel, int dLevel ,int nId, AccessRouter originAR, AccessRouter destinationAR){
        
        id = nId;
        origin_level = oLevel;
        dest_level = dLevel;
        capacity = 0;
        origin = originAR;
        destination = destinationAR;
    }

    //Para conectar una BTS con un Router de Acceso, SOLO PARA EL NIVEL 0 DE LA RED DE ACCESO
    Link(int oLevel, int dLevel, int nId, Point originPoint, AccessRouter destinationAR){

        id = nId;
        origin_level = oLevel;
        dest_level = dLevel;
        capacity = 0;
        origin = null;
        destination = destinationAR;
        originBTS = originPoint;
    }

    void setOriginLevel(int oLevel){
        origin_level = oLevel;
    }

    void setDestinationLevel(int dLevel){
        dest_level = dLevel;
    }

    void setId(int nId){
        id = nId;
    }

    void setCapacity(int nCapacity){
        capacity = nCapacity;
    }

    void setOrigin(AccessRouter originAR){
       origin = originAR;
    }

    void setDestination(AccessRouter destinationAR){
        destination = destinationAR;
    }

    AccessRouter getOrigin(){ // Si es null el origen es una BTS
        return origin;
    }

    AccessRouter getDestination(){
        return destination;
    }


}
