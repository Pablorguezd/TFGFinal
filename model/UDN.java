/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.problems.UDN.model;

import jmetal.problems.UDN.model.users.User;
import jmetal.problems.UDN.model.cells.Cell;
import java.io.FileInputStream;
import java.io.IOException;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.TreeMap;
import jmetal.encodings.variable.Binary;
import jmetal.problems.UDN.model.cells.BTS;
import jmetal.util.PPP;
import java.util.HashMap;

/**
 *
 * @author paco
 */
public abstract class UDN {

    //Users
    int usersTypes_;
    List<String> usersConfig_;

    //terrain parameters
    public int gridPointsX_;
    public int gridPointsY_;
    public int gridPointsZ_;

    int interPointSeparation_;
    int terrainWidth_;
    int terrainHeight_;

    Point grid[][][];

    /**
     * Propagation parameters
     */
    int numPropagationRegions_;
    List<Region> propRegions_;

    /**
     * Cells
     */
    public enum CellType {
        MACRO, MICRO, PICO, FEMTO
    };

    public Map<Double, List<Cell>> cells_;

    /**
     * Social Attractors
     */
    List<SocialAttractor> socialAttractors_;
    double alphaHetHetNet_;
    double meanBetaHetHetNet_;

    /**
     * Users
     */
    List<User> users_;

    /**
     * Misc
     */
    public static Random random_;
    static long seed_;
    long[] seeds_array = {6576946, 15806277, 10306509, 561099, 16435990,
        1904859, 4080598, 16030968, 24070723, 5330056,
        6787642, 11449375, 1801157, 11115944, 10621377,
        14089326, 23581748, 12652207, 22237788, 1781623,
        18336580, 22180257, 14134887, 9970106, 5431969,
        24679879, 544331, 13649575, 15359920, 7049695,
        15520211, 13756445, 7986543, 21061943, 10101328,
        19447250, 5018584, 25009560, 16699832, 10563679,
        7488476, 2692010, 17385187, 2484487, 12358331,
        10457156, 22266884, 18504290, 5847318, 23121394};

    

    //Filenames of the configuration files
    String cellConfigFile_;
    String hetNetConfigFile_;
    String staticUserConfigFile_;
    String mobilityType_;

    String dynamicUserConfigFile_;
    String mobilityMatrixFile_;

    /**
     * Default constructor
     * @param mainConfigFile Filename of the main configuration file
     * @param run The run to generate the same problem instances
     */
    public UDN(String mainConfigFile,
            int run) {

        //load cell configurations
        this.loadMainParameters(mainConfigFile);

        //random number
        if (run == -1) {
            random_ = new Random();
        } else {
            random_ = new Random(this.seeds_array[run]);
        }

        //Generate the grid
        grid = new Point[gridPointsX_][gridPointsY_][gridPointsZ_];
        for (int i = 0; i < gridPointsX_; i++) {
            grid[i] = new Point[gridPointsY_][];
            for (int j = 0; j < gridPointsY_; j++) {
                //grid[i][j] = new Point(i*this.interPointSeparation_, j*this.interPointSeparation_, 0);
                grid[i][j] = new Point[gridPointsZ_];
                for (int k = 0; k < gridPointsZ_; k++) {
                    grid[i][j][k] = new Point(this, i, j, k);
                }

            }
        }

        //Generate the propagations regions and the Voronoi partition
        propRegions_ = new ArrayList<>();
        for (int i = 0; i < numPropagationRegions_; i++) {
            int x = random_.nextInt(gridPointsX_);
            int y = random_.nextInt(gridPointsY_);
            propRegions_.add(new Region(i, x, y));
        }
        computeVoronoiPropagationRegion(propRegions_);

        //load cells
        loadCells(cellConfigFile_);
    }

