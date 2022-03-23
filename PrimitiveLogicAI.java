
/**
 * This class implements a basic logic for the n-queens problem to get you started.
 * Actually, when inserting a queen, it only puts the queen where requested
 * and does not keep track of which other positions are made illegal by this move.
 *
 * @author Mai Ajspur
 * @version 16.02.2018
 */

public class PrimitiveLogicAI implements IQueensLogic{
    private int size;		// Size of quadratic game board (i.e. size = #rows = #columns)
    private int[][] board;	// Content of the board. Possible values: 0 (empty), 1 (queen), -1 (no queen allowed)

    /**
     * Initializes the quadratic board with the given size and initializes the board according to the rules
     * of the n-queen problem.
     * @param size The size of the board ( i.e. size = #rows = #columns)
     */
    public void initializeBoard(int size) {
        this.size = size;
        this.board = new int[size][size];
    }

    /**
     * Return a representation of the board where each entry [c][r] is either
     *  1 : a queen is or must be present in column c and row r,
     * -1 : a queen cannot be present in column c and row r
     *  0 : a queen has not yet been placed in column c and row r. It does not have to
     *      be there but it is allowed to be there.
     * Columns are counted from left to right (starting with 0),
     * and rows are counted from top to bottom (counting from 0).
     */
    public int[][] getBoard() {
        return board;
    }

    /**
     * Inserts a queen at the specified position and updates the rest of the board accordingly,
     * that is afterwards the board specifies where there _must_ be queens and where there _cannot_ be queens.
     */
    public void insertQueen(int column, int row) {
        //BDDFactory fact = JFactory.init(2000000,200000); // The two numbers represents the node number and the cache size.
        //int nVars = getBoard().length * getBoard().length;
        //fact.setVarNum(nVars);
        //System.out.println("The node table: "); // A row contains nodeID, variable number, low_nodeID, high_nodeID
        // The nodeID is the first number in the square brackets
        // fact.printAll(); // Here, BDDs (nodes) for the requested number of variables (unnegated and negated) are already included

        boolean rowAllowed = false;
        boolean columnAllowed = false;
        boolean diagonalAllowed = false;
        if (isPositionAvailable(column, row) && !isPositionReserved(column, row)) {
            columnAllowed = isColumnAvailable(column, row);
            rowAllowed = isRowAvailable(column, row);
            diagonalAllowed = isDiagonalAvailable(column, row);
        }
        if(columnAllowed && rowAllowed && diagonalAllowed){
            reserveColumn(column, row);
            reserveRow(column, row);
            reserveDiagonal(column, row);
            board[column][row] = 1;
        }

        printBoardMatrix();
    }

    private boolean isPositionAvailable(int column, int row) {
        if (board[column][row] == 0) {
            System.out.println("Column:[" + column +"] Row:[" + row + "] is available");
            return true;
        } else {
            System.out.println("Column:[" + column +"] Row:[" + row + "] is not available");
            return false;
        }
    }

    private boolean isPositionReserved(int column, int row) {
        if (board[column][row] == -1) {
            System.out.println("Position [" + column +"][" + row + "] is reserved");
            return true;
        } else {
            System.out.println("Position [" + column +"][" + row + "] is not reserved");
            return false;
        }
    }

    private boolean isColumnAvailable(int column, int row) {
        System.out.println("CHECK COLUMN");

        for (int i = 0; i < getBoard().length; i++) {
            System.out.println("Column: " + column + " Row: " + i);

            //skip own position
            if (i == row) {
                System.out.println("Skip own position");
                continue;
            }

            //check if there already has a queen in that line
            if (board[column][i] == 1) {
                System.out.println("Position [" + column +"][" + i + "]: "  + board[column][i] + " is taken");
                return false;
            } else {
                System.out.println("Position [" + column +"][" + i + "]: "  + board[column][i] + " is not taken");
                continue;
            }
        }
        return true;
    }

    private boolean isRowAvailable(int column, int row) {
        System.out.println("CHECK ROW");

        for (int i = 0; i < getBoard().length; i++) {
            System.out.println("Column: " + i + " Row: " + row);

            //skip own position
            if (i == column) {
                System.out.println("Skip own position");
                continue;
            }

            //check if there already has a queen in that line
            if (board[i][row] == 1) {
                System.out.println("Position [" + i +"][" + row + "]: "  + board[i][row] + " is taken");
                return false;
            } else {
                System.out.println("Position [" + i +"][" + row + "]: "  + board[i][row] + " is not taken");
                continue;
            }
        }
        return true;
    }

