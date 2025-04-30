package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.SymbolTable;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ListLiteralExpression extends Expression {
    List<Expression> values;
    private CatscriptType type;

    public ListLiteralExpression(List<Expression> values) {
        this.values = new LinkedList<>();
        for (Expression value : values) {
            this.values.add(addChild(value));
        }
    }

    public List<Expression> getValues() {
        return values;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        for (Expression value : values) {
            value.validate(symbolTable);
        }
        if (values.size() > 0) {
            // TODO - generalize this looking at all objects in list
            type = CatscriptType.getListType(values.get(0).getType());
        } else {
            type = CatscriptType.getListType(CatscriptType.OBJECT);
        }
    }

    @Override
    public CatscriptType getType() {
        return type;
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        ArrayList<Object> ret = new ArrayList<>();
        for (Expression value : this.values) {
            ret.add(value.evaluate(runtime));
        }
        return ret;
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        CatscriptType component;
        if(this.getType() instanceof CatscriptType.ListType) {
            component = ((CatscriptType.ListType) this.getType()).getComponentType();
        }
        else {
            component = this.getType();
        }
        code.addTypeInstruction(Opcodes.NEW, ByteCodeGenerator.internalNameFor(ArrayList.class));
        code.addInstruction(Opcodes.DUP);

        code.addMethodInstruction(Opcodes.INVOKESPECIAL,
                ByteCodeGenerator.internalNameFor(ArrayList.class),
                "<init>", "()V");
        for (Expression value : values) {
            code.addInstruction(Opcodes.DUP);
            value.compile(code);
            box(code, component);
            code.addMethodInstruction(Opcodes.INVOKEVIRTUAL,
                    ByteCodeGenerator.internalNameFor(ArrayList.class),
                    "add", "(Ljava/lang/Object;)Z");
            code.addInstruction(Opcodes.POP);
        }

    }


}
