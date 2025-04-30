package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.expressions.Expression;
import org.objectweb.asm.Opcodes;

public class VariableStatement extends Statement {
    private Expression expression;
    private String variableName;
    private CatscriptType explicitType;
    private CatscriptType type;

    public Expression getExpression() {
        return expression;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setExpression(Expression parseExpression) {
        this.expression = addChild(parseExpression);
    }

    public void setExplicitType(CatscriptType type) {
        this.explicitType = type;
    }

    public CatscriptType getExplicitType() {
        return explicitType;
    }

    public boolean isGlobal() {
        return getParent() instanceof CatScriptProgram;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        expression.validate(symbolTable);
        if (symbolTable.hasSymbol(variableName)) {
            addError(ErrorType.DUPLICATE_NAME);
        } else {
            // TODO if there is an explicit type, ensure it is correct
            //      if not, infer the type from the right hand side expression
            this.type = expression.getType();
            if(this.explicitType != null) {
                if(!(this.explicitType.isAssignableFrom(this.type))) {
                    this.addError(ErrorType.INCOMPATIBLE_TYPES);
                }
            }
            symbolTable.registerSymbol(variableName, type);
        }
    }

    public CatscriptType getType() {
        return type;
    }

    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime) {
        //have to mess with RUNTIME!!!!!
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
            code.addField(variableName, descriptor); //mallocs space
            code.addVarInstruction(Opcodes.ALOAD, 0);
            expression.compile(code);
            code.addFieldInstruction(Opcodes.PUTFIELD, variableName, descriptor, code.getProgramInternalName());
        }
        else {
            // a slot
            Integer slot = code.createLocalStorageSlotFor(variableName);
            expression.compile(code);
            if(type == CatscriptType.INT || type == CatscriptType.BOOLEAN) {
                code.addVarInstruction(Opcodes.ISTORE, slot);
            }
            else {
                code.addVarInstruction(Opcodes.ASTORE, slot);
            }
        }
    }
}
