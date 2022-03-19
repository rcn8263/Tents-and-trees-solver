package tentsandtrees.backtracker;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 *  The full representation of a configuration in the TentsAndTrees puzzle.
 *  It can read an initial configuration from a file, and supports the
 *  Configuration methods necessary for the Backtracker solver.
 *
 *  @author RIT CS
 *  @author Ryan Nowak
 */
public class TentConfig implements Configuration {
    // INPUT CONSTANTS
    /** An empty cell */
    public final static char EMPTY = '.';
    /** A cell occupied with grass */
    public final static char GRASS = '-';
    /** A cell occupied with a tent */
    public final static char TENT = '^';
    /** A cell occupied with a tree */
    public final static char TREE = '%';

    // OUTPUT CONSTANTS
    /** A horizontal divider */
    public final static char HORI_DIVIDE = '-';
    /** A vertical divider */
    public final static char VERT_DIVIDE = '|';

    private static int DIM;
    private static int[] rowLook;
    private static int[] colLook;
    private char[][] field;
    private int row = 0;
    private int col = -1;

    /**
     * Construct the initial configuration from an input file whose contents
     * are, for example:<br>
     * <tt><br>
     * 3        # square dimension of field<br>
     * 2 0 1    # row looking values, top to bottom<br>
     * 2 0 1    # column looking values, left to right<br>
     * . % .    # row 1, .=empty, %=tree<br>
     * % . .    # row 2<br>
     * . % .    # row 3<br>
     * </tt><br>
     * @param filename the name of the file to read from
     * @throws IOException if the file is not found or there are errors reading
     */
    public TentConfig(String filename) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(filename));

        //Initialize variables
        this.DIM = Integer.parseInt(in.readLine());
        this.rowLook = new int[this.DIM];
        this.colLook = new int[this.DIM];
        this.field = new char[DIM][DIM];

        //Populate rowLook
        String[] fields = in.readLine().split("\\s+");
        for (int row = 0; row < this.DIM; row++) {
            this.rowLook[row] = Integer.parseInt(fields[row]);
        }
        //Populate colLook
        fields = in.readLine().split("\\s+");
        for (int col = 0; col < this.DIM; col++) {
            this.colLook[col] = Integer.parseInt(fields[col]);
        }

        //Populate field
        for (int row = 0; row < DIM; row++) {
            fields = in.readLine().split("\\s+");
            for (int col = 0; col < DIM; col++) {
                this.field[row][col] = fields[col].charAt(0);
            }
        }

        in.close();  // <3 Jimmy
    }

    /**
     * Copy constructor.  Takes a config, other, and makes a full "deep" copy
     * of its instance data.
     * @param other the config to copy
     */
    private TentConfig(TentConfig other, boolean setTent) {
        //Copy cursor position
        this.row = other.row;
        this.col = other.col;

        //Advance cursor to next cell
        this.col += 1;
        if (this.col == this.DIM) {
            this.row += 1;
            this.col = 0;
        }

        //Copy board
        this.field = new char[DIM][DIM];
        for (int row = 0; row < this.DIM; row++) {
            System.arraycopy(other.field[row], 0, this.field[row],
                    0, this.DIM);
        }

        //If setTent is true and there isn't a tree in the cell,
        // then set a tent in the cell. Otherwise, set the cell to be grass.
        if (setTent && this.field[this.row][this.col] == EMPTY) {
            this.field[this.row][this.col] = TENT;
        }
        else if (!setTent && this.field[this.row][this.col] == EMPTY) {
            this.field[this.row][this.col] = GRASS;
        }
    }

    @Override
    public Collection<Configuration> getSuccessors() {
        Collection<Configuration> successors = new LinkedList<Configuration>();
        if (!(row == DIM-1 && col == DIM-1)) {
            //Check if next cell has a tree. If it does, only create
            //a single successor.
            int r = row;
            int c = col;
            c += 1;
            if (c == this.DIM) {
                r += 1;
                c = 0;
            }
            if (field[r][c] != TREE) {
                successors.add(new TentConfig(this, true));
            }
            successors.add(new TentConfig(this, false));
        }
        return successors;
    }

    @Override
    public boolean isValid() {
        return checkTentNearTent() && checkTreeNearTent() &&
                checkRowAndCol();
    }

    /**
     * @return True if there is at least one neighboring tree.
     */
    private boolean checkTreeNearTent() {
        boolean isTree = false;
        if (field[row][col] == TENT) {
            if (row > 0) {
                if (field[row-1][col] == TREE) {isTree = true;}
            }
            if (row < DIM-1) {
                if (field[row+1][col] == TREE) {isTree = true;}
            }
            if (col > 0) {
                if (field[row][col-1] == TREE) {isTree = true;}
            }
            if (col < DIM-1) {
                if (field[row][col+1] == TREE) {isTree = true;}
            }
        }
        else {isTree = true;}

        return isTree;
    }

    /**
     * @return True if there are no neighboring tents.
     */
    private boolean checkTentNearTent() {
        boolean isTent = false;
        if (field[row][col] == TENT) {
            if (row > 0 && col > 0) {
                if (field[row-1][col-1] == TENT) {isTent = true;}
            }
            if (row > 0) {
                if (field[row-1][col] == TENT) {isTent = true;}
            }
            if (row > 0 && col < DIM-1) {
                if (field[row-1][col+1] == TENT) {isTent = true;}
            }
            if (col > 0) {
                if (field[row][col-1] == TENT) {isTent = true;}
            }
        }

        return !isTent;
    }

    /**
     * @return True if there is at least one neighboring tent next to
     * the current tree.
     */
    private boolean checkTentNearTree(int r, int c) {
        boolean isTent = false;
        if (r > 0) {
            if (field[r-1][c] == TENT) {isTent = true;}
        }
        if (r < DIM-1) {
            if (field[r+1][c] == TENT) {isTent = true;}
        }
        if (c > 0) {
            if (field[r][c-1] == TENT) {isTent = true;}
        }
        if (c < DIM-1) {
            if (field[r][c+1] == TENT) {isTent = true;}
        }

        return isTent;
    }

    /**
     * @return True if each row and column has the correct corresponding
     * number of tents. Also checks if each tree has at least one
     * neighboring tent.
     */
    private boolean checkFinalCell() {
        //check if each row has corresponding number of tents.
        int numOfTents = 0;
        for (int r = 0; r < DIM; r++) {
            for (int c = 0; c < DIM; c++) {
                if (field[r][c] == TENT) {
                    numOfTents += 1;
                }
            }
            if (numOfTents != rowLook[r]) {
                return false;
            }
            numOfTents = 0;
        }

        //check if each column has corresponding number of tents.
        for (int c = 0; c < DIM; c++) {
            for (int r = 0; r < DIM; r++) {
                if (field[r][c] == TENT) {
                    numOfTents += 1;
                }
            }
            if (numOfTents != colLook[c]) {
                return false;
            }
            numOfTents = 0;
        }

        //Checks if each tree has a neighboring tent
        for (int r = 0; r < DIM; r++) {
            for (int c = 0; c < DIM; c++) {
                if (field[r][c] == TREE) {
                    if (!checkTentNearTree(r, c)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * @return True if the number of tents currently in the selected row
     * and column does not exceed the maximum number of tents that can go
     * in that row or column.
     */
    private boolean checkRowAndCol() {
        //check if row has less than or equal to corresponding
        //number of tents.
        int numOfTents = 0;
        for (int col = 0; col < DIM; col++) {
            if (field[row][col] == TENT) {
                numOfTents += 1;
            }
        }
        if (numOfTents > rowLook[row]) {
            return false;
        }
        numOfTents = 0;

        //check if column has less than or equal to corresponding
        //number of tents.
        for (int row = 0; row < DIM; row++) {
            if (field[row][col] == TENT) {
                numOfTents += 1;
            }
        }
        if (numOfTents > colLook[col]) {
            return false;
        }
        numOfTents = 0;

        return true;
    }

    @Override
    public boolean isGoal() {
        if (row == DIM-1 && col == DIM-1) {
            return checkFinalCell();
        }
        return false;
    }

    @Override
    public String toString() {
        String config = "";

        config += " ";
        for (int i = 0; i < (2*this.DIM)-1; i++) {
            config += "-";
        }
        config += "\n";

        for (int row = 0; row < this.DIM; row++) {
            config += "|";
            for (int col = 0; col < this.DIM-1; col++) {
                config += field[row][col] + " ";
            }
            config += field[row][this.DIM-1] + "|" +
                    this.rowLook[row] + "\n";
        }

        config += " ";
        for (int i = 0; i < (2*this.DIM)-1; i++) {
            config += "-";
        }
        config += "\n ";

        for (int col = 0; col < this.DIM-1; col++) {
            config += colLook[col] + " ";
        }
        config += colLook[this.DIM-1] + "\n";

        return config;
    }
}
