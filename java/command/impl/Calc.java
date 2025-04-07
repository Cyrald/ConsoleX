package command.impl;

import java.util.List;
import java.util.Stack;

import command.Command;
import command.CommandAlias;
import command.CommandResult;

/**
 * Command to perform basic arithmetic calculations.
 */
@CommandAlias({"calc"})
public class Calc implements Command {

    @Override
    public CommandResult execute(List<String> args) {
        if (args.isEmpty()) {
            return CommandResult.error("Usage: " + getUsage());
        }
        
        // Join all arguments to get the expression
        String expression = String.join(" ", args);
        
        try {
            double result = evaluateExpression(expression);
            // Format the result to avoid trailing zeros
            String resultStr = result % 1 == 0 ? 
                    String.valueOf((int) result) : 
                    String.valueOf(result);
            
            return CommandResult.success(resultStr);
        } catch (Exception e) {
            return CommandResult.error("Error calculating: " + e.getMessage());
        }
    }
    
    /**
     * Evaluate a mathematical expression.
     * 
     * @param expression The expression to evaluate
     * @return The result of the evaluation
     */
    private double evaluateExpression(String expression) {
        // Remove spaces
        expression = expression.replaceAll("\\s+", "");
        
        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();
        
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            
            // If character is a digit or decimal point
            if (Character.isDigit(c) || c == '.') {
                StringBuilder numBuilder = new StringBuilder();
                
                // Parse the number
                while (i < expression.length() && 
                       (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    numBuilder.append(expression.charAt(i));
                    i++;
                }
                
                i--; // Adjust index
                numbers.push(Double.parseDouble(numBuilder.toString()));
            }
            // If character is an operator
            else if (c == '+' || c == '-' || c == '*' || c == '/') {
                while (!operators.isEmpty() && hasPrecedence(c, operators.peek())) {
                    numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
                }
                operators.push(c);
            }
            // If character is an opening bracket
            else if (c == '(') {
                operators.push(c);
            }
            // If character is a closing bracket
            else if (c == ')') {
                while (operators.peek() != '(') {
                    numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
                }
                operators.pop(); // Remove the '('
            }
        }
        
        // Process all remaining operators
        while (!operators.isEmpty()) {
            numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
        }
        
        // The result will be the only value in the numbers stack
        return numbers.pop();
    }
    
    /**
     * Check if op1 has lower or equal precedence than op2.
     */
    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') {
            return false;
        }
        
        return (op1 == '+' || op1 == '-') && (op2 == '*' || op2 == '/') ||
               ((op1 == '+' || op1 == '-') && (op2 == '+' || op2 == '-')) ||
               ((op1 == '*' || op1 == '/') && (op2 == '*' || op2 == '/'));
    }
    
    /**
     * Apply an operation on two numbers.
     */
    private double applyOperation(char operator, double b, double a) {
        switch (operator) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': 
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a / b;
            default: return 0;
        }
    }

    @Override
    public String getName() {
        return "calc";
    }

    @Override
    public String getDescription() {
        return "Performs basic arithmetic calculations";
    }

    @Override
    public String getUsage() {
        return "calc <expression>";
    }
}
