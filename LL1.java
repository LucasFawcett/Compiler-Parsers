import java.util.*;

public class LL1 {
    // List to store the tokens to check
    private List<String> tokens;
    // index of the current token we are looking at
    private int currToken;

    // Constructor that takes the expression, tokenizes it, and adds it to the list
    public LL1 (String string){
        this.tokens = new ArrayList<String>();
        
        StringTokenizer tokenizer = new StringTokenizer(string, "()+-*/$", true);
        while (tokenizer.hasMoreTokens()){
            String token = tokenizer.nextToken().trim();
            if (!token.isEmpty()){
                tokens.add(token);
            }
        }
        this.currToken = 0;
    }

    // return the current token
    private String getToken(){
        if (currToken < tokens.size()){
            return tokens.get(currToken);
        }
        return "";
    }

    // E rule. Calulates T and then passes it to Eprime, which we return the value of
    public double E() throws Exception{
        double value = T();
        return EPrime(value);
    }

    // EPrime rule. Takes previous calculations, checks it operator, calculates the next calls, and calculates the total
    public double EPrime(double previous) throws Exception{
        String symbol = getToken();
        // check symbol, add next values if +. subtract if -
        if (symbol.equals("+")){
            currToken++;
            double value = T();
            return EPrime(previous + value);
        }
        else if(symbol.equals("-")){
            currToken++;
            double value = T();
            return EPrime(previous - value);
        }

        return previous;
    }

    // T rule. Must go to F first and calculate, then Tprime
    public double T() throws Exception{
        double value = F();
        return TPrime(value);
    }

    // TPrime rule. Takes previous calculations, checks operator (which TPrime rule), calculates the next calls, and calculates total
    public double TPrime(double previous) throws Exception{
        // Check next token. multiple or divide depending
        String symbol = getToken();
        if (symbol.equals("*")){
            currToken++;
            double value = F();
            return TPrime(previous * value);
        }
        else if (symbol.equals("/")){
            currToken++;
            double value = F();
            return TPrime(previous / value);
        }

        return previous;
    }

    // F rule. will only return a number or what is calculated in its parenthesis rule
    public double F() throws Exception{
        String symbol = getToken();
        if (symbol.equals("(")){
            currToken++;
            double value = E();
            if (!getToken().equals(")")){
                throw new Exception("Illegal String.");
            }
            currToken++;
            return value;
        }
        try {
            double num = Double.parseDouble(symbol);
            currToken++;
            return num;
        } catch (Exception e){
            throw new Exception("Illegal String.");
        }
    }

    // parse and output the result
    public void parseExpression() throws Exception{
        try {
            double result = E();
            // token should be $ at the end after calculating. Valid expression if it is, invalid if not
            if (getToken().equals("$")){
                System.out.println("Valid: " + result);
            }
            else {
                System.out.println("Expression was invalid.");
                return;
            }
        } catch (Exception e){
            System.out.println("Expression was invalid.");
        }
    }

    public static void main(String[] args){
        if (args.length == 0){
            System.out.println("Invalid execution. Please include the expression.");
            return;
        }
        LL1 parser = new LL1(args[0] + "$");
        
        try {
            parser.parseExpression();
        } catch (Exception e) {
            System.out.println("Expression was invalid.");
        }
    }

}