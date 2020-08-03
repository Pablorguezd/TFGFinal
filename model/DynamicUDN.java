/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.problems.UDN.model;

import jmetal.problems.UDN.model.users.User;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.util.PPP;
import com.jmatio.io.*;
import com.jmatio.types.*;
import java.util.List;
import static jmetal.problems.UDN.model.UDN.random_;

/**
 *
 * @author paco
 */
public class DynamicUDN extends UDN {

    //Use shanghai function to update positions of the users
    private boolean shanghaiMobility_ = false;

    //Mobility info from matrix file
    MLStructure VS_Node_;

    /**
     * Default constructor
     */
    public DynamicUDN(String mainConfigFile,
            int run) {
        super(mainConfigFile, run);

        //Opens the matlab file only once
        MatFileReader users_reader = null;

        //reading input from Matlab file
        try {
            users_reader = new MatFileReader(this.mobilityMatrixFile_);
        } catch (IOException ex) {
            Logger.getLogger(DynamicUDN.class.getName()).log(Level.SEVERE, null, ex);
        }
        MLStructure UsersMatlab;
        UsersMatlab = (MLStructure) users_reader.getContent().get("UE");
        VS_Node_ = (MLStructure) UsersMatlab.getField("VS_NODE");

        //load Users info
        boolean demands;
        if (this.mobilityType_.equalsIgnoreCase("matrix")) {
            loadDynamicUsers(this.dynamicUserConfigFile_, demands = false);
        } else if (this.mobilityType_.equalsIgnoreCase("userDemands")) {
            loadDynamicUsers(this.dynamicUserConfigFile_, demands = true);
        } else if (this.mobilityType_.equalsIgnoreCase("shanghai")) {
            loadDynamicUsers(this.dynamicUserConfigFile_, demands = false);
            shanghaiMobility_ = true;
        } else {
            System.out.println("Incorrect mobility type: " + this.mobilityType_);
            System.exit(-1);
        }

        //load 
    }

    /**
     * Load the config file with the different type of users
     *
     * @param configFile The filename of the configuration file
     */
    private void loadDynamicUsers(String configFile, boolean demands) {
        Properties pro = new Properties();

        try {
            System.out.println("Loading dynamic users config file...");
            pro.load(new FileInputStream(configFile));

            this.usersTypes_ = Integer.parseInt(pro.getProperty("numDynamicUserTypes"));
            this.usersConfig_ = new ArrayList<String>();
            for (int i = 0; i < this.usersTypes_; i++) {
                this.usersConfig_.add(pro.getProperty("type" + i));
            }
            //generate users
            this.users_ = new ArrayList<User>();
            loadDynamicUserConfig(this.users_, this.usersConfig_, demands);

        } catch (IOException e) {
            System.out.println(e + "Error loading properties: " + configFile);
            System.exit(-1);
        }
    }

    /**
     * Load the configuration for each particular type of user
     *
     * @param users_ The data structure to store the users
     * @param usersConfig_ The filename of the configuration file
     */
    private void loadDynamicUserConfig(List<User> users, List<String> configs, boolean demands) {
        //variables
        Properties pro = new Properties();
        PPP ppp = new PPP(DynamicUDN.random_);

        int id = 0;
        int numUsers;

        for (int config = 0; config < configs.size(); config++) {
            System.out.println("Loading dynamic user config file: " + configs.get(config));
            try {
                pro.load(new FileInputStream(configs.get(config)));
            } catch (IOException e) {
                System.out.println(e + "Error loading properties: " + configs.get(config));
                System.exit(-1);
            }

            //load user parameters
            double userDemand = Double.parseDouble(pro.getProperty("trafficDemand"));
            String typename = pro.getProperty("userTypename");
            int numFemtoAntennas = Integer.parseInt(pro.getProperty("numFemtoAntennas"));
            int numPicoAntennas = Integer.parseInt(pro.getProperty("numPicoAntennas"));
            int numMicroAntennas = Integer.parseInt(pro.getProperty("numMicroAntennas"));
            int numMacroAntennas = Integer.parseInt(pro.getProperty("numMacroAntennas"));
            double normalDemand = Double.parseDouble(pro.getProperty("normalDemand"));
            double heavyDemand = Double.parseDouble(pro.getProperty("heavyDemand"));
            double heavyUsersRatio = Double.parseDouble(pro.getProperty("heavyUserRatio"));

            //load number of users in the network
            double lambda = Double.parseDouble(pro.getProperty("lambdaForPPP"));
            double mu = this.gridPointsY_ * this.gridPointsX_ * this.interPointSeparation_ * this.interPointSeparation_;
            mu = mu / (1000000.0);
            //uncomment for PPP distributions
            numUsers = ppp.getPoisson(lambda * mu);
            System.out.println("nUsers: " + numUsers);

            //establish  user generic parameters
            double activationRate = getUserActivity(0);
            boolean isActive = true;

            //extracting initial positions of users
            for (int u = 0; u < numUsers; u++) {

                if (demands == true) {
                    //set activation and usertype according to the given rates
                    typename = "normal";
                    userDemand = normalDemand;
                    double rnd = getRandom().nextDouble();
                    if (rnd > activationRate) {
                        isActive = false;
                    }

                    rnd = getRandom().nextDouble();
                    if (rnd <= heavyUsersRatio) {
                        typename = "heavy";
                        userDemand = heavyDemand;
                    }
                }

                MLNumericArray V_Time = (MLNumericArray) VS_Node_.getField("V_TIME", u);
                MLNumericArray V_Position_X = (MLNumericArray) VS_Node_.getField("V_POSITION_X", u);
                MLNumericArray V_Position_Y = (MLNumericArray) VS_Node_.getField("V_POSITION_Y", u);
                MLNumericArray V_Position_Z = (MLNumericArray) VS_Node_.getField("V_POSITION_Z", u);

                float xf = V_Position_X.getReal(0).floatValue();
                int x = Math.round(xf);
                float yf = V_Position_Y.getReal(0).floatValue();
                int y = Math.round(yf);
                float zf = V_Position_Z.getReal(0).floatValue();
                int z = Math.round(zf);

                User user = new User(
                        id,
                        x,
                        y,
                        z,
                        userDemand,
                        typename,
                        isActive,
                        numFemtoAntennas,
                        numPicoAntennas,
                        numMicroAntennas,
                        numMacroAntennas
                );

                users.add(user);
                //update id
                id++;

            }
        }
    }


