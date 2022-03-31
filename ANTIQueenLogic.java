
import java.util.stream.IntStream;

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
        updateBoard();
        return board;
    }

    @Override
    public void insertQueen(int column, int row) {

        placeQueen(column, row);
        System.out.println("Clicked");
    }

    private void updateBoard() {
        for(int column = 0; column < board.length; column++)
            for (int row = 0; row < board[column].length; row++) 
                if (board[column][row] == 0)
                    board[column][row] = evaluatePosition(column, row);
    }

    private BDD initializeBDD(int size) {

        // Init variables
        factory = JFactory.init(2000000, 200000);
        TRUE = factory.one();
        FALSE = factory.zero();
        factory.setVarNum(size*size);
        BDD temp = TRUE;
                
        
        for (int column = 0; column < size; column++) {

            for (int row = 0; row < size; row++) {

                System.out.println(translatePosition(column, row));
                BDD current = createHorizontalAndVerticalRules(column, row);

                temp.andWith(current);

                if(column==0){
                    BDD eachRow = createEachRowRule(column,row);
                    temp.andWith(eachRow);
                }
            }
        }

        return temp;
    }

    private BDD createEachRowRule(int column, int row){
        BDD eachRowRule = FALSE;
        
        eachRowRule = createRule(IntStream.range(0, size).map(i -> translatePosition(i, row)), 
                                eachRowRule, 
                                (acc, val) -> acc != null ? acc.or(factory.ithVar(val)) : factory.ithVar(val));
        
        return eachRowRule;
    }

    private int evaluatePosition(int column, int row) {
        BDD testBDD = mainBDD.restrict(factory.ithVar(translatePosition(column, row)));

        if(testBDD.isOne()) {
            System.out.println("first check");
            System.out.println("whole thing solved");
            return 1;
        } else if (testBDD.isZero()) {
            System.out.println("second check");
            return -1;
        }

        BDD newTestBDD = mainBDD.restrict(factory.nithVar(translatePosition(column, row)));
        if(newTestBDD.isZero()){
            System.out.println("third check");
            return 1;
        }

        return 0;
    }

    private BDD createHorizontalAndVerticalRules(int column, int row) {        
        BDD columnAndRowFalseRule = TRUE;

        columnAndRowFalseRule = createRule(getVariablesFromSameRow(column, row), columnAndRowFalseRule, (acc, val) -> acc != null ? acc.and(factory.nithVar(val)) : factory.nithVar(val));
        columnAndRowFalseRule = createRule(getVariablesFromSameColumn(column, row), columnAndRowFalseRule, (acc, val) -> acc != null ? acc.and(factory.nithVar(val)) : factory.nithVar(val));
        
        
        return factory.ithVar(translatePosition(column, row)).impWith(columnAndRowFalseRule);
    }

    private void placeQueen(int column, int row) {
        mainBDD.restrictWith(factory.ithVar(translatePosition(column, row)));
        board[column][row] = 1;
    }

    /* UTIL FUNCTIONS */

    /**
     * Translate the position to an interger representation
     * @param column
     * @param row
     * @return Returns an integer value
     */
    private int translatePosition(int column, int row) {

        return (size * column) + row;
    }

    private BDD createRule(IntStream varIds, BDD acc, Accumulater<BDD, Integer> accumulater) {
        
        BDD tempBody = acc;

        for (int i : varIds.toArray()) {
            System.out.println(i);
            tempBody = accumulater.apply(tempBody, i);
        }

        return tempBody;
    }

    private BDD createRule(IntStream varIds, Accumulater<BDD, Integer> accumulater) {
        
        return createRule(varIds, null, accumulater);
    }

    private IntStream getVariablesFromSameRow(int column, int row) {

        return IntStream.range(0, size).filter(i -> i != column).map(i -> translatePosition(i, row));
    }

    private IntStream getVariablesFromSameColumn(int column, int row) {

        return IntStream.range(0, size).filter(i -> i != row).map(i -> translatePosition(column, i));
    }

    private interface Accumulater<T, U> {
    
        T apply(T acc, U val);
    }
}