    /**
     * Load the main parameters of the instance
     *
     * @param configFile The filename of the configuration file
     */
    private void loadMainParameters(String configFile) {
        Properties pro = new Properties();

        try {
            System.out.println("Loading main config file...");
            pro.load(new FileInputStream(configFile));

            this.gridPointsX_ = Integer.parseInt(pro.getProperty("gridPointsX"));
            this.gridPointsY_ = Integer.parseInt(pro.getProperty("gridPointsY"));
            this.gridPointsZ_ = Integer.parseInt(pro.getProperty("gridPointsZ"));
            this.interPointSeparation_ = Integer.parseInt(pro.getProperty("interPointSeparation"));
            //update the width and height given the interpoint separation
//            this.gridPointsY_ = 20;
//            this.gridPointsX_ = 20;
            terrainWidth_ = gridPointsX_ * interPointSeparation_;
            terrainHeight_ = gridPointsY_ * interPointSeparation_;

//            
            this.seed_ = Long.parseLong(pro.getProperty("seed", "2889123676182312233221"));
            this.numPropagationRegions_ = Integer.parseInt(pro.getProperty("propagationRegions"));

            this.cellConfigFile_ = pro.getProperty("cellConfigFile");

            this.hetNetConfigFile_ = pro.getProperty("hetNetConfigFile");
            this.staticUserConfigFile_ = pro.getProperty("staticUserConfigFile");
            this.mobilityType_ = pro.getProperty("mobility");
            this.dynamicUserConfigFile_ = pro.getProperty("dynamicUserConfigFile");
            this.mobilityMatrixFile_ = pro.getProperty("matrixFile");

        } catch (IOException e) {
            System.out.println(e + "Error loading properties: " + configFile);
            System.exit(-1);
        }
    }

    /**
     * Load the different configurations of the difference cells in the UDN
     *
     * @param configFile The filename of the configuration file
     */
    private void loadCells(String configFile) {
        Properties pro = new Properties();
        this.cells_ = new TreeMap<>();

        try {
            System.out.println("Loading cells config file...");
            pro.load(new FileInputStream(configFile));

            int numberOfCellTypes = Integer.parseInt(pro.getProperty("cellTypes", "7"));

            for (int i = 0; i < numberOfCellTypes; i++) {
                String configName = pro.getProperty("cell" + i);
                loadCellConfig(configName);
            }

        } catch (IOException e) {
            System.out.println(e + "Error loading properties: " + configFile);
            System.exit(-1);
        }
    }

