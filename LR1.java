// Lucas Fawcett
// IT327
// LR1 parser
import java.io.*;
import java.util.*;

public class LR1 {

    // token class to store the symbol, value of symbol, and the current state
    private static class Token {
        String symbol;
        double value;
        int state;

        Token(String symbol, double value, int state){
            this.symbol = symbol;
            this.state = state;
            this.value = value;
        }
    }

    // String array to go through each token in the expression and a stack to keep track of the current tokens
    private static String[] tokens;
    private static Stack<Token> currEval;

    // 2d array to store the parsing table for the grammar
    private static String[][] parseTable = {
            {"s5", null, null, null, null, "s4", null, null, "1", "2", "3"},
            {null, "s6", "s6", null, null, null, null, "a", null, null, null},
            {null, "r3", "r3", "s7", "s7", null, "r3", "r3", null, null, null},
            {null, "r6", "r6", "r6", "r6", null, "r6", "r6", null, null, null},
            {"s5", null, null, null, null, "s4", null, null, "8", "2", "3"},
            {null, "r8", "r8", "r8", "r8", null, "r8", "r8", null, null, null},
            {"s5", null, null, null, null, "s4", null, null, null, "9", "3"},
            {"s5", null, null, null, null, "s4", null, null, null, null, "10"},
            {null, "s6", "s6", null, null, null, "s11", null, null, null, null},
            {null, "r1", "r1", "s7", "s7", null, "r1", "r1", null, null, null},
            {null, "r4", "r4", "r4", "r4", null, "r4", "r4", null, null, null},
            {null, "r7", "r7", "r7", "r7", null, "r7", "r7", null, null, null}
        };

    // main
    public static void main(String[] args){
        // check for correct execution
        if (args.length != 1){
            System.out.println("Please include an expression.");
        }
        // output the steps and value
        else {
            parseExpression(args[0]);
            calculateValue();
        }
    }

    // takes the expression and prepares the tokens array
    private static void parseExpression(String exp){
        // remove whitespace
        exp = exp.replaceAll("\\s+","");
        // split the expression by the operators and parenthesis
        String[] tempTokens = exp.split("(?<=[+-/*()])|(?=[+-/*()])");
        // copy tokens to the tokens string and add the ending symbol $
        tokens = new String[tempTokens.length + 1];
        for (int i = 0; i < tempTokens.length; i++){
            tokens[i] = tempTokens[i];
        }
        tokens[tokens.length - 1] = "$";
    }

    // returns an int value for the column matching the symbol in the table
    private static int getColumn(String symbol){
        switch (symbol) {
            case "n": return 0;
            case "+": return 1;
            case "-": return 2;
            case "*": return 3;
            case "/": return 4;
            case "(": return 5;
            case ")": return 6;
            case "$": return 7;
            case "E": return 8;
            case "T": return 9;
            case "F": return 10;
            default: return -1;
        }
    }

    // check if a string is a number
    private static boolean isNumber(String symbol){
        try {
            Integer.parseInt(symbol);
            return true;
        }
        // if parseInt doesnt work, it is not a number
        catch (Exception e) {
            return false;
        }
    }

    // Printing helper function to output the current situation in the current step
    private static void printCurrentEvaluation(int tokenIndex){
        Stack<Token> reverse = new Stack<Token>();
        while (!currEval.empty()){
            reverse.push(currEval.pop());
        }
        System.out.print("Stack:[(" + reverse.peek().symbol + ":" + reverse.peek().state + ")");
        currEval.push(reverse.pop());
        while (!reverse.empty()){
            currEval.push(reverse.pop());
            System.out.print(" (" + currEval.peek().symbol);
            if (!(currEval.peek().symbol.equals("+") || currEval.peek().symbol.equals("-") || currEval.peek().symbol.equals("*") || currEval.peek().symbol.equals("/"))){
                System.out.printf("=%.0f", currEval.peek().value);
            }
            System.out.print(":" + currEval.peek().state + ")");
        }
        System.out.print("]\tExp:[" + tokens[tokenIndex]);
        for (int i = tokenIndex + 1; i < tokens.length; i++){
            System.out.print(" " + tokens[i]);
        }
        System.out.println("]");
    }

