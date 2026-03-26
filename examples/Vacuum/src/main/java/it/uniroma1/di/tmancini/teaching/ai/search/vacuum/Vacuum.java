package it.uniroma1.di.tmancini.teaching.ai.search.vacuum;

import it.uniroma1.di.tmancini.teaching.ai.search.*;
import picocli.CommandLine;

import java.util.*;
import java.util.concurrent.Callable;


public class Vacuum extends Problem implements Callable<Integer> {


	@CommandLine.Option(names={"-x", "--sizeX"}, required = true,
			description = "The size of the X dimension of the world, as a positive integer number.")
	private int sizeX;

	@CommandLine.Option(names={"-y", "--sizeY"}, required = true,
			description = "The size of the Y dimension of the world, as a positive integer number.")
	private int sizeY;

	@CommandLine.Option(names={"--maxdepth"}, required = true)
	private Integer maxDepth;

	@CommandLine.Option(names={"--algos", "--algorithms"}, required = true, split=",",
			description = "The algorithms to use, as a double quoted comma-separated list. Valid values are" +
					"{BFS, DFS, MINCOST, A*}")
	private String[] algos;

	@CommandLine.Option(names={"-v", "--verbosity"}, defaultValue = "0",
			description = "The verbosity level of the output, as a positive integer number. " +
					"0 corresponds to 'standard', while higher numbers correspond to higher verbosity.")
	private int vlevel;

	@CommandLine.Option(names={"-r", "--randomseed"}, defaultValue = "-1",
			description = "The random seed for RNG, as a non-negative number. If no value is specified, the RNG will be initialized with no seed," +
					"thus yielding different random number sequences at each invocation.")
	private int randomSeed;

	private Random random;

	public int getSizeX() {
		return sizeX;
	}
	public int getSizeY() {
		return sizeY;
	}

	public Random getRandom(){
		return this.random;
	}


	public Vacuum() {
		super("Vacuum");
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
		}

		if (this.sizeX <= 0) {
			throw new IllegalArgumentException(
					"[ERROR] Negative or null values for the size of the X dimension are not allowed. Please a positive value for option '--sizeX'");
		}
		if (this.sizeY <= 0) {
			throw new IllegalArgumentException(
					"[ERROR] Negative or null values for the size of the X dimension are not allowed. Please a positive value for option '--sizeY'");
		}
		if (this.maxDepth <= 0) {
			throw new IllegalArgumentException(
					"[ERROR] Negative or null values for the maximum depth are not allowed. Please a positive value for option '--maxdepth'");
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

		System.out.println("[INFO] X size: " + sizeX);
		System.out.println("[INFO] Y size: " + sizeY);
		System.out.println("[INFO] Max depth: " + maxDepth);

		VacuumState initialState = VacuumState.newRandom(this);
		System.out.println("[INFO] Random initial state:\n" + initialState);

		SearchStateExplorer explorer;
		for (String algo : algos) {
			List<String> algoAndSetting = getAlgorithmAndHeuristics(algo);
			String algorithm = algoAndSetting.get(0);

			switch (algorithm){
				case "bfs":
					explorer = new BFSExplorer(this,maxDepth);
					break;
				case "dfs":
					explorer = new DFSExplorer(this,maxDepth);
					break;
				case "mincost":
					explorer = new MinCostExplorer(this,maxDepth);
					break;
				case "a*":
					explorer = new AstarExplorer(this,maxDepth);
					break;
				case "bfg":
					explorer = new BestFirstGreedyExplorer(this,maxDepth);
					break;
				default:
					throw new IllegalStateException("\n[ERROR] Unknown algorithm: " + algorithm);
			}

			explorer.setVerbosity(SearchStateExplorer.VERBOSITY.values()[vlevel]);

			System.out.println("\n\n\n===================\n\nAlgorithm " + explorer + " started");
			if (explorer.getMaxDepth() != null) {
				System.out.println("[INFO] Maximum search depth set to " + explorer.getMaxDepth());
			}
				List<Action> result = explorer.run( initialState );

				System.out.println("[INFO] Algorithm " + explorer + " terminated.");
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
				else System.out.printf("\n\n\nNo solution found  by algorithm %s.", explorer.toString());
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

	private static void printOutputHeader(){
		System.out.println("\n======================================================"+
				"=\n=\t\tSearchStateExplorer\t\t      =\n" +
				"=\t\t  Example: Vacuum\t\t      =\n" +
				"= Computer Science Dept - Sapienza University of Rome =\n" +
				"=======================================================\n");
	}

	public static void main(String[] args) {
		printOutputHeader();
		int exitCode = new CommandLine(new Vacuum()).execute(args);

		System.out.printf("%nExecution completed. Exiting %s errors.%n", exitCode > 0 ? "with" : "without");
		System.exit(exitCode);
	}
	

	
}