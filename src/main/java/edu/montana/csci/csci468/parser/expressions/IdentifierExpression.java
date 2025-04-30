package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import org.objectweb.asm.Opcodes;

public class IdentifierExpression extends Expression {
    private final String name;
    private CatscriptType type;

    public IdentifierExpression(String value) {
        this.name = value;
    }

    public String getName() {
        return name;
    }

    @Override
    public CatscriptType getType() {
        return type;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        CatscriptType type = symbolTable.getSymbolType(getName());
        if (type == null) {
            addError(ErrorType.UNKNOWN_NAME);
        } else {
            this.type = type;
        }
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        return runtime.getValue(this.name);
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        Integer i = code.resolveLocalStorageSlotFor(name);
        if (i != null) {
            //local
            if(this.type == CatscriptType.INT || this.type == CatscriptType.BOOLEAN) {
                code.addVarInstruction(Opcodes.ILOAD, i);
            }
            else {
                code.addVarInstruction(Opcodes.ALOAD, i);
            }
        }
        else {
            //global
            code.addVarInstruction(Opcodes.ALOAD, 0);
            //help session 2 - need to have different getFields for each catscript type
            if(this.type == CatscriptType.INT || this.type == CatscriptType.BOOLEAN) {
                code.addFieldInstruction(Opcodes.GETFIELD, name, "I", code.getProgramInternalName());
            }
            else if(this.type == CatscriptType.STRING) {
                code.addFieldInstruction(Opcodes.GETFIELD, name, "Ljava/lang/String;", code.getProgramInternalName());
            }
            else if(this.type instanceof CatscriptType.ListType) {
                code.addFieldInstruction(Opcodes.GETFIELD, name, "Ljava/util/List;", code.getProgramInternalName());
            }
            else {
                code.addFieldInstruction(Opcodes.GETFIELD, name, "Ljava/lang/Object;", code.getProgramInternalName());
            }


        }


    }


}