    /**
     * Update the current user position based on
     *
     * @param time
     * @return
     */
    public boolean updateUsersPositionFromMatrix(int time) {
        boolean modified = false;
        int numUsers = this.users_.size();

        //looking for changes in the positions
        for (int k = 0; k < numUsers; k++) {

            MLNumericArray V_Time = (MLNumericArray) VS_Node_.getField("V_TIME", k);
            MLNumericArray V_Position_X = (MLNumericArray) VS_Node_.getField("V_POSITION_X", k);
            MLNumericArray V_Position_Y = (MLNumericArray) VS_Node_.getField("V_POSITION_Y", k);
            MLNumericArray V_Position_Z = (MLNumericArray) VS_Node_.getField("V_POSITION_Z", k);

            //Check if the user is moving in this certain instant
            for (int i = 0; i < V_Time.getM(); i++) {
                int instant = V_Time.getReal(i).intValue();
                if (instant == time) {
                    float xf = V_Position_X.getReal(i).floatValue();
                    int x = Math.round(xf);

                    float yf = V_Position_Y.getReal(i).floatValue();
                    int y = Math.round(yf);

                    float zf = V_Position_Z.getReal(i).floatValue();
                    int z = Math.round(zf);

                    //modify the user's position
                    this.users_.get(k).setX(x);
                    this.users_.get(k).setY(y);
                    this.users_.get(k).setZ(z);

//                    System.out.println("The user " + k + " has changed his position");
//                    System.out.println("time= " + time + ", Position= (" + x + "," + y + "," + z + ")");
                    modified = true;
                    break;
                }

            }

        }

        return modified;
    }

    public boolean updateUsersPositionShanghai(int time) {
        boolean modified = false;
        int numUsers = this.users_.size();

        int initTime = 150; //movement in shanghai does not start in 0
        //looking for changes in the positions
        for (int k = 0; k < numUsers; k++) {
            //MLNumericArray V_Time = (MLNumericArray) VS_Node.getField("V_TIME", k);
            MLNumericArray V_Position_X = (MLNumericArray) VS_Node_.getField("V_POSITION_X", k);
            MLNumericArray V_Position_Y = (MLNumericArray) VS_Node_.getField("V_POSITION_Y", k);

            //Check if the user is moving in this certain instant
            float xf = V_Position_X.getReal(time + initTime).floatValue();
            int x = Math.round(xf);

            float yf = V_Position_Y.getReal(time + initTime).floatValue();
            int y = Math.round(yf);
            int z = 0;

            //if user is not active, deactivate it
            if (xf == 0 && yf == 0 && z == 0) {
                this.users_.get(k).deactivateUser();
            } else {
                modified = true;
                this.users_.get(k).setX(x);
                this.users_.get(k).setY(y);
                //randomize z
                z = this.getRandom().nextInt(gridPointsZ_);
                this.users_.get(k).setZ(z);
            }
            //modify the user's position
            break;

        }

        return modified;
    }

    /**
     * returns the share of active subscribers at time "time" (%)
     *
     * @param time
     * @param interval
     */
    public double getUserActivity(int time) {
        double activity;
        int actualTime = (time) % 1440;

        if ((actualTime > 120) & (actualTime < 600)) { // entre las 2 y las 10
            activity = 0.05;
        } else if ((actualTime >= 600) & (actualTime < 1080)) {//entre las 10 y las 18
            activity = 0.09;
        } else {
            activity = 0.13;
        }
        //activity = activity;
        //return 1;

        return activity;
    }

    /**
     * Get the number of active users
     *
     * @return
     */
    public int getNumberOfActiveUsers() {
        int activeUsers = 0;
        for (User u : this.users_) {
            if (u.isActive()) {
                activeUsers++;
            }
        }
        return activeUsers;
    }

