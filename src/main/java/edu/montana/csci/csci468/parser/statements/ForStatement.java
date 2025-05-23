package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.expressions.Expression;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import java.util.LinkedList;
import java.util.List;

public class ForStatement extends Statement {
    private Expression expression;
    private String variableName;
    private List<Statement> body;

    public void setExpression(Expression expression) {
        this.expression = addChild(expression);
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setBody(List<Statement> statements) {
        this.body = new LinkedList<>();
        for (Statement statement : statements) {
            this.body.add(addChild(statement));
        }
    }

    public Expression getExpression() {
        return expression;
    }

    public String getVariableName() {
        return variableName;
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        symbolTable.pushScope();
        if (symbolTable.hasSymbol(variableName)) {
            addError(ErrorType.DUPLICATE_NAME);
        } else {
            expression.validate(symbolTable);
            CatscriptType type = expression.getType();
            if (type instanceof CatscriptType.ListType) {
                symbolTable.registerSymbol(variableName, getComponentType());
            } else {
                addError(ErrorType.INCOMPATIBLE_TYPES, getStart());
                symbolTable.registerSymbol(variableName, CatscriptType.OBJECT);
            }
        }
        for (Statement statement : body) {
            statement.validate(symbolTable);
        }
        symbolTable.popScope();
    }

    private CatscriptType getComponentType() {
        return ((CatscriptType.ListType) expression.getType()).getComponentType();
    }

    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime) {
        List eval = (List) expression.evaluate(runtime);
        runtime.pushScope();
        for (Object o : eval) {
            runtime.setValue(variableName, o);
            for (Statement statement : body) {
                statement.execute(runtime);
            }
        }
        runtime.popScope();
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {

        Integer iteratorSlot = code.nextLocalStorageSlot();
        Label iterationStart = new Label();
        Label end = new Label();

        expression.compile(code);
        //invoke virtual method iterator()
        code.addMethodInstruction(Opcodes.INVOKEINTERFACE, "java/lang/Iterable", "iterator",
                "()Ljava/util/Iterator;");

        //astore the result into the iterator slot
        code.addVarInstruction(Opcodes.ASTORE, iteratorSlot);

        code.addLabel(iterationStart); //started the loop
        //check
        code.addVarInstruction(Opcodes.ALOAD, iteratorSlot);

        //invoke the interface method hasNext() - leaves a boolean on the op stack
        code.addMethodInstruction(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");

        code.addJumpInstruction(Opcodes.IFEQ, end);

        //set up the loop variable
        code.addVarInstruction(Opcodes.ALOAD, iteratorSlot);

        //invoke the interface method next() - leaves a object on the op stack
        code.addMethodInstruction(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "next",
                "()Ljava/lang/Object;");

        CatscriptType componentType = getComponentType();
        code.addTypeInstruction(Opcodes.CHECKCAST, ByteCodeGenerator.internalNameFor(componentType.getJavaType()));
        unbox(code, componentType);

        Integer slot = code.createLocalStorageSlotFor(variableName);
        //either ISTORE or ASTORE into that slot
        if(expression.getType() instanceof CatscriptType.ListType) {
            if(((CatscriptType.ListType) expression.getType()).getComponentType() == CatscriptType.INT || ((CatscriptType.ListType) expression.getType()).getComponentType() == CatscriptType.BOOLEAN) {
                code.addVarInstruction(Opcodes.ISTORE, slot);
            }
        }
        else {
            code.addVarInstruction(Opcodes.ASTORE, slot);
        }



        //body
        for (Statement statement : body) {
            statement.compile(code);
        }
        code.addJumpInstruction(Opcodes.GOTO, iterationStart);

        //end
        code.addLabel(end);

    }

}
