/** GrayWolfOptimizatio.java
 *
 * Solves the N-Queens puzzle using Gray Wolf Optimization Algorithm

 */

import java.util.ArrayList;
import java.util.Random;

public class GrayWolfOptimization {
    /* GRAY WOLF PARAMETERS */
    private int MAX_LENGTH; // N number of queens

    Random R = new Random();
    private int WOLF_COUNT;
    private int MAX_EPOCHS;
    
    /****** VARIABLES FOR Python implementation for GWO **********/
    int[] AlphaPosition; // pos init arrays
    int[] BetaPosition;
    int[] DeltaPosition;
    double Alpha_score = Integer.MAX_VALUE; // set to inf
    double Beta_score = Integer.MAX_VALUE;
    double Delta_score = Integer.MAX_VALUE;

    int[] Convergence_curve;
    int[][] Positions;
    double fitness;
    /****** VARIABLES FOR Python implementation for GWO **********/

    private ArrayList<Wolf> wolves;
    private ArrayList<Wolf> solutions;
    private int epoch;

    private int SHUFFLE_RANGE_MIN;
    private int SHUFFLE_RANGE_MAX;

    public GrayWolfOptimization(int n){
        MAX_LENGTH = n;
        WOLF_COUNT = 30;
        MAX_EPOCHS = 1000;
        SHUFFLE_RANGE_MIN = 8;
        SHUFFLE_RANGE_MAX = 20;
        Convergence_curve = new int[MAX_EPOCHS];

        AlphaPosition = new int[MAX_LENGTH];
        BetaPosition = new int[MAX_LENGTH];
        DeltaPosition = new int[MAX_LENGTH];
        Positions = new int[WOLF_COUNT][MAX_LENGTH];
    }
    public GrayWolfOptimization(int n, int trialLimit, int maxCount, int minShuffles, int maxShuffles){
        MAX_LENGTH = n;
        WOLF_COUNT = trialLimit;
        MAX_EPOCHS = maxCount;
        SHUFFLE_RANGE_MIN = minShuffles;
        SHUFFLE_RANGE_MAX = maxShuffles;
        Convergence_curve = new int[MAX_EPOCHS];

        AlphaPosition = new int[MAX_LENGTH];
        BetaPosition = new int[MAX_LENGTH];
        DeltaPosition = new int[MAX_LENGTH];
        Positions = new int[WOLF_COUNT][MAX_LENGTH];
    }

    /*
        Initializes positions of each wolf with search agents number
     */
    public void initialize(){
        // NOTE DO NOT CHANGE. ALREADY SAME AS PSO
        int shuffles = 0;
        Wolf newWolf = null;
        int newWolfIndex = 0;
        for(int i=0; i < WOLF_COUNT; i++){
            newWolf = new Wolf(MAX_LENGTH);
            wolves.add(newWolf);
            newWolfIndex = wolves.indexOf(newWolf);
            shuffles = getRandomNumber(SHUFFLE_RANGE_MIN, SHUFFLE_RANGE_MAX);
            for( int j=0; j< shuffles; j++){
                randomlyArrange(newWolfIndex);
            }
            wolves.get(newWolfIndex).computeConflicts();
        }
    }

    /*
     * Main algorithm mechanism
     */
    public boolean algorithm(){
        wolves = new ArrayList<>();
        solutions = new ArrayList<>();

        epoch = 0;
        boolean done = false;
        Wolf aWolf = null;

        initialize();

        while(!done){
            if(epoch < MAX_EPOCHS){
                for (int h = 0; h< WOLF_COUNT; h++ )
                {
                    aWolf = wolves.get(h);
                    aWolf.computeConflicts();
                    if (aWolf.getConflicts() == 0) {
                        done = true;
                    }
                }
                huntForPrey(); // Wolves scatter to find a prey

                getFitness(); // Computes Fitness

                findDominance(); // Checks who gets to be the alpha

                attackPrey(); // Wolves move in a strategic way to trap prey

                prioritizeAlpha(); // Alpha gets to eat the prey first

                epoch++;
            }
            else{ done = true; }
        }


        if(epoch == MAX_EPOCHS){ done = false; }
        for( Wolf w : wolves){
            if(w.getConflicts() == 0){
                solutions.add(w);
            }
        }
        System.out.println("Total # of Solutions: " + solutions.size());
        return done;
    }

