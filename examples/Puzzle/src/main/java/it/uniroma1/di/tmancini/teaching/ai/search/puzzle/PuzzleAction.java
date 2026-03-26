package it.uniroma1.di.tmancini.teaching.ai.search.puzzle;
import java.util.*;
import it.uniroma1.di.tmancini.teaching.ai.search.*;


/**
	For this simple problem, actions could be represented with an enum.
	Since we need to extend Action, we mimic enum behaviour while defining a subclass.
	In particular, 4 objects are defined for this class at load time.
*/


public class PuzzleAction extends Action {

	private static enum ACTION { slideLeft, slideRight, slideUp, slideDown };	
	private ACTION a;
	private double cost;


	private PuzzleAction(ACTION a, double cost) {
		this.a = a;
		this.cost = cost;
	}

	private static Map<ACTION, PuzzleAction> actions; // all possible actions
	
	static {
		// Create objects for all possible actions at class load time
		actions = new HashMap<ACTION, PuzzleAction>();
		actions.put( ACTION.slideLeft, new PuzzleAction(ACTION.slideLeft, 1) );
		actions.put( ACTION.slideRight, new PuzzleAction(ACTION.slideRight, 1) );
		actions.put( ACTION.slideUp, new PuzzleAction(ACTION.slideUp, 1) );
		actions.put( ACTION.slideDown, new PuzzleAction(ACTION.slideDown, 1) );	
	}
	
	public static PuzzleAction slideLeft() {
		return actions.get(ACTION.slideLeft);
	}
	public static PuzzleAction slideRight() {
		return actions.get(ACTION.slideRight);
	}
	public static PuzzleAction slideUp() {
		return actions.get(ACTION.slideUp);
	}
	public static PuzzleAction slideDown() {
		return actions.get(ACTION.slideDown);
	}

	public double getCost() {
		return this.cost;
	}

	public boolean equals(Object o) {
		if (o == null) return false;
		if (!o.getClass().equals(this.getClass())) return false;
		PuzzleAction oo = (PuzzleAction)o;
		return this.a == oo.a;
	}
	public int hashCode() {
		return a.hashCode(); 
	}

	public String toString() {
		return a.toString();
	}
}