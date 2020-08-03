package jmetal.problems.UDN;
/* 
    -   Router 0 Level 0???
    -   Crear enlace cuando se conecta una BTS
    -   Añadir funciones a los ruters.
    -   Si estamos en el nivel 0 (1) no podemos tener hijos solo BTS conectadas
    -   Create First Level Access Router -> revisar la parte donde conecto las BTS.


*/


/*
 * @author PabloRodriguezDiaz
 */

import static java.lang.Math.abs;
import java.util.ArrayList;
import jmetal.problems.UDN.model.Point;
import jmetal.problems.UDN.model.UDN;



public class AN {

    int MAXLEVELS = 5;
    int MAXDISTANCE = 50; //Mayor distancia entre dos puntos del mapa ¿?
    int MAXLINKS = 3; //MAXIMO NUMERO DE CONEXIONES ENTRE ROUTERS DEL MISMO NIVEL
    int MAXCHILD = 2 ;
    //int id_AN = 0;
    static int id_AccessRouter = 0;
    static int id_Links = 0;
    static int id_Function = 0;
    UDN networkUDN;
    //int R = 5; //Numero de subdiviones que tengo que hacer en el mapa
    ArrayList <AccessRouter> routerList = new ArrayList<>(); //CAMBIAR ARRAYLIST sin int

    
    public AN(){
        
    
    }
    
    
    public AN (UDN nUDN, int R){
        
        networkUDN = nUDN;
        Point grid[][][]; //[X][Y][Z]
        grid = networkUDN.getGrid();
        int division = (int)Math.sqrt(R); // numero total de regiones en las que subdivido la matriz (filas*columnas)
        int increment = (int)(MAXDISTANCE/division);//LA MATRIZ ES CUADRADA
        int level = 1;
        routerList.clear();
        
        
        System. out. print("Llamada para crear el primer nivel de la RA: \n ");        
        createFirstLevelAccessRouter(networkUDN ,grid, division, increment, routerList);
        System. out. print("Llamada para conectar los routers del primer nivel entre ellos: \n "); 
        connectionBetweenLevel(routerList, level);
        int num_routers = countSameLevelRouters(routerList, level);
        //level++;

        for(level = 2 ;  level <= MAXLEVELS && num_routers >=2; level++){

            int next_level_routers = numNextLevelRouters (num_routers);

            for(int r = 0; r<next_level_routers; r++){

                AccessRouter router = new AccessRouter (level, id_AccessRouter, 0, 0);
                System. out. println("Se ha creado el router con id: " +  id_AccessRouter+ " Level: " + level);
                routerList.add(router);
                id_AccessRouter++;
            }
            System. out. print("Llamada para conectar el nivel" + level +" con los routers del nivel inferior \n "); 
            connectionLowLevel(routerList, level);
            System. out. print("Llamada para conectar entre ellos los routers del nivel" + level +": \n "); 
            connectionBetweenLevel(routerList, level);
            num_routers = countSameLevelRouters(routerList, level);
            System. out. print("Tenemos: " + num_routers+ " routers en el mismo nivelÇ: " +level+ "\n"); 
            //level++;
        }

    }

    void connectionLowLevel(ArrayList routerList, int level){
        int connected = 0;

        for (int i = 0; i < routerList.size() && level != 0; i++) {
            AccessRouter router = (AccessRouter)routerList.get(i);

            while (router.getLevel() == level && connected < MAXCHILD){
                //BUSCO los router del nivel inferior que quiero conectar
                AccessRouter lowRouter = searchNearbyRouter(router, level-1, routerList);
                connectRouters(router, lowRouter);
                router.setPosition(lowRouter.getPointX(), lowRouter.getPointY());
                connected++;
            }
            if(connected != 0){
                updateVirtualPosition(router);
                connected = 0;
            }
        }
    }

    void updateVirtualPosition (AccessRouter router){
        ArrayList<AccessRouter> l_son = (ArrayList) router.getConnectedSon();
        int x = 0;
        int y = 0;
        
        for (AccessRouter son_router : l_son){
            x =+ son_router.getPointX();
            y =+ son_router.getPointY();
        } 

        x = x/l_son.size();
        y = y/l_son.size();
        router.setPosition(x, y);

    }