    // Calculates the expression
    private static void calculateValue(){
        // set up the stack with an arbitrary token that tells the parser to start at state 0
        currEval = new Stack<Token>();
        currEval.push(new Token("S", 0, 0));
        // keep track of current token to parse
        int tokenIndex = 0;

        // loop through expression until it fails or accepts
        boolean accept = false;
        while (!accept){
            // get the current token and create a token object
            String tokenString = tokens[tokenIndex];
            Token currToken;
            if (isNumber(tokenString)){
                currToken = new Token("n", Integer.parseInt(tokenString), 0);
            }
            else {
                currToken = new Token(tokenString, 0, 0);
            }

            // do not change the current token until we shift and perform all reductions necessary
            boolean shifted = false;
            while(!shifted){
                //output current situation
                printCurrentEvaluation(tokenIndex);
                // find what to do based on the state and symbol seen
                int currState = currEval.peek().state;
                int symbolCol = getColumn(currToken.symbol);
                String toDo = parseTable[currState][symbolCol];
                // if null, the expression is invalid
                if (toDo == null){
                    System.out.println("Invalid expression.");
                    return;
                }
                // if a, accept and exit loops
                else if (toDo.equals("a")){
                    accept = true;
                    shifted = true;
                }
                // if starts with s, then we shift
                else if (toDo.charAt(0) == 's'){
                    // look at state to shift to and push token on stack
                    currToken.state = Integer.parseInt(toDo.substring(1));
                    currEval.push(currToken);
                    shifted = true;
                    // done with current token, increase index
                    tokenIndex++;
                }
                // if not s or a, must be an r for reduction
                else {
                    // find what rule to reduce to
                    int rule = Integer.parseInt(toDo.substring(1));
                    Token reduced;
                    // rule 1 and 2 reduction performed here
                    if (rule == 1){
                        // pop off all parts of the rule
                        Token rhs = currEval.pop();
                        Token operator = currEval.pop();
                        Token lhs = currEval.pop();
                        // check operator, add or substract the two values popped off based on it (rule 1 or 2)
                        if (operator.symbol.equals("+")){
                            reduced = new Token("E", lhs.value + rhs.value, 0);
                        }
                        else {
                            reduced = new Token("E", lhs.value - rhs.value, 0);
                        }
                    }
                    // reduce T to E
                    else if (rule == 3){
                        Token popped = currEval.pop();
                        reduced = new Token("E", popped.value, 0);
                    }
                    // rule 4 and 5 reduction performed here
                    else if (rule == 4){
                        // pop off all parts of the rule
                        Token rhs = currEval.pop();
                        Token operator = currEval.pop();
                        Token lhs = currEval.pop();
                        // check operator, multiply (rule 4) or divide (rule 5) two values based on it
                        if (operator.symbol.equals("*")){
                            reduced = new Token("T", lhs.value * rhs.value, 0);
                        }
                        else {
                            reduced = new Token("T", lhs.value / rhs.value, 0);
                        }
                    }
                    // reduce F to T
                    else if (rule == 6){
                        Token popped = currEval.pop();
                        reduced = new Token("T", popped.value, 0);
                    }
                    // reduce what is in parenthesis to F
                    else if (rule == 7){
                        currEval.pop();
                        Token popped = currEval.pop();
                        currEval.pop();
                        reduced = new Token("F", popped.value, 0);
                    }
                    // must be rule 8, reduce n to F
                    else {
                        Token popped = currEval.pop();
                        reduced = new Token("F", popped.value, 0);
                    }
                    // find what state to go to next based on previous state, and push to the stack
                    reduced.state = Integer.parseInt(parseTable[currEval.peek().state][getColumn(reduced.symbol)]);
                    currEval.push(reduced);
                }
            }
        }
        // expression if valid if loop is exited, output the value
        System.out.printf("\nValid Expression, value = %.2f\n", currEval.peek().value);

    }
}