package simplf; 

import java.util.List;
import java.util.ArrayList;

import simplf.Stmt.For;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> {
    public Environment globals = new Environment();
    private Environment environment = globals;

    Interpreter() {

    }

    public void interpret(List<Stmt> stmts) {
        try {
            for (Stmt stmt : stmts) {
                execute(stmt);
            }
        } catch (RuntimeError error) {
            Simplf.runtimeError(error);
        }
    }

    @Override
public Object visitExprStmt(Stmt.Expression stmt) {
    return evaluate(stmt.expr);  // Return the result of the evaluated expression
}


    @Override
    public Object visitPrintStmt(Stmt.Print stmt) {
        Object val = evaluate(stmt.expr);
        System.out.println(stringify(val));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        //throw new UnsupportedOperationException("TODO: implement statements");
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name, value);
        return null;
    }

    @Override
    public Object visitBlockStmt(Stmt.Block stmt) {
        //throw new UnsupportedOperationException("TODO: implement statements");
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    Object executeBlock(List<Stmt> statements, Environment newEnv) {
    Environment previous = this.environment;
    Object lastValue = null;  // Track the last evaluated expression
    try {
        this.environment = newEnv;
        for (Stmt statement : statements) {
            lastValue = execute(statement);  // Update lastValue with each statement result
        }
    } finally {
        this.environment = previous;
    }
    return lastValue;  // Return the last evaluated expression in function context
}


   

    @Override
    public Object visitIfStmt(Stmt.If stmt) {
        //throw new UnsupportedOperationException("TODO: implement statements");
        if (isTruthy(evaluate(stmt.cond))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Object visitWhileStmt(Stmt.While stmt) {
        //throw new UnsupportedOperationException("TODO: implement statements");
        while (isTruthy(evaluate(stmt.cond))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Object visitForStmt(For stmt) {
        throw new UnsupportedOperationException("For loops are not interpreted.");
    }

    @Override
    public Object visitFunctionStmt(Stmt.Function stmt) {
    SimplfFunction function = new SimplfFunction(stmt, environment);
    environment.define(stmt.name, function);
    return null;
}
public Object callFunction(Stmt.Function stmt, Environment closureEnv) {
    Environment previous = this.environment;
    this.environment = new Environment(closureEnv);  // Use the closure environment
    Object lastValue = null;  // Variable to capture the last evaluated expression

    //System.out.println("Executing function: " + stmt.name.lexeme);

    try {
        for (Stmt statement : stmt.body) {
            lastValue = execute(statement);  // Track each statement result
            //System.out.println("Executed statement result: " + lastValue);
        }
    } finally {
        this.environment = previous;  // Restore the previous environment after execution
    }

    //System.out.println("Returning from function: " + lastValue);
    return lastValue;  // Return the last evaluated expression in function body
}





    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        if (expr.op.type == TokenType.OR) {
            if (isTruthy(left))
                return left;
        } else {
            if (!isTruthy(left))
                return left;
        }
        return evaluate(expr.right);
    }

    private Double toNumber(Object operand) {
    //if (operand == null) return 0.0; // Default to 0 if null
    if (operand instanceof Double) return (Double) operand;
    if (operand instanceof String) {
        try {
            return Double.parseDouble((String) operand);
        } catch (NumberFormatException e) {
            return null; // Conversion failed
        }
    }
    return null; // Unsupported type
}


@Override
public Object visitBinary(Expr.Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right);

    switch (expr.op.type) {
        case PLUS:
            // If either operand is a string, perform string concatenation
            if (left instanceof String || right instanceof String) {
                return stringify(left) + stringify(right);
            }
            // Otherwise, try to convert to numbers and add
            Double leftNum = toNumber(left);
            Double rightNum = toNumber(right);
            if (leftNum != null && rightNum != null) {
                return leftNum + rightNum;
            }
            throw new RuntimeError(expr.op, "Operands must be numbers or strings for addition.");

        case MINUS:
            leftNum = toNumber(left);
            rightNum = toNumber(right);
            if (leftNum != null && rightNum != null) {
                return leftNum - rightNum;
            }
            throw new RuntimeError(expr.op, "Operands must be numbers for subtraction.");

        case STAR:
    leftNum = toNumber(left);
    rightNum = toNumber(right);
    if (leftNum != null && rightNum != null) {
        return leftNum * rightNum;
    }
    if (rightNum == null) {
        return leftNum * 0.0;
    }
    if (leftNum == null) {
        return 0.0 * rightNum;
    }
    System.out.println("Multiplication Error: Left operand = " + left + ", Right operand = " + right);
    throw new RuntimeError(expr.op, "Operands must be numbers for multiplication.");

        case SLASH:
            leftNum = toNumber(left);
            rightNum = toNumber(right);
            if (leftNum != null && rightNum != null) {
                if (rightNum == 0) {
                    throw new RuntimeError(expr.op, "Cannot divide by zero.");
                }
                return leftNum / rightNum;
            }
            throw new RuntimeError(expr.op, "Operands must be numbers for division.");

        case GREATER:
            leftNum = toNumber(left);
            rightNum = toNumber(right);
            if (leftNum != null && rightNum != null) {
                return leftNum > rightNum;
            }
            throw new RuntimeError(expr.op, "Operands must be numbers for comparison.");

        case GREATER_EQUAL:
            leftNum = toNumber(left);
            rightNum = toNumber(right);
            if (leftNum != null && rightNum != null) {
                return leftNum >= rightNum;
            }
            throw new RuntimeError(expr.op, "Operands must be numbers for comparison.");

        case LESS:
            leftNum = toNumber(left);
            rightNum = toNumber(right);
            if (leftNum != null && rightNum != null) {
                return leftNum < rightNum;
            }
            throw new RuntimeError(expr.op, "Operands must be numbers for comparison.");

        case LESS_EQUAL:
            leftNum = toNumber(left);
            rightNum = toNumber(right);
            if (leftNum != null && rightNum != null) {
                return leftNum <= rightNum;
            }
            throw new RuntimeError(expr.op, "Operands must be numbers for comparison.");

        case EQUAL_EQUAL:
            return isEqual(left, right);

        case BANG_EQUAL:
            return !isEqual(left, right);

        default:
            break;
    }
    return null;
}


    @Override
    public Object visitUnary(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.op.type) {
            case MINUS:
                checkNumber(expr.op, right);
                return -(double) right;
            case BANG:
                return !isTruthy(right);
            default:
                break;
        }
        return null;
    }

    @Override
    public Object visitLiteral(Expr.Literal expr) {
        return expr.val;
    }

    @Override
    public Object visitGrouping(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitVarExpr(Expr.Variable expr) {
        // Check if the variable exists in the current environment first
        Object value = environment.get(expr.name);
        if (value == null) {
            // If not found in the current scope, fall back to the global environment
            value = globals.get(expr.name);
        }
        return value;
    }

   @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);
        //System.out.println("Calling function: " + callee);

        if (!(callee instanceof SimplfCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions.");
        }

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.args) {
            arguments.add(evaluate(argument));
        }
        //System.out.println("With arguments: " + arguments);

        SimplfCallable function = (SimplfCallable) callee;
        Object result = function.call(this, arguments);
        //System.out.println("Function result: " + result);
        return result;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        //throw new UnsupportedOperationException("TODO: implement assignments");
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitConditionalExpr(Expr.Conditional expr) {
        if (isTruthy(evaluate(expr.cond))) {
            return evaluate(expr.thenBranch);
        } else {
            return evaluate(expr.elseBranch);
        }
    }

    private Object execute(Stmt stmt) {
        return stmt.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean) {
            return (boolean) object;
        }
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null)
            return b == null;
        return a.equals(b);
    }

    private void checkNumber(Token op, Object object) {
        if (object instanceof Double)
            return;
        throw new RuntimeError(op, "Operand must be a number");
    }

    private void checkNumbers(Token op, Object a, Object b) {
        if (a instanceof Double && b instanceof Double)
            return;
        throw new RuntimeError(op, "Operand must be numbers");
    }

    private String stringify(Object object) {
        if (object == null)
            return "nil";
        if (object instanceof Double) {
            String num = object.toString();
            if (num.endsWith(".0")) {
                num = num.substring(0, num.length() - 2);
            }
            return num;
        }
        return object.toString();
    }

    // Define a helper method to ensure operands are numbers.
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.op.type) {
            case STAR: // Multiplication
                checkNumberOperands(expr.op, left, right);
                return (double)left * (double)right;
            case MINUS: // Subtraction
                checkNumberOperands(expr.op, left, right);
                return (double)left - (double)right;
            case PLUS: // Addition
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                // Allow string concatenation if operands are strings
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                throw new RuntimeError(expr.op, "Operands must be two numbers or two strings.");
            case SLASH: // Division
                checkNumberOperands(expr.op, left, right);
                if ((double)right == 0) {
                    throw new RuntimeError(expr.op, "Division by zero.");
                }
                return (double)left / (double)right;
        }
        return null; // For other operators not shown in this example.
    }


}