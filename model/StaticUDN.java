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
import java.util.List;
import java.util.Properties;
import jmetal.problems.UDN.model.cells.BTS;
import jmetal.util.PPP;

/**
 *
 * @author paco
 */
public class StaticUDN extends UDN {

    /**
     * Default constructor
     */
    public StaticUDN(String mainConfigFile,
            int run) {
        super(mainConfigFile, run);

        
        //load Users info
        loadUsers(staticUserConfigFile_);

        //load hethetnet confir
        loadHetHetNetConfig(hetNetConfigFile_);

    }

    /**
     * Load the config file with the different type of users
     *
     * @param configFile The filename of the configuration file
     */
    private void loadUsers(String configFile) {
        Properties pro = new Properties();

        try {
            System.out.println("Loading users config file...");
            pro.load(new FileInputStream(configFile));

            this.usersTypes_ = Integer.parseInt(pro.getProperty("numUserTypes"));
            this.usersConfig_ = new ArrayList<String>();
            for (int i = 0; i < this.usersTypes_; i++) {
                this.usersConfig_.add(pro.getProperty("type" + i));
            }
            //generate users
            this.users_ = new ArrayList<User>();
            loadUserConfig(this.users_, this.usersConfig_);

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
    private void loadUserConfig(List<User> users, List<String> configs) {
        //variables
        Properties pro = new Properties();
        PPP ppp = new PPP(DynamicUDN.random_);

        int numFemtoAntennas;
        int numPicoAntennas;
        int numMicroAntennas;
        int numMacroAntennas;
        int id = 0;
        double bw;
        String typename;

        for (int config = 0; config < configs.size(); config++) {
            System.out.println("Loading user config file: " + configs.get(config));
            try {
                pro.load(new FileInputStream(configs.get(config)));
            } catch (IOException e) {
                System.out.println(e + "Error loading properties: " + configs.get(config));
                System.exit(-1);
            }

            //loading parameters
            bw = Double.parseDouble(pro.getProperty("trafficDemand", "1"));
            typename = pro.getProperty("userTypename");
            numFemtoAntennas = Integer.parseInt(pro.getProperty("numFemtoAntennas", "4"));
            numPicoAntennas = Integer.parseInt(pro.getProperty("numPicoAntennas", "4"));
            numMicroAntennas = Integer.parseInt(pro.getProperty("numMicroAntennas", "4"));
            numMacroAntennas = Integer.parseInt(pro.getProperty("numMacroAntennas", "4"));

            //load the number of cells of with this configuration
            int numUsers = Integer.parseInt(pro.getProperty("numUsers", "10"));
            //int numUsers = 15;
            double lambda = Double.parseDouble(pro.getProperty("lambdaForPPP", "50"));
            double mu = this.gridPointsY_ * this.gridPointsX_ * this.interPointSeparation_ * this.interPointSeparation_;
            mu = mu / (1000000.0);
            //uncomment for PPP distributions
            numUsers = ppp.getPoisson(lambda * mu);
            System.out.println("nUsers: " + numUsers);

            for (int u = 0; u < numUsers; u++) {
                //randomize the position
                int x = random_.nextInt(gridPointsX_);
                int y = random_.nextInt(gridPointsY_);
                int z = random_.nextInt(gridPointsZ_);

                User user = new User(
                        id,
                        x,
                        y,
                        z,
                        bw,
                        typename,
                        true,
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
     * Load the main parameters of the instance
     *
     * @param configFile The filename of the configuration file
     */
    private void generateSocialAttractors() {

            System.out.println("Generating Social Attractors config file...");

            if ((this.users_ == null) || (this.users_.size() == 0)) {
                System.out.println("Error generating social attractors. Missing users info.");
            }
            int numberOfSAs = this.users_.size() / 10;
            System.out.println("Number of SAs: " + numberOfSAs);

            socialAttractors_ = new ArrayList<>();
            for (int sa = 0; sa < numberOfSAs; sa++) {
                //randomize the position
                int x = random_.nextInt(gridPointsX_);
                int y = random_.nextInt(gridPointsY_);
                int z = random_.nextInt(gridPointsZ_);
                socialAttractors_.add(new SocialAttractor(sa, x, y, z));
            }

    }

    

    /**
     * Load the config for using the procedure in M. Mirahsan, R. Schoenen, and
     * H. Yanikomeroglu, “HetHetNets: Heterogeneous Traffic Distribution in
     * Heterogeneous Wireless Cellular Networks,” IEEE J. Sel. Areas Commun.,
     * vol. 33, no. 10, pp. 2252–2265, 2015.
     *
     * @param deploymentconf The configuration parameters
     */
    private void loadHetHetNetConfig(String deploymentconf) {
        //0.- Load properties
        Properties pro = new Properties();
        FileInputStream fis = null;
        //double alpha = 0.0, meanBeta = 0.0;
        try {
            //System.out.println("Loading HetHetNet deployment config file...");
            fis = new FileInputStream(deploymentconf);
            pro.load(fis);
            //1.- load the alpha and beta value for attracting SAs and Users
            this.alphaHetHetNet_ = Double.parseDouble(pro.getProperty("alpha", "0.1"));
            this.meanBetaHetHetNet_ = Double.parseDouble(pro.getProperty("meanBeta", "0.1"));
            fis.close();
        } catch (IOException e) {
            System.out.println(e + "Error loading properties: " + deploymentconf);
            System.exit(-1);
        }
        
        //generate social attractos
        generateSocialAttractors();


        //proceed to move SAs and UEs
        hetHetNet();
    }

    /**
     * Deploy BTSs and users following the procedure in M. Mirahsan, R.
     * Schoenen, and H. Yanikomeroglu, “HetHetNets: Heterogeneous Traffic
     * Distribution in Heterogeneous Wireless Cellular Networks,” IEEE J. Sel.
     * Areas Commun., vol. 33, no. 10, pp. 2252–2265, 2015.
     *
     * @param deploymentconf The configuration parameters
     */
    public void hetHetNet() {

        //2.- Move every SA towards its closest BTS in terms of the
        //    received signal power, by a factor of alpha
        for (SocialAttractor sa : this.socialAttractors_) {
            Point p = getGridPoint(sa.x_, sa.y_, sa.z_);
            BTS b = p.getCellWithHigherReceivingPower().getBTS();
            sa.moveSATowardsBTS(b, this.alphaHetHetNet_);
        }

        //3.- Move every user towards its closest SA in terms of the Euclidean
        //    distance, by a factor of beta
        for (User u : this.users_) {
            Point p = getGridPoint(u.getX(), u.getY(), u.getZ());
            SocialAttractor sa = p.getClosestSA(this);

            u.moveUserTowardsSA(sa, this, this.meanBetaHetHetNet_);
        }
    }

}
