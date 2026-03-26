package it.uniroma1.di.tmancini.teaching.ai.search.vacuum;
import java.util.*;


import it.uniroma1.di.tmancini.teaching.ai.search.*;

/**
	For this simple problem, actions could be represented with an enum.
	Since we need to extend Action, we mimic enum behaviour while defining a subclass.
	In particular, 5 objects are defined for this class at load time.
*/


public class VacuumAction extends Action {

	private enum ACTION { left, right, up, down, suck };
	private final ACTION a;
	private final double cost;


	private VacuumAction(ACTION a, double cost) {
		this.a = a;
		this.cost = cost;
	}

	private static final Map<ACTION, VacuumAction> actions; // all possible actions
	
	static {
		// Create objects for all possible actions at class load time
		actions = new HashMap<>();
		actions.put( ACTION.left, new VacuumAction(ACTION.left, 1) );
		actions.put( ACTION.right, new VacuumAction(ACTION.right, 1) );
		actions.put( ACTION.up, new VacuumAction(ACTION.up, 1) );
		actions.put( ACTION.down, new VacuumAction(ACTION.down, 1) );
		actions.put( ACTION.suck, new VacuumAction(ACTION.suck, 1) );		
	}
	
	public static VacuumAction goLeft() {
		return actions.get(ACTION.left);
	}
	public static VacuumAction goRight() {
		return actions.get(ACTION.right);
	}
	public static VacuumAction goUp() {
		return actions.get(ACTION.up);
	}
	public static VacuumAction goDown() {
		return actions.get(ACTION.down);
	}
	public static VacuumAction suck() {
		return actions.get(ACTION.suck);
	}

	public double getCost() {
		return this.cost;
	}

	public boolean equals(Object o) {
		if (o == null) return false;
		if (!o.getClass().equals(this.getClass())) return false;
		VacuumAction oo = (VacuumAction)o;
		return this.a == oo.a;
	}
	public int hashCode() {
		return a.hashCode(); 
	}

	public String toString() {
		return a.toString();
	}
}