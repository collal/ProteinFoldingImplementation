package it.uniroma1.di.tmancini.teaching.ai.search.proteinfolding;

import it.uniroma1.di.tmancini.teaching.ai.search.*;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

public class ProteinFolding extends Problem implements Callable<Integer> {
    @CommandLine.Option(names={"--size"},required = true,
            description = "The size of the protein, as a positive integer number.")
    private int size;


    @CommandLine.Option(names={"--algos", "--algorithms"}, required = true, split=",",
            description = "The algorithms to use, as a double quoted comma-separated list. Valid values are" +
                    "{BFS, DFS, MINCOST, 'A*', 'BFG'}")
    private String[] algos;

    @CommandLine.Option(names={"--protein"})
    private String protein;

    @CommandLine.Option(names={"-v", "--verbosity"}, defaultValue = "0",
            description = "The verbosity level of the output, as a positive integer number. " +
                    "0 corresponds to 'standard', while higher numbers correspond to higher verbosity.")
    private int vlevel;

    public static enum Heuristics { PAIRS, ANTIPAIRS} // these heuristics are consistent and optimise to some extent; should find better heuristics tho
    private ProteinFolding.Heuristics h;

    private int xStart, yStart;
    private int minX, minY, maxX, maxY;

//    public ProteinFolding(String name, String protein, int proteinLen, int xStart, int yStart) {
//        super(name);
//        this.protein = protein;
//        this.size = proteinLen;
//        this.xStart = xStart;
//        this.yStart = yStart;
//
//        this.minX = -(proteinLen - 1);
//        this.minY = -(proteinLen - 1);
//        this.maxX = (proteinLen - 1);
//        this.maxY = (proteinLen - 1);
//    }

    public ProteinFolding() {
        super("ProteinFolding");
    }

    public void setVals() {
        // default values for cartesian grid
        this.xStart = 0;
        this.yStart = 0;

        this.minX = -(size - 1);
        this.minY = -(size - 1);
        this.maxX = (size - 1);
        this.maxY = (size - 1);
    }

    public String getProtein() {
        return protein;
    }

    public int getXStart() {
        return xStart;
    }
    public int getYStart() {
        return yStart;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }


    private void checkInput() throws IllegalArgumentException{
        List<String> algorithms = Arrays.asList(
                "bfs",
                "dfs",
                "mincost",
                "a*",
                "bfg");
        for (String algoHeur : this.algos) {
            List<String> algoAndHeuristics = getAlgorithmAndHeuristics(algoHeur);
            String algo = algoAndHeuristics.get(0);
            if (!algorithms.contains(algo.toLowerCase())){
                throw new IllegalArgumentException(
                        String.format("[ERROR] Algorithm '%s' is unknown. Please specify one of " +
                                "{'BFS', 'DFS', 'MINCOST', 'A*', 'BFG'}", algo));
            }
            if (algoAndHeuristics.size() > 1) {
                String heur = algoAndHeuristics.get(1);
                if (!hasHeuristics(heur)){
                    throw new IllegalArgumentException(
                            String.format("[ERROR] Heuristics '%s' is unknown. Please specify one of " +
                                    "{'SPIRAL', 'SNAKE'}", heur));
                }
                if (!algo.equalsIgnoreCase("A*") && !algo.equalsIgnoreCase("BFG")){
                    System.err.printf("[WARNING] heuristics '%s' is not used by algorithm '%s'%n%n",
                            heur, algo);
                }
            }
        }

        if (this.size <= 0) {
            throw new IllegalArgumentException(
                    "[ERROR] Negative or null puzzle sizes are not allowed. Please a positive value for option '--size'");
        }
        if (this.vlevel < 0) {
            throw new IllegalArgumentException(
                    "[ERROR] Negative verbosity levels are not allowed. Please a non-negative value for option '--verbosity'");
        }


    }