    void connectionBetweenLevel(ArrayList routerList, int nLevel){
        AccessRouter router1;
        AccessRouter router2;

        int num_routers = (countSameLevelRouters(routerList, nLevel));
        for(int i = 0; i < (routerList.size()) && num_routers != 0; i++){
            router1 = (AccessRouter) routerList.get(i);
            
            if(router1.getLevel() == nLevel){
                
                for(int n = 0; n < MAXLINKS; n++){
                    router2 = searchNearbyRouter(router1, nLevel, routerList);
                    connectRouters(router1, router2);
                    System. out. print("conectamos: " + router1.getId()+ " con router en el mismo nivelÇ: " +router2.getId()+ "\n"); 
                }
                num_routers--;
            }
        }
    }
/*
    boolean hasFather (AccessRouter router){
        boolean y = false;
        
        if(router.getConnectedFather().size() != 0){
        AccessRouter father = router.getConnectedFather().get(0);
     
            if (father.getId() != 0) {
                y = true;
            }
        }

        return y;
    }
*/
    private void createFirstLevelAccessRouter (UDN networkUDN, Point[][][] grid, int division, int increment, ArrayList routerList){

        int level = 1;
        int lim_inf_l, lim_sup_l = 0;
        int lim_inf_c, lim_sup_c = 0;


        //Nos vamos moviendo por las regiones en las que estamos subdividiendo el grid de la red UDN.
        for(int l = 1 ; l <= division; l++){
            lim_inf_l = lim_sup_l;
            lim_sup_l = lim_sup_l + increment;
            
            for(int c = 1; c <= division; c++){
                //ESTAMOS DENTRO DE LAS REGIONES DE SUBDIVISION DEL GRID.
                lim_inf_c = lim_sup_c;
                lim_sup_c = lim_sup_c + increment;
                
                System. out. println("\n--R-- Region; Fila: " + l + " Columna: "+ c); 
                //añadir tambien line, column para los argumentos del AccessRouter en los ejes X e Y    
                AccessRouter router = new AccessRouter (level, id_AccessRouter, (lim_inf_l+lim_sup_l)/2, (lim_inf_c+lim_sup_c)/2);
                routerList.add(router);
                System. out. println("Router Primer Nivel; Router id: " + id_AccessRouter+ " Level: "+ router.getLevel()+ "\n"); 
                id_AccessRouter++;

                //Nos movemos por el mapa y comprobamos donde hay una BTS, ANTES tengo que subdividir el espacio con la variable R
                for(int line = lim_inf_l; line < lim_sup_l; line ++){
                    for(int column = lim_inf_c; column < lim_sup_c; column++){

                        //tenemos una BTs isntalada en ese punto, crear AccessRouter y almacenar el nivel y el punto virtual donde se encuentra.
                        //podemos utilizar line, colum para almacenar le punto virtual.
                        Point pnt = networkUDN.getGridPoint(line, column, 0);
                        if(pnt.hasBTSInstalled()){
                            System. out. print("Hay una BTS en: line = " + line +", column = " +column + "\n"); 
                            connectBtsRouter(router, pnt);
                            
                        }
                    }//FOR COLUMN

                }// FOR LINE

            }//FOR C
        }//FOR L

    }



    //Busca el router mas cercano(en el nivel nlevel) a el router que le paso en la cabecera. Se puede utilizar para buscar el router mas cercano
    //en el mismo nivel o en un nivel superior
    AccessRouter searchNearbyRouter(AccessRouter router, int nLevel ,ArrayList routerList){
        AccessRouter nearRouter = new AccessRouter();
        int incX = MAXDISTANCE;
        int incY = MAXDISTANCE;

        if(router.getLevel() == nLevel){//Buscar router mas cercano en el mismo nivel
            int nRouterLevel = countSameLevelRouters(routerList, nLevel);

            for(int i = 0; i < nRouterLevel; i++){
                AccessRouter candidate = (AccessRouter) routerList.get(i);

                if( (candidate.getLevel() == nLevel) && (router.getId() != candidate.getId()) ){ 
                    //No es el mismo router y el candidato es del nivel buscado
                    //Comparar la distancia
                    int distX = pointDistance(router.getPointX(), candidate.getPointX());
                    int distY = pointDistance(router.getPointY(), candidate.getPointY());


                    if( distX <= incX && distY <= incY && !areConnected(router, candidate) && !candidate.hasBrotherConnections()){
                        incX = distX;
                        incY = distY;
                        nearRouter = candidate;
                    }
                }
            }
        }else if (router.getLevel() > nLevel){ //Buscar router mas cercano en el nivel inferior
            int nRouterLevel = countSameLevelRouters(routerList, nLevel);

            for(int i = 0; i < nRouterLevel; i++){

                AccessRouter candidate = (AccessRouter) routerList.get(i);
                if(candidate.getLevel() == nLevel){
                    int distX = pointDistance(router.getPointX(), candidate.getPointX());
                    int distY = pointDistance(router.getPointY(), candidate.getPointY());

                    if( distX < incX && distY < incY && !areConnected(router, candidate) && !candidate.hasFather()){
                        incX = distX;
                        incY = distY;
                        nearRouter = candidate;
                    }
                }
            }
        }else{//Buscamos en el nivel superior al router
            int nRouterLevel = countSameLevelRouters(routerList, nLevel);

            for(int i = 0; i < nRouterLevel; i++){

                AccessRouter candidate = (AccessRouter) routerList.get(i);

                if(candidate.getLevel() == nLevel){
                    int distX = pointDistance(router.getPointX(), candidate.getPointX());
                    int distY = pointDistance(router.getPointY(), candidate.getPointY());

                    if( distX < incX && distY < incY && !areConnected(router, candidate) && candidate.hasChildConnections()){
                        incX = distX;
                        incY = distY;
                        nearRouter = candidate;
                    }
                }
            }
        
        }
        return nearRouter;
    }