    /**
     * Load the configuration of a particular cell type
     *
     * @param cells The list of cells to store the cells
     * @param cellType The type of cells loades
     * @param configs The filename of the configuration file
     */
    private void loadCellConfig(String cellConfigName) {
        //variables
        Properties pro = new Properties();
        PPP ppp = new PPP(this.random_);

        System.out.println("Loading cell config file: " + cellConfigName);
        try {
            pro.load(new FileInputStream(cellConfigName));

        } catch (IOException e) {
            System.out.println(e + "Error loading properties: " + cellConfigName);
            System.exit(-1);
        }

        //load parameters
        //load cell name
        String cellTypeName = pro.getProperty("type");
        String cellname = pro.getProperty("name");
        String radiationPatternFile = pro.getProperty("radiationPatternFile");


        //loading propagation parameters
        int numAntennasTX = Integer.parseInt(pro.getProperty("numAntennasTX", "4"));
        double transmittedPower = Double.parseDouble(pro.getProperty("transmittedPower", "50"));
        double alfa = Double.parseDouble(pro.getProperty("alfa", "21"));
        double beta = Double.parseDouble(pro.getProperty("beta", "344"));
        double delta = Double.parseDouble(pro.getProperty("delta", "2"));
        double transmitterGain = Double.parseDouble(pro.getProperty("transmitterGain", "14"));
        double receptorGain = Double.parseDouble(pro.getProperty("receptorGain", "1"));
        double workingFrequency = Double.parseDouble(pro.getProperty("workingFrequency", "2.1e6"));
        double coverageRadius = Double.parseDouble(pro.getProperty("coverageRadius", "2.1e6"));

        //load the number of cells of with this configuration
        //int numCells = Integer.parseInt(pro.getProperty("numCells", "10"));
        //int numCells = 5;
        double lambda = Double.parseDouble(pro.getProperty("lambdaForPPP", "50"));
        double mu = this.gridPointsY_ * this.gridPointsX_ * this.interPointSeparation_ * this.interPointSeparation_;
        mu = mu / (1000000.0);
        //uncomment for PPP distributions
        int numCells = ppp.getPoisson(lambda * mu);
        if (cellTypeName.equals("macro")) {
            numCells = 1;
        }

        System.out.println("numCells (" + cellname + "): " + numCells);

        List<Cell> cells = new ArrayList<>();
        for (int c = 0; c < numCells; c++) {
            //randomize the position: ensure that two BTSs are not placed in the
            //same position
            int x, y, z;
            do {
                x = random_.nextInt(gridPointsX_);
                y = random_.nextInt(gridPointsY_);
                z = random_.nextInt(gridPointsZ_);

                if (this.grid[x][y][z].hasBTSInstalled_) {
                    x = random_.nextInt(gridPointsX_);
                    y = random_.nextInt(gridPointsY_);
                    z = random_.nextInt(gridPointsZ_);
                } else {
                    this.grid[x][y][z].hasBTSInstalled_ = true;
                }

            } while (!this.grid[x][y][z].hasBTSInstalled_);

            Cell cell = Cell.newInstance(
                    cellTypeName,
                    cellname,
                    this,
                    x,
                    y,
                    z,
                    numAntennasTX,
                    transmittedPower,
                    alfa,
                    beta,
                    delta,
                    transmitterGain,
                    receptorGain,
                    workingFrequency,
                    coverageRadius,
                    radiationPatternFile
            );

            //add the cell to the map
            cells.add(cell);
            //include a reference to the cell in the point
            this.grid[x][y][z].setInstalledCell(cell);
        }
        //System.out.println(cells);

        List<Cell> all = this.cells_.get(workingFrequency);
        if (all != null) {
            all.addAll(cells);
            this.cells_.put(workingFrequency, all);
        } else {
            //attach the list to its name and to the UDN
            this.cells_.put(workingFrequency, cells);
        }

    }

    /**
     * Computes a Voronoi teselation based on a giver set of propagation regions
     *
     */
    //TODO: check if propagation regions are assigned properly
    private void computeVoronoiPropagationRegion(List<Region> regions) {
        //for each point in the grid, find the closest Region
        for (int i = 0; i < gridPointsX_; i++) {
            for (int j = 0; j < gridPointsY_; j++) {
                int closestRegion = 0;
                double d, maxDistance = Double.MAX_VALUE;
                for (int r = 0; r < regions.size(); r++) {
                    d = distance2D(grid[i][j][0].x_, grid[i][j][0].y_, regions.get(r).x_, regions.get(r).y_);
                    if (d < maxDistance) {
                        maxDistance = d;
                        closestRegion = r;
                    }
                }
                //Set prop Region for all points with the given x,y coordinates
                for (Point p : grid[i][j]) {
                    p.setPropagationRegion(regions.get(closestRegion));
                }

            }
        }
    }

    public String getMobilityType() {
        return mobilityType_;
    }

    /**
     * Return the grid used to discretize the terrain
     *
     * @return
     */
    public Point[][][] getGrid() {
        return this.grid;
    }

    /**
     * Print user information
     */
    void printUsers() {
        System.out.println("Printing information of " + this.users_.size() + " users:");
        System.out.println(this.users_);
    }

    /**
     * Returns the list or users in the UDN
     *
     * @return The list of users
     */
    public List<User> getUsers() {
        return users_;
    }

    /**
     * Number of total cells in the UDN
     *
     * @return The total number of cells in the UDN
     */
    public int getTotalNumberOfCells() {
        int count = 0;

        for (List<Cell> c : this.cells_.values()) {
            count += c.size();
        }

        return count;
    }

