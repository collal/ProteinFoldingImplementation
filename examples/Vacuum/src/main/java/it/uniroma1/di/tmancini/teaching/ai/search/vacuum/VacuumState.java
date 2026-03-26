package it.uniroma1.di.tmancini.teaching.ai.search.vacuum;
import java.util.*;

import it.uniroma1.di.tmancini.teaching.ai.search.*;

public class VacuumState extends State {
	
	private int x;
	private int y;
	
	private boolean[][] isDirty;
	private int nbDirtyCells;
	
	
	public VacuumState(Vacuum p, int x, int y) {
		super(p);
		if (x < 0 || x >= p.getSizeX()) {
			throw new RuntimeException("x out of bounds");
		}
		if (y < 0 || y >= p.getSizeY()) {
			throw new RuntimeException("y out of bounds");
		}

		this.isDirty = new boolean[p.getSizeX()][p.getSizeY()];
		this.nbDirtyCells = 0;
		this.x = x;
		this.y = y;
	}
	
	public static VacuumState newRandom(Vacuum p) {

		VacuumState result = new VacuumState(p, p.getRandom().nextInt(p.getSizeX()), p.getRandom().nextInt(p.getSizeY()) );
		result.nbDirtyCells = 0;
		boolean b; // = false;
		for(int dx = 0; dx < p.getSizeX(); dx++) {
			for(int dy = 0; dy < p.getSizeY(); dy++) {
				b = p.getRandom().nextBoolean();
				result.isDirty[dx][dy] = b;
				if (b) result.nbDirtyCells++;
			}
		}
		return result; 
	}


	public Collection<? extends Action> executableActions() {
		List<VacuumAction> result = new ArrayList<VacuumAction>();
		Vacuum v = (Vacuum)getProblem();
		if (x > 0) {
			result.add( VacuumAction.goLeft() );
		}
		if (y > 0) {
			result.add( VacuumAction.goDown() );
		}
		if (x < v.getSizeX()-1) {
			result.add( VacuumAction.goRight() );
		}
		if (y < v.getSizeY()-1) {
			result.add( VacuumAction.goUp() );
		}

		if (isDirty[x][y]) {
			result.add( VacuumAction.suck() );
		}
		return result;				
	}
	
	public State resultingState(Action a) {
		VacuumState result = (VacuumState)this.clone();
		Vacuum p = (Vacuum)this.getProblem();
		
		result.isDirty = new boolean[p.getSizeX()][p.getSizeY()];
		for(int dx = 0; dx < p.getSizeX(); dx++) {
			if (p.getSizeY() >= 0)
				System.arraycopy(this.isDirty[dx], 0, result.isDirty[dx], 0, p.getSizeY());
		}
		
		if (a == VacuumAction.goLeft() ) {
			result.x--;
		}
		else if (a == VacuumAction.goRight() ) {
			result.x++;
		}
		else if (a == VacuumAction.goUp() ) {
			result.y++;
		}
		else if (a == VacuumAction.goDown() ) {
			result.y--;
		}
		else if (a == VacuumAction.suck() ) {
			result.isDirty[x][y] = false;
			result.nbDirtyCells--;
		}
		return result;
	}
	
	public boolean isGoal() {
		return nbDirtyCells == 0;
	}
	
	public double hValue() {
		return nbDirtyCells;
	}	
	
	
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!o.getClass().equals(this.getClass())) return false;
		VacuumState oo = (VacuumState)o;
		if (!  (this.x == oo.x && this.y == oo.y && this.nbDirtyCells == oo.nbDirtyCells) ) return false;
				
		if (this.hashCode() != oo.hashCode()) return false;
		return Arrays.deepEquals(isDirty, oo.isDirty);
	}
	public int hashCode() {
		return x+y + Arrays.deepHashCode(isDirty);
	}
	public Object clone() {
		return super.clone();
	}

	public String toStringWithPrefix(String prefix) {
		Vacuum v = (Vacuum)getProblem();
		StringBuilder s = new StringBuilder();
		/*           i
		     j      ~X    (~ = dirty,  X=current position)
		*/
			
		for(int dy = v.getSizeY()-1; dy >= 0; dy--) {
			s.append(prefix);
			for(int dx = 0; dx < v.getSizeX(); dx++) {				
				s.append(" | ");
				if ( isDirty[dx][dy]) s.append("~");
				else s.append(" ");
				
				if ( x == dx && y == dy) s.append("X");
				else s.append(" ");
			}
			s.append(" |\n");
		}
		s.append(prefix);
		return s.toString();
	}	

	public String toString() {
		return toStringWithPrefix("");
	}
}