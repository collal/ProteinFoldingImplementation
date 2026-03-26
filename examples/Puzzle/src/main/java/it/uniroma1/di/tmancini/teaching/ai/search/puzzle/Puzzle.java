package it.uniroma1.di.tmancini.teaching.ai.search.puzzle;

import it.uniroma1.di.tmancini.teaching.ai.search.*;
import picocli.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

public class Puzzle extends Problem implements Callable<Integer> {

    public static enum Heuristics { MANHATTAN, TILES_OUT_OF_PLACE }

    @CommandLine.Option(names={"--size"},required = true,
            description = "The size of the puzzle, i.e. the number of tiles in each row/column, as a positive integer number.")
    private int size;

    @CommandLine.Option(names={"--algos", "--algorithms"}, required = true, split=",",
            description = "The algorithms to use, as a double quoted comma-separated list. Valid values are" +
                    "{BFS, DFS, MINCOST, A*:<heuristics>, BFG:<heuristics>}, where" +
                    "<heuristics> can be MANHATTAN or TILES_OUT_OF_PLACE")
    private String[] algos;

    @CommandLine.Option(names={"-v", "--verbosity"}, defaultValue = "0",
            description = "The verbosity level of the output, as a positive integer number. " +
                    "0 corresponds to 'standard', while higher numbers correspond to higher verbosity.")
    private int vlevel;

    @CommandLine.Option(names={"--solvableOnly"}, defaultValue = "false",
            description = "If specified, only tries to solve solvable instances, building random initial states until " +
                    "a solvable instance is found.")
    private boolean solvableOnly;

    @CommandLine.Option(names={"-r", "--randomseed"}, defaultValue = "-1",
            description = "The random seed for RNG, as a non-negative number. If no value is specified, the RNG will be initialized with no seed," +
                    "thus yielding different random number sequences at each invocation.")
    private int randomSeed;

    private Random random;

    private Puzzle.Heuristics h;

    public Puzzle() {
        super("Puzzle");
        this.h = null;
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
                                    "{'MANHATTAN', 'TILES_OUT_OF_PLACE'}", heur));
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

        if (this.randomSeed < 0) {
            System.err.println(
                    "[WARNING] Ignoring negative random seed. If you desire repeatable experiments, specify a positive integer number using '--randomseed'");
        }
    }


    @Override
    public Integer call() {
        try{
            this.checkInput();


            this.random = new Random(this.randomSeed);

            PuzzleState initialState;
            boolean initStateFound = false;
            do {
                initialState = PuzzleState.newRandom(this);
                System.out.println("[INFO] Random initial state:\n" + initialState);

                boolean solvable = initialState.isSolvable();
                System.out.println("Is puzzle solvable? " + solvable);

                if (!solvable && solvableOnly) {
                    System.out.println("[INFO] Retrying, as requested by flag --solvableOnly.\n");
                } else {
                    initStateFound = true;
                }

            } while (!initStateFound);

            SearchStateExplorer explorer;

            for (String algo : algos) {
                List<String> algoAndSetting = getAlgorithmAndHeuristics(algo);
                String algorithm = algoAndSetting.get(0);
                String setting = null;
                if (algoAndSetting.size() > 1) setting = algoAndSetting.get(1);

                switch (algorithm){
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
                            this.setHeuristics(Puzzle.Heuristics.valueOf( setting ));
                        }
                        break;
                    case "bfg":
                        explorer = new BestFirstGreedyExplorer(this);
                        this.clearHeuristics();
                        if (algoAndSetting.size() > 1) {
                            this.setHeuristics(Puzzle.Heuristics.valueOf( setting ));
                        }
                        break;
                    default:
                        throw new IllegalStateException("\n[ERROR] Unknown algorithm: " + algorithm);
                }

                explorer.setVerbosity(SearchStateExplorer.VERBOSITY.values()[vlevel]);

                System.out.println("\n\n\n===================\n\nAlgorithm " + explorer +
                        (setting != null ? " (" + setting + ")" : "") + " started");
                List<Action> result = explorer.run( initialState );

                System.out.println("Algorithm " + explorer + " terminated.");
                explorer.outputStats();

                System.out.println("Initial state:\n" + initialState);
                if (result != null) {
                    System.out.println("\n\n\nSolution found by algorithm " + explorer + " (" + result.size() + " actions):\n");
                    int i = 0;
                    for(Action a : result) {
                        System.out.println(" [" + i + "]" + a);
                        i++;
                    }
                }
                else System.out.println("\n\n\nNo solution found by algorithm " + explorer);
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

    public static boolean hasHeuristics(String test) {
        for (Heuristics c : Heuristics.values()) {
            if (c.name().equals(test))
                return true;
        }
        return false;
    }

    public int getSize() {
        return size;
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

    public Random getRandom(){
        return this.random;
    }


    private static void printOutputHeader(){
        System.out.println("\n======================================================"+
                "=\n=\t\tSearchStateExplorer\t\t      =\n" +
                "=\t\t  Example: Puzzle\t\t      =\n" +
                "= Computer Science Dept - Sapienza University of Rome =\n" +
                "=======================================================\n");
    }

    public static void main(String[] args) {
        printOutputHeader();
        int exitCode = new CommandLine(new Puzzle()).execute(args);

        System.out.printf("%nExecution completed. Exiting %s errors.%n", exitCode > 0 ? "with" : "without");
        System.exit(exitCode);
    }
}
