package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.expressions.Expression;
import org.objectweb.asm.Opcodes;

public class AssignmentStatement extends Statement {
    private Expression expression;
    private String variableName;
    private CatscriptType type;
    private boolean global;

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = addChild(expression);
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public boolean isGlobal() {return getParent() instanceof CatScriptProgram;}

    @Override
    public void validate(SymbolTable symbolTable) {
        expression.validate(symbolTable);
        type = symbolTable.getSymbolType(variableName);
        global = symbolTable.isSymbolGlobal(variableName);

        CatscriptType symbolType = symbolTable.getSymbolType(getVariableName());
        if (symbolType == null) {
            addError(ErrorType.UNKNOWN_NAME);
        } else {
            // TODO - verify compatibility of types
            if(symbolType != expression.getType()) {
                this.addError(ErrorType.INCOMPATIBLE_TYPES);
            }
        }
    }

    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime) {
        //JESUS CHRIST IM SO STUPID
        Object result = expression.evaluate(runtime);
        runtime.setValue(variableName, result);
        return;
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        if(isGlobal()) {
            //a field
            //iterate through types
            String descriptor;
            if(type == CatscriptType.INT || type == CatscriptType.BOOLEAN) {
                descriptor = "I";
            }
            else if (type == CatscriptType.STRING){
                descriptor = "Ljava/lang/String;";
            }
            else if (type instanceof CatscriptType.ListType) {
                descriptor = "Ljava/util/List;";
            }
            else {
                descriptor = "Ljava/lang/Object;";
            }
            code.addVarInstruction(Opcodes.ALOAD, 0);
            expression.compile(code);
            code.addFieldInstruction(Opcodes.PUTFIELD, variableName, descriptor, code.getProgramInternalName());
        }
        else {
            // a slot
            expression.compile(code);
            if(type == CatscriptType.INT || type == CatscriptType.BOOLEAN) {
                code.addVarInstruction(Opcodes.ISTORE, code.resolveLocalStorageSlotFor(variableName));
            }
            else {
                code.addVarInstruction(Opcodes.ASTORE, code.resolveLocalStorageSlotFor(variableName));
            }
        }
    }
}
