package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.eval.ReturnException;
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
        ParseElement cur = this;
        while (!(cur instanceof FunctionDefinitionStatement) && cur.getParent() != null) {
            cur = cur.getParent();
        }
        if (cur instanceof FunctionDefinitionStatement) {
            return (FunctionDefinitionStatement) cur;
        }
        else {
            return null;
        }
    }


    @Override
    public void validate(SymbolTable symbolTable) {
        if(expression != null) {
            expression.validate(symbolTable);
        }
        FunctionDefinitionStatement function = getFunctionDefinitionStatement();
        if (function == null) {
            // TODO - if there is no enclosing function add a ErrorType.INVALID_RETURN_STATEMENT error
            this.addError(ErrorType.INVALID_RETURN_STATEMENT);

        } else {
            // TODO - if there is an expression associated with this return statement
            // ensure it is compatible with function.getType() or add an ErrorType.INCOMPATIBLE_TYPE error
            CatscriptType funType = function.getType();
            CatscriptType returnType;
            if (this.expression != null) {
                returnType = this.expression.getType();
            } else {
                returnType = null;
            }
            if (returnType == null && funType != CatscriptType.VOID) {
                this.addError(ErrorType.INCOMPATIBLE_TYPES);
            }
            else if(returnType == null && funType == CatscriptType.VOID) {
                return;
            }
            else if (!(funType.isAssignableFrom(returnType))) {
                this.addError(ErrorType.INCOMPATIBLE_TYPES);
            }







        }
    }

    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime) {
        Object value = null;
        if(expression != null) {
            value = expression.evaluate(runtime);
        }
        throw new ReturnException(value);
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