    /**
     * Return the number of activable cells, i.e., all but macros
     *
     * @return The total number of activable cells in the UDN
     */
    public int getTotalNumberOfActivableCells() {
        int count = 0;

        for (List<Cell> c : this.cells_.values()) {
            if ((c.size() > 0) && (c.get(0).getType() != CellType.MACRO)) {
                count += c.size();
            }
        }

        return count;
    }

    /**
     * Getter of a point of the grid
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return A point of the grid used to discretize the working area
     */
    public Point getGridPoint(int x, int y, int z) {
        return this.grid[x][y][z];
    }

    /**
     * Getter of the random number
     *
     * @return
     */
    public Random getRandom() {
        return random_;
    }

    /**
     * Getter of the inter point separation of the grid
     *
     * @return
     */
    public double getInterpointSeparation() {
        return this.interPointSeparation_;
    }

    /**
     * Activate/deactivates BTSs according to the information enclosed in the
     * binary solution of the problem
     *
     * @param cso
     */
    public void setCellActivation(Binary cso) {
        int bts = 0;

        for (List<Cell> cells : this.cells_.values()) {
            for (Cell c : cells) {
                if (c.getType() != CellType.MACRO) {
                    c.setActivation(cso.getIth(bts));
                    bts++;
                }
            }
        }
    }

    /**
     * Set the activation/deactivation plan of a binary string into the UDN
     *
     * @param cso A binary string containing whether the cell is active or not
     */
    public void copyCellActivation(Binary cso) {
        int bts = 0;

        for (List<Cell> cells : this.cells_.values()) {
            for (Cell c : cells) {
                if (c.getType() != CellType.MACRO) {
                    //c.setActivation(cso.getIth(bts));
                    cso.setIth(bts, c.isActive());
                    bts++;
                }
            }
        }
    }

    /**
     * Computes the number of grid points with the different stats computed
     *
     * @return The number of points
     */
    public int pointsWithStatsComputed() {
        int count = 0;
        for (int i = 0; i < this.gridPointsX_; i++) {
            for (int j = 0; j < this.gridPointsY_; j++) {
                for (int k = 0; k < this.gridPointsZ_; k++) {
                    if (grid[i][j][k].statsComputed_) {
                        count++;
                    }

                }
            }
        }

        return count;
    }

