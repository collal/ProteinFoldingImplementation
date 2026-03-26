package it.uniroma1.di.tmancini.teaching.ai.search.puzzle;
import java.util.*;
import it.uniroma1.di.tmancini.teaching.ai.search.*;

public class PuzzleState extends State {
	
	// the blank position
	private int x;
	private int y;
	
	private int[][] value;
	private int nbOutOfPlace;
	
	private int hManhattan;
	
	public PuzzleState(Puzzle p) {
		super(p);
		this.value = new int[p.getSize()][p.getSize()];
		this.nbOutOfPlace = 0;
		this.hManhattan = 0;
		this.x = 0;
		this.y = 0;
	}
	
	public int getSize() {
		return ((Puzzle)getProblem()).getSize();
	}

	public static PuzzleState newRandom(Puzzle p) {
		Random rnd = p.getRandom();
		int size = p.getSize();
		PuzzleState result = new PuzzleState(p);
		result.nbOutOfPlace = 0;
		result.hManhattan = 0;
		
		// Randomised list of numbers from 0 to N*N-1
		List<Integer> numbers = new ArrayList<Integer>();
		int maxNumber = size*size - 1;
		for (int i=0; i <= maxNumber; i++) numbers.add(i);		
		Collections.shuffle(numbers, rnd);
			
		int i = 0;
		int number = -1;
		Coord coord = new Coord(0,0);
		for(int dx = 0; dx < size; dx++) {
			for(int dy = 0; dy < size; dy++) {
				coord.x = dx; coord.y = dy;
				number = numbers.get(i);
				result.value[dx][dy] = number;

				if (number == 0) {
					// redundantly set blank position
					result.x = dx;
					result.y = dy;
				}

				if (number != 0 && result.isOutOfPlace(coord)) {
					result.nbOutOfPlace++;
					result.hManhattan += result.manhattanDistance(coord);
				}				
				i++;
			}
		}
		return result; 
	}

	public boolean isSolvable() {
		int permutationInversions = 0;
		int size = getSize();
		
		List<Integer> numbers = new ArrayList<>();
		for(int j = size-1; j >= 0; j--) {
			for(int i = 0; i < size; i++) {
				if (value[i][j] != 0) numbers.add(value[i][j]);
			}
		}
		// For each pair of non-empty cells A and B (where B > A)...
		for (int A=0; A < numbers.size(); A++) {
			for(int B=A+1; B < numbers.size(); B++) {		
				if (numbers.get(A) > numbers.get(B)) permutationInversions++;
			}
		}
		//System.out.println("numbers: " + numbers + "\n");
		//System.out.println("permutationInversions: " + permutationInversions + "\n");
		//System.out.println("Blank row: " + y + "\n");

		return (
			((size % 2 != 0) && (permutationInversions % 2 == 0))
			||
			((size % 2 == 0) && (y+1 % 2 == 0)) && ((permutationInversions % 2 != 0))
			||
			((size % 2 == 0) && (y+1 % 2 != 0)) && ((permutationInversions % 2 == 0))
		);
	}


	private int goalNumber(int dx, int dy) {
		if (dx == getSize()-1 && dy == 0) return 0;
		else return 1 + getSize()*(getSize()-1-dy) + dx;
	}
	
	private static class Coord { 
		private int x; 
		private int y; 
		private Coord(int x, int y) { 
			this.x = x; this.y = y;
		}
		public boolean equals(Object o) {
			if (o == null) return false;
			if (!o.getClass().equals(this.getClass())) return false;
			Coord oo = (Coord)o;
			return (oo.x == this.x && oo.y == this.y);
		}
		public String toString() {
			return "(" + x + ", " + y + ")";
		}
	}

	/** Returns the goal position of number in a size*size puzzle
	*/
	private Coord goalPosition(int number) {
		if (number == 0) return new Coord(getSize()-1,0);
		Coord result = new Coord(0,0);
 		int yReverse = (number-1)/getSize();
		result.x = (number-1) - (yReverse*getSize());
		result.y = (getSize()-1) - yReverse;
		return result;
	}

	/** Returns the Manhattan distance of coordinate coord to goal position
	*/
	private int manhattanDistance(Coord coord) {
		Coord goal = goalPosition(value[coord.x][coord.y]);
		return Math.abs(goal.x - coord.x) + Math.abs(goal.y - coord.y);
	}

	/** Tells if position coord contains an out of place number */
	private boolean isOutOfPlace(Coord coord) {
		int currNumber = value[coord.x][coord.y];
		Coord goalPos = goalPosition(currNumber);
		return !coord.equals(goalPos);
	}

	public Collection<? extends Action> executableActions() {
		List<PuzzleAction> result = new ArrayList<PuzzleAction>();
		int size = getSize();
		if (x > 0) {
			result.add( PuzzleAction.slideRight() );
		}
		if (y > 0) {
			result.add( PuzzleAction.slideUp() );
		}
		if (x < size-1) {
			result.add( PuzzleAction.slideLeft() );
		}
		if (y < size-1) {
			result.add( PuzzleAction.slideDown() );
		}
		return result;				
	}
	
