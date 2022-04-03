import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.sf.javabdd.*;

public class ANTIQueenLogic implements IQueensLogic {

    private int[][] board;
    private BDD mainBDD;
    private BDD TRUE;
    private BDD FALSE;

    private BDDFactory factory;
    private int size;


    @Override
    public void initializeBoard(int size) {
        this.size = size;
        board = new int[this.size][this.size];
        mainBDD = initializeBDD(this.size);
    }

    @Override
    public int[][] getBoard() {
        System.out.println("update board er kaldt");
        updateBoard();
        return board;
    }

    @Override
    public void insertQueen(int column, int row) {

        Position position = new Position(column, row);
        
        placeQueen(position);
    }

    /**
     * Updates the board with placements of queens 
     * and positions where a queen is not allowed to be placed
     */
    private void updateBoard() {
        for(int column = 0; column < board.length; column++)
            for (int row = 0; row < board[column].length; row++) 
                if (board[column][row] == 0)
                    board[column][row] = evaluatePosition(column, row);
    }

    /**
     * Creates the Binary Decision Diagram
     * @param size The maximum number of cells in both x and y direction
     * @return A binary decision diagram 
     */
    private BDD initializeBDD(int size) {

        // Init variables
        factory = JFactory.init(2000000, 200000);
        TRUE = factory.one();
        FALSE = factory.zero();
        factory.setVarNum(size*size);
        BDD temp = TRUE;
                
        
        for (int column = 0; column < size; column++) {

            for (int row = 0; row < size; row++) {

                Position currentPosition = new Position(column, row);
                
                // Create the queen attack rules
                BDD current = createQueenAttackingRules(currentPosition);
                temp.andWith(current);
                
                // Create "one queen on each row"-rule
                if(column==0) {
                    BDD eachRow = createOneQueenOnRowRule(row);
                    temp.andWith(eachRow);
                }
            }
        }

        return temp;
    }

    /**
     * Evaluates the position to see if there can be placed a queen or not.
     * @param column The column number of the postion
     * @param row The row number of the position
     * @return 1 if the position needs to have a queen placed. -1 if a queen cannot be placed at that position. 0 if cannot be decided.
     */
    private int evaluatePosition(int column, int row) {
        BDD testPlaceQueenBDD = mainBDD.restrict(factory.ithVar(translatePosition(column, row))); // Placing a queen on the position
        BDD testNotPlacingQueenBDD = mainBDD.restrict(factory.nithVar(translatePosition(column, row))); // Not placing a queen on the position

        if (testPlaceQueenBDD.isZero()) {

            // It is not possible to place a queen on the position
            return -1;
        } else if(testNotPlacingQueenBDD.isZero()){

            // There has to be placed a queen on the position
            return 1;
        }

        return 0;
    }

    /************************************
     ******** Rule Functions ************
     ***********************************/

    /**
     * Creates a rule that there has to be one queen on the row
     * @param row Number of the row
     * @return Returns the Binary Decision Diagram that 
     */
    private BDD createOneQueenOnRowRule(int row){
        BDD eachRowRule = FALSE;
        
        eachRowRule = createRule(IntStream.range(0, size).map(i -> translatePosition(i, row)), 
                                eachRowRule, 
                                (acc, val) -> acc != null ? acc.or(factory.ithVar(val)) : factory.ithVar(val));
        
        return eachRowRule;
    }

    

    /**
     * Create the queen attacking rules for the specific position
     * @param pos The position that the rule should be created for
     * @return A binary decision diagram 
     */
    private BDD createQueenAttackingRules(Position pos) {        
        BDD tempQueenAttackingRule = TRUE;

        // Create rules for the row
        tempQueenAttackingRule = createRule(getVariablesFromSameRow(pos.getColumn(), pos.getRow()), 
                                            tempQueenAttackingRule, 
                                            (acc, val) -> acc.and(factory.nithVar(val)));
        // Create rules for the column
        tempQueenAttackingRule = createRule(getVariablesFromSameColumn(pos.getColumn(), pos.getRow()), 
                                            tempQueenAttackingRule, 
                                            (acc, val) -> acc.and(factory.nithVar(val)));

        // Create diagonal rules
        tempQueenAttackingRule = createRule(getVariablesFromDiagonals(pos.getColumn(), pos.getRow()), 
                                            tempQueenAttackingRule, 
                                            (acc, val) -> acc.and(factory.nithVar(val)));
        
        
        return factory.ithVar(translatePosition(pos)).impWith(tempQueenAttackingRule);
    }

