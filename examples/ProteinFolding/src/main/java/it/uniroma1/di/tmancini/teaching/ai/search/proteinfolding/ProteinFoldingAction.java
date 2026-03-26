package it.uniroma1.di.tmancini.teaching.ai.search.proteinfolding;

import it.uniroma1.di.tmancini.teaching.ai.search.Action;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ProteinFoldingAction extends Action {

    public static final int OFFSET = 6; // use this to give cost of a step; helps make an admissibile heuristic

    public static enum ACTION {
        // do not need to know if we put a H or a P -> if we put a P, will always have numContacts 0

        goLeft0('L', 0), goLeft1('L', 1), goLeft2('L', 2), goLeft3('L', 3),
        goRight0('R', 0), goRight1('R', 1), goRight2('R', 2), goRight3('R', 3),
        goUp0('U', 0), goUp1('U', 1), goUp2('U', 2), goUp3('U', 3),
        goDown0('D', 0), goDown1('D',1), goDown2('D',2), goDown3('D', 3),
        initialAction('X', 0);

        public final int numContacts;
        public final char dir;

        private ACTION(char dir, int numContacts) {
            this.dir = dir;
            this.numContacts = numContacts;
        }

        public static ACTION valueOfLabel(char dir, int numContacts) throws IllegalArgumentException {
            for (ACTION a : values()) {
                if (a.dir == dir && a.numContacts == numContacts ) {
                    return a;
                }
            }
            throw new IllegalArgumentException();
        }
    };


    private ACTION action;
    private double cost;
    private char dir;

    private ProteinFoldingAction(ACTION a, double cost, char dir) {
        this.action = a;
        this.cost = cost;
        this.dir = dir;
    }

    private static Map<ACTION, ProteinFoldingAction> actions; // all possible actions

    static {
        // Create objects for all possible actions at class load time
        actions = new HashMap<>();
        Stream.of(ACTION.values()).forEach(action -> actions.put(action,
                new ProteinFoldingAction(action, ProteinFoldingAction.numContactsToCost(action.numContacts), action.dir)));
    }

    public static ProteinFoldingAction move(char dir, int numContacts) {
        return actions.get(ACTION.valueOfLabel(dir, numContacts));
    }

    private static double numContactsToCost(int numContacts) {
        // use this value to keep costs positive
        return OFFSET - numContacts;
    }

    public static double costToEnergy(double cost) {
        return cost - OFFSET;
    }


    @Override
    public double getCost() {
        return this.cost;
    }

    public char getDir() {
        return this.dir;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (!o.getClass().equals(this.getClass())) return false;
        ProteinFoldingAction oo = (ProteinFoldingAction) o;
        return this.action == oo.action;
    }
    public int hashCode() {
        return action.hashCode();
    }

    public String toString() {
        return action.toString();
    }

    public int getNumContacts() {
        return this.action.numContacts;
    }
}