    /**
     * Number of heavy users
     *
     * @return
     */
    public int getNumberOfHeavyUsers() {
        int heavyUsers = 0;
        for (User u : this.users_) {
            if (u.isActive() & (u.getUserTypename() == "heavy")) {
                //if(u.getUserTypename()=="heavy"){
                heavyUsers++;
            }
        }
        return heavyUsers;
    }

    /**
     * Update the activation of the users given a instant time
     *
     * @param time
     */
    public void updateUsersActivation(int time) {
        int numUsers = this.users_.size();
        double currentActivity = this.getUserActivity(time);
        int must = (int) (numUsers * currentActivity);
        int usersToUpdate = ((int) (numUsers * currentActivity)) - getNumberOfActiveUsers();
        Random rnd = this.getRandom();

        int count = 0;
        //System.out.println("NUM ACTIVE USERS BEFORE: " + this.getNumberOfActiveUsers());

        if (usersToUpdate < 0) { //A set of users must be deactivated
            rnd.nextInt(this.users_.size());
            while (count < Math.abs(usersToUpdate)) {
                //TODO find a better way: too inneficient
                for (User u : this.users_) {
                    if (u.isActive() & (u.getID() == (rnd.nextInt(this.users_.size())))) {
                        u.deactivateUser();
                        count++;
                    }
                }
            }
        } else if (usersToUpdate > 0) { //A set of users must be activated
            while (count < Math.abs(usersToUpdate)) {
                //TODO find a better way: too inneficient
                for (User u : this.users_) {
                    if (!u.isActive() & (u.getID() == (rnd.nextInt(this.users_.size())))) {
                        u.activateUser();
                        count++;
                    }
                }
            }
        }
        System.out.println("ACTIVATION RATIO: " + currentActivity);
        System.out.println("Num active users: " + this.getNumberOfActiveUsers() + "(" + this.getNumberOfHeavyUsers() + " heavy)");
    }

    /**
     * Computes the current resource supply, user capacity demand and link
     * capacity of the whole network at the actual instant
     *
     * @return provisioning indicators (st, dt ser capacity ct)
     *
     */
    public double[] computeProvisioning() {
        double[] provisioning = new double[3];
        //dt=resource demand
        double dt = 0;
        //st= resource supply
        double st = 0;
        //ct = capacity
        double ct = 0;

        for (User u : this.getUsers()) {
            if (u.isActive()) {
                double userCapacity = 0;
                double userDemand = 0;
                //userCapacity += u.capacityMIMO(this, u.getServingCell().getSharedBWForAssignedUsers());
                userCapacity = u.capacity(this, u.getServingCell().getSharedBWForAssignedUsers());
                userDemand = u.getTrafficDemand() / 1000;

                //If the user demand is satisfied (capacity>demand), the user will "consume" its demand
                //if not satisfied, the upper bound is the capcity of the link
                st += Math.min(userCapacity, userDemand);
                dt += userDemand;
                ct += userCapacity;
            }
        }

        provisioning[0] = st;
        provisioning[1] = dt;
        provisioning[2] = ct;

        return provisioning;
    }

    /**
     * Computes the global provisioning stats of the network until the current
     * time instant in the simulation
     *
     * @param st accumulated power supply
     * @param dt accumulated user capacity demand
     * @param ct accumulated user link capacity
     * @param interval time interval between two simulation instants
     * @param t current time
     * @return Provisioning metrics tu, qu, pu
     */
    public double[] computeProvisioningStats(double[] st, double[] dt, double[] ct, int interval, int t) {
        double stats[] = new double[3];  //stats[0]=tu, stats[1]=qu, stats[2] = pu

        //compute tu (underprovisioning time share)
        double sum = 0;
        for (int i = 0; i < t; i++) {
            if (dt[i] > st[i]) {
                sum += interval;
            }
        }
        stats[0] = (100 * sum) / (t * interval);

        //compute qu (underprovisioning accuracy)
        sum = 0;
        for (int j = 0; j < t; j++) {
            if (dt[j] > st[j]) {
                sum += ((dt[j] - st[j]) / dt[j]) * interval;
            }
        }
        stats[1] = (100 * sum) / (t * interval);

        //compute pu (overprovisioning time share)
//        sum = 0;
//        for(int k=0; k<t; k++){
//            if(ct[k] > dt[k]){
//                sum += ((ct[k] - dt[k])/ct[k])*interval;
//            }            
//        }
//        
        //compute pu (overprovisioning accuracy)
        sum = 0;
        for (int k = 0; k < t; k++) {
            double over = ct[k] - dt[k];
            if (over > 0) {
                sum += (over / ct[k]) * interval;
            }

            if (t == 1) {
                sum = 0;
            }

        }

        stats[2] = (100 * sum) / (t * interval);

        System.out.println("Tu (instant " + t + "): " + stats[0]);
        System.out.println("Qu (instant" + t + "): " + stats[1]);
        System.out.println("Pu (instant" + t + "): " + stats[2]);

        return stats;
    }

}