    /**
     * Places the queen on the board at the given position
     * @param column number of the column
     * @param row number of the row
     */
    private void placeQueen(Position pos) {

        if (board[pos.getColumn()][pos.getRow()] == 0){
            mainBDD.restrictWith(factory.ithVar(translatePosition(pos)));
            board[pos.getColumn()][pos.getRow()] = 1;
        }
    }

    /******************************* 
    ******** UTIL FUNCTIONS ********
    *******************************/

    /**
     * Translates the position to an interger representation
     * @param column number of the column
     * @param row number of the row
     * @return Returns an integer value
     */
    private int translatePosition(Position pos) {

        return translatePosition(pos.getColumn(), pos.getRow());
    }

    /**
     * Translates the position to an interger representation
     * @param column number of the column
     * @param row number of the row
     * @return Returns an integer value
     */
    private int translatePosition(int column, int row) {

        return (size * row) + column;
    }

    /**
     * Creates a rule for each of variable Ids in the int stream
     * @param varIds A stream of variable ids
     * @param acc The accumulated BDD
     * @param accumulater The function to be applied on the BDD
     * @return Returns the create rule as a BDD
     */
    private BDD createRule(IntStream varIds, BDD acc, Accumulater<BDD, Integer> accumulater) {
        
        BDD tempBody = acc;
        var ids = varIds.toArray();

        for (int i : ids) {
            //System.out.println(i);
            tempBody = accumulater.apply(tempBody, i);
        }

        return tempBody;
    }

    /**
     * Gets the variable ids from the same row, except it self
     * @param column Number of the current column
     * @param row Number of the current row
     * @return Returns an intstream with variable ids from the same row except it self
     */
    private IntStream getVariablesFromSameRow(int column, int row) {

        return IntStream.range(0, size)
                        .filter(i -> i != column)
                        .map(i -> translatePosition(i, row));
    
    }

    /**
     * Gets the variable ids from the same column except it self
     * @param column Number of the current column
     * @param row Number fo the current row
     * @return Returns an int stream with variable ids from the same column except it self
     */
    private IntStream getVariablesFromSameColumn(int column, int row) {

        return IntStream.range(0, size)
                        .filter(i -> i != row)
                        .map(i -> translatePosition(column, i));
    }

    /**
     * Gets the diagonal variable ids, except it self, based on the postion
     * @param column Number of the current column 
     * @param row Number of the current row
     * @return Returns an int stream with variable ids, except it self
     */
    private IntStream getVariablesFromDiagonals(int column, int row) {
        int currentPos = translatePosition(column, row);

        // Generate top
        var leftTopCorner = IntStream.range(0, size).boxed().map(i -> new Position(column - i ,row - i));
        var rightTopCorner = IntStream.range(0, size).boxed().map(i -> new Position(column + i ,row - i));

        var top = Stream.concat(leftTopCorner, rightTopCorner);

        // Generate bottom
        var leftBottomCorner = IntStream.range(0, size).boxed().map(i -> new Position(column - i ,row + i));
        var rightBottomCorner = IntStream.range(0, size).boxed().map(i -> new Position(column + i ,row + i));
        
        var bottom = Stream.concat(leftBottomCorner, rightBottomCorner);

        // Concat bottom and top aaaaand remove varIds out of range
        return Stream.concat(top, bottom)
                                .filter(pos -> pos.getColumn() >= 0 && size > pos.getColumn() && pos.getRow() >= 0 && size > pos.getRow())
                                .mapToInt(pos -> translatePosition(pos.getColumn(), pos.getRow()))
                                .filter(i -> currentPos != i);
    }

    /**
     * Defines a function that takes a value as an input and an accumulater. It then returns the new accumulated value
     */
    private interface Accumulater<T, U> {
    
        T apply(T acc, U val);
    }

    /**
     * Represents a postion with x, y coordinates
     */
    private class Position {
        private final int row;
        private final int column;

        public Position(int column, int row) {
            this.row = row;
            this.column = column;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }
        
    }
}