    private boolean isDiagonalAvailable(int column, int row) {
        System.out.println("CHECK DIAGONAL");

        //Q1
        for (int i = column; i >= 0; i--) {
            for (int j = row; j >= 0; j--) {
                System.out.println("(--) Column: " + i + " Row: " + j);
            }
        }

        /*Q2
        for (int i = column; i >= 0; i--) {
            for (int j = row; j < getBoard().length; j++) {
                System.out.println("(-+) Column: " + i + " Row: " + j);
            }
        }
        */

        //Q3
        for (int i = column; i < getBoard().length; i++) {
            for (int j = row; j < getBoard().length; j++) {
                System.out.println("(++) Column: " + i + " Row: " + j);
            }
        }

        //Q4
/*        for (int i = column; i > 0; i++) {
            for (int j = row; j > 0; j--) {
                System.out.print("(+-) Column: " + i + " Row: " + j);
            }
        }*/

        return true;
    }


    private void reserveRow(int column, int row) {
        System.out.println("RESERVE ROW");

        for (int i = 0; i < getBoard().length; i++) {
            System.out.println("Column: " + column + " row: " + i);
            if (i == row) continue; // skip its own position check
            else if (board[column][i] == 0) {
                System.out.println("Reserving Position [" + column +"][" + i + "]");
                board[column][i] = -1;
            }
        }
    }

    private void reserveColumn(int column, int row) {
        System.out.println("RESERVE COLUMN");

        for (int i = 0; i < getBoard().length; i++) {
            System.out.println("Column: " + i + " Row: " + row);
            if (i == column) continue; // skip its own position check
            else if (board[i][row] == 0) {
                System.out.println("Reserving Position [" + i +"][" + row + "]");
                board[i][row] = -1;
            }
        }
    }

    private void reserveDiagonal(int column, int row) {
        System.out.println("RESERVE DIAGONAL");

        //Q1
        for (int i = column; i >= 0; i--) {
            for (int j = row; j >= 0; j--) {
                if(column -i == row -j) {
                    System.out.println("(--) Column: " + i + " Row: " + j);
                    board[i][j] = -1;
                }
            }
            System.out.println();
        }

        /*Q2
        System.out.println(column + " " + row);
        System.out.println();

        for (int i = column; i < getBoard().length; i++) {
            for (int j = row; j >= 0; j--) {
                board[i][j] = -1;
                System.out.println("(+-) Column: " + i + " Row: " + j);

                if(column -j == row +i) {
                    System.out.println("(InnerLoop) Column: " + i + " Row: " + j);
                }

                if(Math.abs(column -j) == row -i) {
                    //System.out.println("(-+) Column: " + i + " Row: " + j);
                    //board[i][j] = -1;
                }
            }
            System.out.println();
        }
        */

        //Q3
        for (int i = column; i < getBoard().length; i++) {
            for (int j = row; j < getBoard().length; j++) {
                if(column +j == row +i) {
                    System.out.println("(++) Column: " + i + " Row: " + j);
                    board[i][j] = -1;
                }
            }
            System.out.println();
        }

        //Q4
        /*
        for (int i = column; i >= 0; i--) {
            for (int j = row; j < getBoard().length; j++) {
                board[i][j] = -1;
                System.out.println("(-+) Column: " + i + " Row: " + j);

                if(column -j == row +i) {
                    System.out.println("(InnerLoop) Column: " + i + " Row: " + j);
                }

                if(Math.abs(column -j) == row -i) {
                    //System.out.println("(-+) Column: " + i + " Row: " + j);
                    //board[i][j] = -1;
                }
            }
            System.out.println();
        }
        */
    }

    private void printBoardMatrix() {
        System.out.println("\nBoard[" +getBoard().length + "][" +getBoard().length + "]");
        for (int i = 0; i < getBoard().length; i++) {
            for (int j = 0; j < getBoard().length; j++) {
                System.out.print(String.format( "%5d", board[j][i]));
            }
            System.out.println();
        }
        System.out.println();
    }

}