	public State resultingState(Action a) {
		//if (value[x][y] != 0) throw new RuntimeException("ERROR");
		PuzzleState result = (PuzzleState)this.clone();
		int size = getSize();
		
		result.value = new int[size][size];
		for(int dx = 0; dx < size; dx++) {
			System.arraycopy(this.value[dx], 0, result.value[dx], 0, size);
		}
		
		int fromX = x, fromY = y;
		if (a == PuzzleAction.slideLeft() ) {
			fromX = x+1;
		}
		else if (a == PuzzleAction.slideRight() ) {
			fromX = x-1;
		}
		else if (a == PuzzleAction.slideUp() ) {
			fromY = y-1;
		}
		else if (a == PuzzleAction.slideDown() ) {
			fromY = y+1;
		}
		/*
			Starting state has blank in (x,y).
			Action 'a' moves tile from (fromX,fromY) to (x,y)
			 --> Result state has blank in (fromX,fromY)

		*/
		result.value[fromX][fromY] = 0; // New blank position
		result.x = fromX;
		result.y = fromY;		
		int tileNumber = value[fromX][fromY];		
		result.value[x][y] = tileNumber; // New tile in position x,y

		Coord tileFrom = new Coord(fromX, fromY);
		Coord tileTo = new Coord(x,y);

		//System.out.println("\nChosen action " + a + ":\n");
		//System.out.println(" - Sliding tile from pos. " + tileFrom + " with number " + tileNumber + "\n");

		//System.out.println("Starting state:\n");
		//System.out.println(this);

		// Incrementally evaluate heuristic values of state 'result'
		if (this.isOutOfPlace(tileFrom)) {
			// Tile was in out of place position before action
			result.nbOutOfPlace--;
			int deltaManhattan = this.manhattanDistance(tileFrom);
			result.hManhattan -= deltaManhattan;
			//System.out.println(" - Tile was in out-of-place position\n");
			//System.out.println("   --> nbOutOfPlace decreased by 1 to " + 
			//	result.nbOutOfPlace);
			//System.out.println("   --> hManhattan decreased by " + deltaManhattan + " to " + result.hManhattan);
		}
		if (result.isOutOfPlace(tileTo)) {
			result.nbOutOfPlace++;
			int deltaManhattan = result.manhattanDistance(tileTo);
			result.hManhattan += deltaManhattan;
			//System.out.println(" - Tile is still in out-of-place position\n");
			//System.out.println("   --> nbOutOfPlace increased by 1 to " + 
			//	result.nbOutOfPlace);
			//System.out.println("   --> hManhattan increased by " + deltaManhattan + " to " + result.hManhattan);
		}

		//System.out.println("Resulting state:\n");
		//System.out.println(result);



		/*System.out.println("AFTER [fromX=" + fromX + 
			",fromY=" + fromY+"] value = " + result.value[fromX][fromY] + ", goal = " + goalNumber(size,fromX,fromY));
			*/
		
		return result;
	}
	

	public boolean isGoal() {
		return nbOutOfPlace == 0;
	}
	
	public double hValue() {
		Puzzle.Heuristics h = ((Puzzle)getProblem()).getHeuristics();
		if (h == null) {
			return 0;
		}
		switch (h) {
			case MANHATTAN: return hManhattan; 
			case TILES_OUT_OF_PLACE: return nbOutOfPlace;
			default: throw new RuntimeException("Heuristics " + h + " unknown");
		}
	}	
	
	
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!o.getClass().equals(this.getClass())) return false;
		PuzzleState oo = (PuzzleState)o;
		if (!  (this.x == oo.x && this.y == oo.y && this.nbOutOfPlace == oo.nbOutOfPlace) ) return false;
				
		if (this.hashCode() != oo.hashCode()) return false;
		return Arrays.deepEquals(value, oo.value);
	}
	public int hashCode() {
		return x+y + Arrays.deepHashCode(value);
	}
	public Object clone() {
		return super.clone();
	}

	public String toStringWithPrefix(String prefix) {
		int size = ((Puzzle)getProblem()).getSize();
		StringBuilder s = new StringBuilder();
		/*           i
		     j       N 
		*/
			
		for(int dy = size-1; dy >= 0; dy--) {
			s.append(prefix);
			for(int dx = 0; dx < size; dx++) {				
				s.append(" | ");
				if ( value[dx][dy] != 0) {
					s.append(String.format("%3d", value[dx][dy]));
				}
				else {
					s.append("   ");
				}
			}
			s.append(" |\n");
		}
		s.append(prefix);
		s.append(" - nbOutOfPlace = ").append(nbOutOfPlace).append("\n");
		s.append(" - hManhattan = ").append(hManhattan).append("\n");
		//s.append("(x = "); s.append(x); s.append(", y = "); s.append(y); s.append(")\n");
		return s.toString();
	}	

	public String toString() {
		return toStringWithPrefix("");
	}
}