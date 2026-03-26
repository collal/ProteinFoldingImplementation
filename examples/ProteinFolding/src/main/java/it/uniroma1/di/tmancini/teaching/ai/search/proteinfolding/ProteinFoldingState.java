package it.uniroma1.di.tmancini.teaching.ai.search.proteinfolding;

import it.uniroma1.di.tmancini.teaching.ai.search.Action;
import it.uniroma1.di.tmancini.teaching.ai.search.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ProteinFoldingState extends State {

    private int numCellsPlaced;
    private int xCurr,  yCurr; // most recent cell visited
    private int xPrev, yPrev; // need to track prev cell for calculating number of contacts
    private double energy, cost;

    public class CartesianCharGrid implements Cloneable{
        // maps a cartesian grid to an array

        public static final char NULLCHAR = '\u0000';

        private int minX, maxX, minY, maxY;
        private char[][] grid;

        public CartesianCharGrid(int minX, int maxX, int minY, int maxY) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;

            grid = new char[maxX-minX+1][maxY-minY+1];
            for (int i = 0; i < maxX-minX+1; i++) {
                for (int  j = 0; j < maxY-minY+1; j++) {
                    grid[i][j] = NULLCHAR;
                }
            }
        }

        public void replace(int x, int y, char val) throws IllegalArgumentException {
            try{
                grid[x-minX][y-minY] = val;
            } catch(ArrayIndexOutOfBoundsException e){
                throw new IllegalArgumentException();
            }
        }

        public boolean isEmpty(int x, int y) throws IllegalArgumentException {
            try {
                return grid[x-minX][y-minY] == NULLCHAR;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException();
            }
        }

        public char get(int x, int y) throws IllegalArgumentException {
            try {
                return grid[x-minX][y-minY];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException();
            }
        }

        public int getMinX() {
            return minX;
        }

        public int getMaxX() {
            return maxX;
        }

        public int getMinY() {
            return minY;
        }

        public int getMaxY() {
            return maxY;
        }

        public CartesianCharGrid clone() {
            CartesianCharGrid clone = new  CartesianCharGrid(minX, maxX, minY, maxY);
            char[][] newvals = new char[maxX-minX+1][maxY-minY+1];
            for (int i = 0; i < maxX-minX+1; i++) {
                for (int  j = 0; j < maxY-minY+1; j++) {
                    newvals[i][j] = grid[i][j];
                }
            }
            clone.grid = newvals;
            return clone;
        }

    }

    private CartesianCharGrid values;

    public ProteinFoldingState(ProteinFolding p) {
        super(p);
        numCellsPlaced = 0;
        xCurr = p.getXStart();
        yCurr = p.getYStart();
        xPrev = xCurr;
        yPrev = yCurr;
        energy = 0;
        cost = 0;
        values = new CartesianCharGrid(p.getMinX(), p.getMaxX(), p.getMinY(), p.getMaxY());
        hPairs = this.numPairs() * ProteinFoldingAction.OFFSET;
    }

    public static ProteinFoldingState initialState(ProteinFolding p) {
        return new ProteinFoldingState(p);
    }

    @Override
    public Collection<? extends Action> executableActions() {
        List<ProteinFoldingAction> result = new ArrayList<ProteinFoldingAction>();

        if (this.numCellsPlaced == 0) {
            // only one move allowed for first move
            result.add(ProteinFoldingAction.move('X', 0));
            return result;
        }
        // we do the following: check if move is valid, and if so, check # of contacts and add
        // corresponding action to list of possible actions

        // left
        if (xCurr > values.getMinX() && isEmpty(xCurr-1, yCurr)) {
            result.add( ProteinFoldingAction.move('L', this.numContacts(xCurr-1, yCurr)));
        }

        // right
        if (xCurr < values.getMaxX() && isEmpty(xCurr+1, yCurr)) {
            result.add( ProteinFoldingAction.move('R', this.numContacts(xCurr+1, yCurr)));
        }

        // down
        if (yCurr > values.getMinY() && isEmpty(xCurr, yCurr-1)) {
            result.add( ProteinFoldingAction.move('D', this.numContacts(xCurr, yCurr-1)));
        }

        // up
        if (yCurr < values.getMaxY() && isEmpty(xCurr, yCurr+1)) {
            result.add( ProteinFoldingAction.move('U', this.numContacts(xCurr, yCurr+1)));
        }

        return result;
    }

    private boolean isEmpty(int x, int y) {
        return this.values.isEmpty(x,y);
    }

    private boolean sameAsCurr(int x, int y) {
        return x == xCurr && y == yCurr;
    }

    private int numContacts(int x, int y) {
        if (((ProteinFolding)this.getProblem()).getProtein().charAt(numCellsPlaced) == 'P') {
            return 0;
        }

        int result = 0;
        if (x > values.getMinX() && !this.sameAsCurr(x-1, y) && values.get(x-1, y) == 'H') { result += 1;}
        if (x < values.getMaxX()  && !this.sameAsCurr(x+1, y) && values.get(x+1, y) == 'H') { result += 1;}
        if (y > values.getMinY() && !this.sameAsCurr(x, y-1) && values.get(x, y-1) == 'H') { result += 1;}
        if (y  < values.getMaxY() && !this.sameAsCurr(x, y+1) && values.get(x, y+1) == 'H') { result += 1;}
        return result;
    }

    private int hPairs;

    private int numPairs() {
        // finds the number of pairs of non-consecutive Hs
        int result = 0;
        String protein = ( (ProteinFolding)this.getProblem()).getProtein();
        for (int i = 0; i < ( (ProteinFolding)this.getProblem()).getSize(); i++) {
            if (protein.charAt(i) == 'H') {
                for (int j = i + 2; j < ((ProteinFolding) this.getProblem()).getSize(); j++) {
                    if (protein.charAt(j) == 'H') {
                        result += 1;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public State resultingState(Action a) {
        ProteinFoldingState result = this.clone();
        int numPlaced = this.numCellsPlaced;

        char dir = ((ProteinFoldingAction)a).getDir();
        double cost = a.getCost();

        // place next protein
        xPrev = xCurr;
        yPrev = yCurr;

        if (dir == 'L') {
            result.values.replace(xCurr-1, yCurr, ((ProteinFolding)this.getProblem()).getProtein().charAt(numCellsPlaced));
            result.xCurr = xCurr-1;
        } else if (dir == 'R') {
            result.values.replace(xCurr+1, yCurr, ((ProteinFolding)this.getProblem()).getProtein().charAt(numCellsPlaced));
            result.xCurr = xCurr+1;
        } else if (dir == 'D') {
            result.values.replace(xCurr, yCurr-1, ((ProteinFolding)this.getProblem()).getProtein().charAt(numCellsPlaced));
            result.yCurr = yCurr-1;
        } else if (dir == 'U') {
            result.values.replace(xCurr, yCurr+1, ((ProteinFolding)this.getProblem()).getProtein().charAt(numCellsPlaced));
            result.yCurr = yCurr+1;
        } else if (dir == 'X') {
            // first action
            result.values.replace(xCurr, yCurr, ((ProteinFolding)this.getProblem()).getProtein().charAt(numCellsPlaced));
        }

        result.numCellsPlaced = numPlaced + 1;
        result.cost += cost;
        result.energy += ProteinFoldingAction.costToEnergy(cost);

        result.hPairs -= ProteinFoldingAction.OFFSET * (-(int) result.energy);

        return result;
    }

    @Override
    public boolean isGoal() {
        return numCellsPlaced == ((ProteinFolding) (this.getProblem())).getSize();
    }

    public int hashCode() {
        return xCurr+yCurr + Arrays.deepHashCode(values.grid);
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (!o.getClass().equals(this.getClass())) return false;
        ProteinFoldingState oo = (ProteinFoldingState)o;
        if (!  (this.xCurr == oo.xCurr && this.yCurr == oo.yCurr && this.numCellsPlaced == oo.numCellsPlaced) ) return false;


        if (this.hashCode() != oo.hashCode()) return false;
        return Arrays.deepEquals(values.grid, oo.values.grid);
    }

    public String toStringWithPrefix(String prefix) {
        int xMin = ((ProteinFolding)(this.getProblem())).getMinX();
        int xMax = ((ProteinFolding)(this.getProblem())).getMaxX();
        int yMin = ((ProteinFolding)(this.getProblem())).getMinY();
        int yMax = ((ProteinFolding)(this.getProblem())).getMaxY();

        StringBuilder s = new StringBuilder();
		/*           i
		     j       N
		*/

        for(int dy = yMax; dy >= yMin; dy--) {
            for(int dx = xMin; dx <= xMax; dx++) {
                s.append(" | ");
                if ( ! values.isEmpty(dx, dy) ) {
                    s.append(String.format(" %c ", values.get(dx, dy)));
                }
                else {
                    s.append("   ");
                }
            }
            s.append(" |\n");
        }
        s.append(prefix);
        s.append(" - nbPlaced = ").append(numCellsPlaced).append("\n");
        return s.toString();
    }

    public String toString() {
        return toStringWithPrefix("");
    }

    @Override
    public ProteinFoldingState clone() {
        ProteinFoldingState myClone = (ProteinFoldingState) super.clone();
        myClone.values = this.values.clone();
        return myClone;
    }

    public static ProteinFoldingState getFinalState(ProteinFoldingState initialState, List<Action> actions) {
        ProteinFoldingState finalState = initialState;
        for (Action action : actions) {
            finalState = (ProteinFoldingState) finalState.resultingState(action);
        }
        return finalState;
    }

    public double getEnergy() {
        return energy;
    }

    public double hValue() {
        ProteinFolding.Heuristics h = ((ProteinFolding)getProblem()).getHeuristics();
        if (h == null) {
            return 0;
        }
        switch (h) {
            case PAIRS: return hPairs;
            default: throw new RuntimeException("Heuristics " + h + " unknown");
        }
    }


}