    @Override
    public Integer call() {
        try {
            this.checkInput();
            this.setVals();
            if (protein == null) {
                StringBuilder temp = new StringBuilder();
                Random rand = new Random();
                for (int i = 0; i < this.size; i++) {
                    int myChar = (int) (rand.nextFloat() * 2);
                    if (myChar == 0) {
                        temp.append('H');
                    } else {
                        temp.append('P');
                    }
                }
                protein = temp.toString();
            }
            ProteinFoldingState initialState = ProteinFoldingState.initialState(this);
            System.out.println("Random initial state:\n" + initialState);
            System.out.println("Protein: " + protein);
            System.out.println("Start position: (" + xStart + ", " + yStart + ")");


            SearchStateExplorer explorer;
            for (String algo : algos) {
                List<String> algoAndSetting = getAlgorithmAndHeuristics(algo);
                String algorithm = algoAndSetting.get(0);
                String setting = null;
                if (algoAndSetting.size() > 1) setting = algoAndSetting.get(1);

                switch (algorithm) {
                    case "bfs":
                        explorer = new BFSExplorer(this);
                        break;
                    case "dfs":
                        explorer = new DFSExplorer(this);
                        break;
                    case "mincost":
                        explorer = new MinCostExplorer(this);
                        break;
                    case "a*":
                        explorer = new AstarExplorer(this);
                        this.clearHeuristics();
                        if (algoAndSetting.size() > 1) {
                            this.setHeuristics(ProteinFolding.Heuristics.valueOf( setting ));
                        }
                        break;
                    case "bfg":
                        explorer = new BestFirstGreedyExplorer(this);
                        this.clearHeuristics();
                        if (algoAndSetting.size() > 1) {
                            this.setHeuristics(ProteinFolding.Heuristics.valueOf( setting ));
                        }
                        break;
                    default:
                        throw new IllegalStateException("[ERROR] Unknown algorithm: " + algorithm);
                }


                explorer.setVerbosity(SearchStateExplorer.VERBOSITY.values()[vlevel]);

                System.out.println("\n\n\n===================\n\nAlgorithm " + explorer +
                        (setting != null ? " (" + setting + ")" : "") + " started");
                List<Action> result = explorer.run(initialState);

                System.out.println("[INFO] Algorithm " + explorer + " terminated.");
                explorer.outputStats();

//                System.out.println("Initial state:\n" + initialState);
                if (result != null) {
                    System.out.println("\n\n\nSolution found by algorithm " + explorer + " (" + result.size() + " actions):\n");
                    int i = 0;
                    for (Action a : result) {
                        System.out.println(" [" + i + "]" + a);
                        i++;
                    }
                    ProteinFoldingState finalState = ProteinFoldingState.getFinalState(initialState, result);
                    System.out.println(finalState);
                    System.out.println("Energy: " + finalState.getEnergy());
                    System.out.println("hValue at End: " + finalState.getHPairs());
                } else System.out.printf("\n\n\nNo solution found by algorithm %s%n", explorer);
            }
            return 0;
        } catch (IllegalArgumentException e){
            System.err.println(e.getMessage());
            return 1;
        } catch (IllegalStateException e){
            System.err.println(e.getMessage());
            return 2;
        }
    }

    private List<String> getAlgorithmAndHeuristics(String algo){
        List<String> algoAndSetting = Arrays.asList(algo.split(":"));
        List<String> result = new ArrayList<>();
        result.add(algoAndSetting.get(0).trim().toLowerCase());
        if (algoAndSetting.size() > 1) result.add(algoAndSetting.get(1).trim());
        return result;
    }

    public int getSize(){
        return this.size;
    }

    private static void printOutputHeader(){
        System.out.println("\n======================================================"+
                "=\n=\t\tSearchStateExplorer\t\t      =\n" +
                "=\t\t  Exercise: Protein Folding\t\t      =\n" +
                "= Computer Science Dept - Sapienza University of Rome =\n" +
                "=======================================================\n");
    }

    public static void main(String[] args) {
        printOutputHeader();
        int exitCode = new CommandLine(new ProteinFolding()).execute(args);

        System.out.printf("%nExecution completed. Exiting %s errors.%n", exitCode > 0 ? "with" : "without");
        System.exit(exitCode);
    }

    public static boolean hasHeuristics(String test) {
        for (Heuristics c : Heuristics.values()) {
            if (c.name().equals(test))
                return true;
        }
        return false;
    }

    public Heuristics getHeuristics() {
        return h;
    }

    public void setHeuristics(Heuristics h) {
        this.h = h;
    }

    public void clearHeuristics() {
        this.h = null;
    }

}
