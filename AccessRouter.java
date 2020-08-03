package jmetal.problems.UDN;


import java.util.ArrayList;
import jmetal.problems.UDN.model.Point;

/**
 * @author PabloRodriguezDiaz
 * 
 *      Podrán tener conectado un unico Router Padre (Nivel superior)
 *      Podrán tener conectado 3 o menos Router Hijos (Nivel inferior)
 */

public class AccessRouter{


    int id;
    int level;
    int posX; //CAMBIO POR VAR TIPO PUNTO-------------------------------
    int posY; //CAMBIO POR VAR TIPO PUNTO-------------------------------
    ArrayList<Point> connectedBTS = new ArrayList<>();
    ArrayList<AccessRouter> connectedChild = new ArrayList<>();
    //AccessRouter connectedFather = new AccessRouter();
    ArrayList<AccessRouter> connectedFather = new ArrayList<>();
    ArrayList<AccessRouter> connectedBrothers = new ArrayList<>();
    ArrayList<Function> functionList = new ArrayList<>();
    ArrayList<Link> linkList = new ArrayList<>();
    int MAXCHILD = 2;
    int MAXBROTHERS = 3;

    AccessRouter (){

        level = 0;
        id = 0;
        posX = 0; //CAMBIO POR VAR TIPO PUNTO-------------------------------
        posY = 0; //CAMBIO POR VAR TIPO PUNTO-------------------------------
    }
    AccessRouter(int nLevel, int nId, int nX, int nY){ //CAMBIO POR VAR TIPO PUNTO-------------------------------

        level = nLevel;
        id = nId;
        posX = nX; //CAMBIO POR VAR TIPO PUNTO-------------------------------
        posY = nY; //CAMBIO POR VAR TIPO PUNTO-------------------------------
    }


    void setLevel(int nLevel){
        level = nLevel;
    }

    void setId(int nId){
        id = nId;
    }

    void setPosition(int nX, int nY){//CAMBIO POR VAR TIPO PUNTO-------------------------------
        posX = nX;
        posY = nY;
    }

    void addConnectedBTS(Point pointBTS){
        connectedBTS.add(pointBTS);
    }

    void addConnectedSon(AccessRouter router){
        connectedChild.add(router);
    }

    void addConnectedFather(AccessRouter router){
        connectedFather.add(router);
    }

    void addConnectedBrother(AccessRouter router){
        connectedBrothers.add(router);
    }

    void addFunctions (Function nFunction){
        functionList.add(nFunction);
    }

    void addLink (Link link){
        linkList.add(link);
    }

    void deleteConnectedBTS(Point pointBTS){
        connectedBTS.remove(pointBTS);
    }

    int getLevel(){
        return level;
    }

    int getId(){
        return id;
    }

    int getPointX(){
        return posX;
    }

    int getPointY(){
        return posY;
    }

    ArrayList<Point> getConnectedBTS() {
        return connectedBTS;
    }

    ArrayList<AccessRouter> getConnectedSon() {
        return connectedChild;
    }

    ArrayList<AccessRouter> getConnectedFather() {
        return connectedFather;
    }

    ArrayList<AccessRouter> getConnectedBrothers() {
        return connectedBrothers;
    }

    ArrayList<Function> getFunctions() {
        return functionList;
    }

    ArrayList<Link> getLinks() {
        return linkList;
    }
    
    boolean hasFather(){
        boolean ok;
        
        if(connectedFather.isEmpty()){
            ok =false;
        }else{
            ok = true;
        }
       return ok;
    }
    
    boolean hasChildConnections(){
        boolean complete;
        
        if(connectedChild.size() < MAXCHILD){
            complete = false;
        }else{
            complete = true;
        }
      return complete;
    }
    
    boolean hasBrotherConnections(){
        boolean complete;
        
        if(connectedBrothers.isEmpty()){
            complete =false;
        }else if(connectedBrothers.size() < MAXBROTHERS){
            complete = false;
        }else{
            complete = true;
        }
      return complete;
    }
}