    void connectBtsRouter(AccessRouter router, Point pnt){
        if(router.getLevel() == 1){
            
            router.addConnectedBTS(pnt);
            System. out. print("BTs conectada al router "+ router.getId() + "\n"); 
        }
    }

    int pointDistance(int router1X, int router2X){
        int sol = 0;

        if(router1X > router2X){
            sol = abs(router1X - router2X);
        }else{
            sol = abs(router2X - router1X); 
        }

        return sol;
    }

    int numNextLevelRouters(int num_same_level_routers){

        int next_level_routers = 0;

        if (num_same_level_routers%2 == 0){ //even

            next_level_routers = num_same_level_routers/2;

        }else{//odd

            next_level_routers = (int)(num_same_level_routers/2);
        }

        return next_level_routers;
    }


    int countSameLevelRouters(ArrayList routerList, int nLevel){
        int num_routers = 0;
        for(int i = 0; i < (routerList.size()); i++){

            if(nLevel == ( (AccessRouter)routerList.get(i)).getLevel()){
                num_routers++;
            }
        }
        System.out.println("Same level routers" + num_routers);
        return num_routers;

    }

    void connectRouters(AccessRouter router1, AccessRouter router2){
        //DIFERENCIAR SI LOS ROUTERS SON DEL MISMO NIVEL, si es padre, hijo....
        if(router1.getLevel() == router2.getLevel() && !areConnected(router1,router2)){
            //añadir capacidad enlace
            Link link1 = new Link(router1.getLevel(), router2.getLevel(), id_Links, router1, router2);
            id_Links++;
            Link link2 = new Link(router2.getLevel(), router1.getLevel(), id_Links, router2, router1);
            id_Links++;

            router1.addConnectedBrother(router2);
            router1.addLink(link1);
            router2.addConnectedBrother(router1);
            router2.addLink(link2);
            System. out. print("MISMO NIVEL: Se ha conectado router:" + router1.getId()+ " lev(" + router1.getLevel() + ")"+
                " con rotuer:" +router2.getId() + " lev(" + router2.getLevel() + ")"+ "\n"); 

        }else if(router1.getLevel() > router2.getLevel() && !areConnected(router1,router2)){
            //añadir capacidad enlace+
            Link link1 = new Link(router1.getLevel(), router2.getLevel(), id_Links, router1, router2);
            id_Links++;
            Link link2 = new Link(router2.getLevel(), router1.getLevel(), id_Links, router2, router1);
            id_Links++;

            router1.addConnectedSon(router2);
            router1.addLink(link1);
            router2.addConnectedFather(router1);
            router2.addLink(link2);
            
            System. out. print("R1 > R2:  Se ha conectado router:" + router1.getId()+ " lev(" + router1.getLevel() + ")"+
                " con rotuer:" +router2.getId() + " lev(" + router2.getLevel() + ")"+ "\n"); 

        }else if(router1.getLevel() < router2.getLevel() && !areConnected(router1,router2)){
            //añadir capacidad enlaces
            Link link1 = new Link(router1.getLevel(), router2.getLevel(), id_Links, router1, router2);
            id_Links++;
            Link link2 = new Link(router2.getLevel(), router1.getLevel(), id_Links, router2, router1);
            id_Links++;

            router1.addConnectedFather(router2);
            router1.addLink(link1);
            router2.addConnectedSon(router1);
            router2.addLink(link2);
            System. out. print("R1 < R2: Se ha conectado router:" + router1.getId()+ " lev(" + router1.getLevel() + ")"+
                " con rotuer:" +router2.getId() + " lev(" + router2.getLevel() + ")"+ "\n"); 
        }
    }

    boolean areConnected (AccessRouter router1, AccessRouter router2){
        boolean check = false;

        ArrayList<Link> linkList = router1.getLinks();

        if (linkList.isEmpty()) {
            
            check = false;

        }else {
            AccessRouter destination_router;
            
            for(Link enlace: linkList){
                destination_router = enlace.getDestination();
                if(destination_router.getId() == router2.getId()){
                    check = true;
                }
            }
        }

        return check;
    }

    
    ArrayList<AccessRouter> getRoutersList(){
        return routerList;
    }

}//Class