    /**
     * Number of active cells in the UDN
     *
     * @return The number of active cells
     */
    public int getTotalNumberOfActiveCells() {
        int count = 0;

        for (List<Cell> cells : this.cells_.values()) {
            for (Cell c : cells) {
                if (c.isActive()) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Computes the total received power at the grid points where a user is
     * placed
     */
    public void computeSignaling() {
        //computes for all as it is used in the solution initialization
        for (User u : this.users_) {
            int i = u.getX();
            int j = u.getY();
            int k = u.getZ();

            grid[i][j][k].computeTotalReceivedPower();
            //grid[i][j][k].computeSINR(u.getServingCell());
        }

    }

    /**
     * Set to 0 the number of users assigned to all the cells of the UDN
     */
    public void resetNumberOfUsersAssignedToCells() {
        for (List<Cell> cells : this.cells_.values()) {
            for (Cell c : cells) {
                c.setNumbersOfUsersAssigned(0);
            }
        }
    }

    /**
     * Checks that all the users have been assigned to a cell
     */
    public void validateUserAssigned() {
        int users = 0;
        for (List<Cell> cells : this.cells_.values()) {
            for (Cell c : cells) {
                users += c.getAssignedUsers();
            }
        }

        if (users != this.users_.size()) {
            System.out.println("Error when assgning users.");
            System.exit(-1);
        }

    }

    /**
     * 2D distance in the grid
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public double distance2D(int x1, int y1, int x2, int y2) {
        double d = 0.0;

        x1 = x1 * this.interPointSeparation_;
        y1 = y1 * this.interPointSeparation_;
        x2 = x2 * this.interPointSeparation_;
        y2 = y2 * this.interPointSeparation_;

        if ((x1 == x2) && (y1 == y2)) {
            return 0.1 * this.interPointSeparation_;
        } else {

            d += (x1 - x2) * (x1 - x2);
            d += (y1 - y2) * (y1 - y2);
            d = Math.sqrt(d);

            return d;
        }
    }

    /**
     * Computes the Euclidean distance between two points in the grid
     *
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @return
     */
    public double distance(int x1, int y1, int z1, int x2, int y2, int z2) {
        double d = 0.0;

        x1 = x1 * this.interPointSeparation_;
        y1 = y1 * this.interPointSeparation_;
        z1 = z1 * this.interPointSeparation_;
        x2 = x2 * this.interPointSeparation_;
        y2 = y2 * this.interPointSeparation_;
        z2 = z2 * this.interPointSeparation_;

        if ((x1 == x2) && (y1 == y2) && (z1 == z2)) {
            return 0.1 * this.interPointSeparation_;
        } else {

            d += (x2 - x1) * (x2 - x1);
            d += (y2 - y1) * (y2 - y1);
            d += (z2 - z1) * (z2 - z1);
            d = Math.sqrt(d);

            return d;
        }
    }

    /**
     * Given a point and a BTS, this function calculates both the azimuthal and
     * the occipital angles with respect to the BTS
     *
     * @param p
     * @param bts
     * @return
     */
    public int[] calculateAngles(Point p, BTS bts) {
        int occi; //occipithal angle (theta)
        int azi; //azimithal angle (phi)

        Point bts_p = this.getGridPoint(bts.getX(), bts.getY(), bts.getZ());
        double d3 = distance(p.x_, p.y_, p.z_, bts_p.x_, bts_p.y_, bts_p.z_);
        double d2 = distance2D(p.x_, p.y_, bts_p.x_, bts_p.y_);

        occi = (int) acos(d2 / d3);
        int hDistance = abs(p.x_ - bts_p.x_);
        azi = (int) acos(hDistance / d2);

        int[] angles = {azi, occi};

        return angles;
    }

    /**
     * Randomly allocate the positions of the users
     *
     */
    public void updateUsersPosition() {
        for (User u : users_) {
            int x = random_.nextInt(gridPointsX_);
            int y = random_.nextInt(gridPointsY_);
            u.setX(x);
            u.setY(y);
        }
    }

    /**
     * Updates the demands of all the users
     */
    public void updateUsersDemand() {
        for (User u : this.users_) {
            u.updateDemand();
        }
    }

    /**
     * Computes the users whose serving BTS is the macrocell
     *
     * @return
     */
    public int usersConnectedToMacro() {
        int count = 0;
        for (User u : this.users_) {
            if (u.getServingCell().getType() == CellType.MACRO) {
                count++;
            }
        }
        return count;
    }

    /**
     * Computes the users being served by femtocells
     *
     * @return
     */
    public int usersConnectedToFemto() {
        int count = 0;
        for (User u : this.users_) {
            if (u.getServingCell().getType() == CellType.FEMTO) {
                count++;
            }
        }
        return count;
    }

    /**
     * Computes the users being served by microcells
     *
     * @return
     */
    public int usersConnectedToMicro() {
        int count = 0;
        for (User u : this.users_) {
            if (u.getServingCell().getType() == CellType.MICRO) {
                count++;
            }
        }
        return count;
    }

    /**
     * Computes the users being served by picocells
     *
     * @return
     */
    public int usersConnectedToPico() {
        int count = 0;
        for (User u : this.users_) {
            if (u.getServingCell().getType() == CellType.PICO) {
                count++;
            }
        }
        return count;
    }

    /**
     * Restarts diffent data structures in all the Points used to store
     * precomputed signal power and SINR. The goal is saving computational time
     * and memory.
     */
    public void emptyMapsAtPoints() {
        for (int i = 0; i < this.gridPointsX_; i++) {
            for (int j = 0; j < this.gridPointsY_; j++) {
                for (int k = 0; k < this.gridPointsZ_; k++) {
                    this.grid[i][j][k].signalPowerMap_ = new HashMap();
                    this.grid[i][j][k].sinrMap_ = new HashMap();
                }
            }
        }
    }

}
