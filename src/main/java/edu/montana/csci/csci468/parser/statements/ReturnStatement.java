package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.*;
import edu.montana.csci.csci468.parser.expressions.Expression;

public class ReturnStatement extends Statement {
    private Expression expression;

    public void setExpression(Expression parseExpression) {
        this.expression = addChild(parseExpression);
    }

    public Expression getExpression() {
        return expression;
    }

    public FunctionDefinitionStatement getFunctionDefinitionStatement() {
        // TODO implement - recurse up the parent hierarchy and find a FunctionDefinitionStatement
        // use the `instanceof` operator in java
        // if there are none, return null
        return null;
    }


    @Override
    public void validate(SymbolTable symbolTable) {
        FunctionDefinitionStatement function = getFunctionDefinitionStatement();
        if (function == null) {

        } else {
            // TODO - if there is an expression associated with this return statement
            // ensure it is compatible with function.getType() or add an ErrorType.INCOMPATIBLE_TYPE error


        }
    }

    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime) {
        super.execute(runtime);
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        super.compile(code);
    }

}