    public void huntForPrey(){
        for (int i = 0; i < WOLF_COUNT; i++) {
            for (int j = 0; j < MAX_LENGTH; j++) {
                Positions[i][j] = getRandomNumber(SHUFFLE_RANGE_MIN, SHUFFLE_RANGE_MAX);
            }
        }
    }
    public void attackPrey(){
        for (int i = 0; i < WOLF_COUNT; i++) {
            for (int j = 0; j < MAX_LENGTH; j++) {
                int a = 2 - epoch * ((2) / MAX_EPOCHS);
                double r1 = Math.random(); // Random [0,1]
                double r2 = Math.random(); // Random [0,1]
                double A1 = 2 * a * r1 - a;
                double C1 = 2 * r2;

                double D_alpha = Math.abs(C1 * AlphaPosition[j] - Positions[i][j]);
                double X1 = AlphaPosition[j] - A1 * D_alpha;
                r1 = Math.random(); // Random [0,1]
                r2 = Math.random(); // Random [0,1]
                double A2 = 2 * a * r1 - a;
                double C2 = 2 * r2;
                double D_beta = Math.abs(C2 * BetaPosition[j] - Positions[i][j]);
                double X2 = BetaPosition[j] - A2 * D_beta;
                r1 = Math.random(); // Random [0,1]
                r2 = Math.random(); // Random [0,1]
                double A3 = 2 * a * r1 - a;
                double C3 = 2 * r2;
                double D_delta = Math.abs(C3 * DeltaPosition[j] - Positions[i][j]);
                double X3 = DeltaPosition[j] - A3 * D_delta;
                Positions[i][j] = (int) (X1 + X2 + X3) / 3;
            }
        }
    }
    public void getFitness(){
        double alpha_temp = 1000;
        double delta_temp = -1000; // worst results
        Wolf aWolf;
        for (int i = 0; i < WOLF_COUNT; i++) {
            aWolf = wolves.get(i);
            int current_conflicts = aWolf.getConflicts();
            if(current_conflicts < alpha_temp){
                alpha_temp = current_conflicts;
            }
            if(current_conflicts > delta_temp){
                delta_temp = current_conflicts;
            }
        }

//        System.out.println("Final Alpha (Best): " + alpha_temp);
//        System.out.println("Final Delta (Worst): " + delta_temp);
        for (int i = 0; i < WOLF_COUNT; i++) {
            aWolf = wolves.get(i);
            int current_conflicts = aWolf.getConflicts();
            int fitness_value = (int) -(alpha_temp - current_conflicts * 100 / delta_temp);
            aWolf.setFitness(fitness_value);
        }

    }
    public void findDominance(){
        int value = 0;
        for(Wolf aWolf: wolves){
            value = aWolf.getConflicts();
            if (value < Alpha_score) {
                Delta_score = Beta_score;
                DeltaPosition = BetaPosition.clone();
                Beta_score = Alpha_score;
                BetaPosition = AlphaPosition.clone();
                Alpha_score = value;
                AlphaPosition = aWolf.getPos().clone(); // Update alpha
            }
            if (value > Alpha_score && fitness < Beta_score) {
                Delta_score = Beta_score; // Update delta
                DeltaPosition = BetaPosition.clone();
                Beta_score = value; // Update beta
                BetaPosition = aWolf.getPos().clone();
            }
            if (fitness > Alpha_score && fitness > Beta_score && fitness < Delta_score) {
                Delta_score = fitness; // update delta
                DeltaPosition = aWolf.getPos().clone();
            }
        }
    }
    public void prioritizeAlpha(){
        for (int i = 0; i < wolves.size(); i++) {

            Wolf aWolf = wolves.get(i);
            int j = i-1;
            while( j >= 0 && wolves.get(j).getFitness() > aWolf.getFitness()){
                wolves.set(j+1, wolves.get(j));
                j = j-1;
            }
            wolves.set(j+1, aWolf);
        }
    }



    public int getRandomNumber(int low, int high) {
        /* Gets a random number in the range of the parameters
         *
         * @param: the minimum random number
         * @param: the maximum random number
         * @return: random number
         */
        return (int)Math.round((high - low) * R.nextDouble() + low);
    }
    public int getExclusiveRandomNumber(int high, int except) {
        /* Gets a random number with the exception of the parameter
         *
         * @param: the maximum random number
         * @param: number to to be chosen
         * @return: random number
         */
        boolean done = false;
        int getRand = 0;

        while(!done) {
            getRand = R.nextInt(high);
            if(getRand != except){
                done = true;
            }
        }

        return getRand;
    }
    public void randomlyArrange(int index) { //randomly swap 2 positions
        /* Changes a position of the queens in a particle by swapping a randomly selected position
         *
         * @param: index of the particle
         */
        int positionA = getRandomNumber(0, MAX_LENGTH - 1);
        int positionB = getExclusiveRandomNumber(MAX_LENGTH - 1, positionA);
        Wolf thisWolf = wolves.get(index);

        int temp = thisWolf.getPos(positionA);
        thisWolf.setPos(positionA, thisWolf.getPos(positionB));
        thisWolf.setPos(positionB, temp);
    }
    public int clip(int x, int min, int max){
        /*
         * Utility function for clipping data values outside a specific range (Java-converted method from python module)
         * @param: current value to clip
         * @param: minimum range
         * @param: maximum range
         */
        if(x < min) x = min;
        if(x > max) x = max;
        return x;
    }
    public int getEpoch() {
        /*
        returns the current epoch of the run
        @return: epoch of the run
     */
        return epoch;
    }
    public int getPopSize(){
        /*
        gets the population size of the run
        @return: size of arraylist wolf
     */
        return wolves.size();
    }
    public ArrayList<Wolf> getWolves() {
        /*
        returns an array list of wolves
         */
        return wolves;
    }

    public static void main(String[] args) {
        GrayWolfOptimization GWO = new GrayWolfOptimization(4);
        GWO.algorithm();
    }
}
