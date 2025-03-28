package edu.montana.csci.csci468.parser;

import edu.montana.csci.csci468.parser.expressions.*;
import edu.montana.csci.csci468.parser.statements.*;
import edu.montana.csci.csci468.tokenizer.CatScriptTokenizer;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenList;
import edu.montana.csci.csci468.tokenizer.TokenType;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;

public class CatScriptParser {

    private TokenList tokens;

    public CatScriptProgram parse(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();

        // first parse an expression
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = null;
        try {
            expression = parseExpression();
        } catch(RuntimeException re) {
            // ignore :)
        }
        if (expression == null || tokens.hasMoreTokens()) {
            tokens.reset();
            while (tokens.hasMoreTokens()) {
                program.addStatement(parseProgramStatement());
            }
        } else {
            program.setExpression(expression);
        }

        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    public CatScriptProgram parseAsExpression(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = parseExpression();
        program.setExpression(expression);
        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    //============================================================
    //  Statements
    //============================================================

    private Statement parseProgramStatement() {
        Statement Stmt = parsePrintStatement();
        if (Stmt != null) {
            return Stmt;
        }
        Stmt = parseForStatement();
        if (Stmt != null) {
            return Stmt;
        }
        Stmt = parseIfStatement();
        if (Stmt != null) {
            return Stmt;
        }
        Stmt = parseVarStatement();
        if (Stmt != null) {
            return Stmt;
        }
        Stmt = parseAssignmentOrFunctionCallStatement();
        if (Stmt != null) {
            return Stmt;
        }
        Stmt = parseFunctionDefinitionStatement();
        if (Stmt != null) {
            return Stmt;
        }
        Stmt = parseReturnStatement();
        if (Stmt != null) {
            return Stmt;
        }
        return new SyntaxErrorStatement(tokens.consumeToken());
    }

    private Statement parsePrintStatement() {
        if (tokens.match(PRINT)) {

            PrintStatement printStatement = new PrintStatement();
            printStatement.setStart(tokens.consumeToken());

            require(LEFT_PAREN, printStatement);
            printStatement.setExpression(parseExpression());
            printStatement.setEnd(require(RIGHT_PAREN, printStatement));

            return printStatement;
        } else {
            return null;
        }
    }

    private Statement parseForStatement() {
        if (tokens.match(FOR)) {
            ForStatement forStatement = new ForStatement();
            forStatement.setStart(tokens.consumeToken());

            require(LEFT_PAREN, forStatement);

            Token variableName = require(IDENTIFIER, forStatement);
            forStatement.setVariableName(variableName.getStringValue());

            require(IN, forStatement);

            Expression expression = parseExpression();
            forStatement.setExpression(expression);

            require(RIGHT_PAREN, forStatement);

            require(LEFT_BRACE, forStatement);

            //loop for body, have to append to body field of for statement : List<Statement>
            //empty body
            if (tokens.getCurrentToken().getType() == RIGHT_BRACE) {
                forStatement.setEnd(tokens.consumeToken());
                return forStatement;
            }
            else {
                List<Statement> statements = new ArrayList<>();
                do {
                    Statement cur = parseProgramStatement();
                    statements.add(cur);

                } while (tokens.getCurrentToken().getType() != RIGHT_BRACE && tokens.getCurrentToken().getType() != EOF);
                forStatement.setBody(statements);
                forStatement.setEnd(require(RIGHT_BRACE, forStatement, ErrorType.UNEXPECTED_TOKEN));
            }
            return forStatement;
        }
        else {
            return null;
        }
    }

    private Statement parseIfStatement() {
        if (tokens.match(IF)) {
            //setup
            IfStatement ifStatement = new IfStatement();
            ifStatement.setStart(tokens.consumeToken());

            require(LEFT_PAREN, ifStatement);

            Expression expression = parseExpression();
            ifStatement.setExpression(expression);

            require(RIGHT_PAREN, ifStatement);

            require(LEFT_BRACE, ifStatement);

            //body of IF
            if (tokens.getCurrentToken().getType() == RIGHT_BRACE) {
                //cant set end because of empty if body but else exists
                tokens.consumeToken();
            }
            else {
                List<Statement> trueStatements = new ArrayList<>();
                do {
                    Statement cur = parseProgramStatement();
                    trueStatements.add(cur);


                } while (tokens.getCurrentToken().getType() != RIGHT_BRACE && tokens.getCurrentToken().getType() != EOF);
                ifStatement.setTrueStatements(trueStatements);
                ifStatement.setEnd(require(RIGHT_BRACE, ifStatement, ErrorType.UNEXPECTED_TOKEN));

            }

            //body of ELSE (recursive call for else if)
            if(tokens.getCurrentToken().getType() == ELSE) {
                tokens.consumeToken();
                require(LEFT_BRACE, ifStatement);
                if(tokens.getCurrentToken().getType() == IF) {
                    List<Statement> elseStatements = new ArrayList<>();
                    //recursive else if
                    Statement recursiveIfStatement = parseIfStatement();
                    elseStatements.add(recursiveIfStatement);
                    do {
                        Statement cur = parseProgramStatement();
                        elseStatements.add(cur);

                    } while (tokens.getCurrentToken().getType() != RIGHT_BRACE && tokens.getCurrentToken().getType() != EOF);
                    ifStatement.setElseStatements(elseStatements);
                    ifStatement.setEnd(require(RIGHT_BRACE, ifStatement, ErrorType.UNEXPECTED_TOKEN));

                }
                else {
                    List<Statement> elseStatements = new ArrayList<>();
                    if(tokens.getCurrentToken().getType() == EOF) {
                        ifStatement.setElseStatements(elseStatements);
                        ifStatement.setEnd(require(RIGHT_BRACE, ifStatement, ErrorType.UNEXPECTED_TOKEN));
                        return ifStatement;
                    }
                    do {
                        Statement cur = parseProgramStatement();
                        elseStatements.add(cur);

                    } while (tokens.getCurrentToken().getType() != RIGHT_BRACE && tokens.getCurrentToken().getType() != EOF);
                    ifStatement.setElseStatements(elseStatements);
                    ifStatement.setEnd(require(RIGHT_BRACE, ifStatement, ErrorType.UNEXPECTED_TOKEN));
                }
            }

            return ifStatement;
        }
        else {
            return null;
        }
    }

    private Statement parseVarStatement() {
        if (tokens.match(VAR)) {
            VariableStatement varStatement = new VariableStatement();
            varStatement.setStart(tokens.consumeToken());

            Token name = require(IDENTIFIER, varStatement);
            varStatement.setVariableName(name.getStringValue());

            if(tokens.getCurrentToken().getType() == COLON) {
                tokens.consumeToken();
                //parse type literal, set the explicit type field
                TypeLiteral type = parseTypeExpression();
                varStatement.setExplicitType(type.getType());


            }

            require(EQUAL, varStatement);
            Expression expression = parseExpression();
            varStatement.setExpression(expression);
            varStatement.setEnd(expression.getEnd());

            return varStatement;
        }
        else {
            return null;
        }
    }

    private Statement parseAssignmentOrFunctionCallStatement() {
        if(tokens.match(IDENTIFIER)) {
            Token start = tokens.getCurrentToken();
            tokens.consumeToken(); //wrong
            if(tokens.match(EQUAL)) {
                tokens.consumeToken();
                AssignmentStatement assignmentStatement = new AssignmentStatement();
                assignmentStatement.setStart(start);
                assignmentStatement.setVariableName(start.getStringValue());
                Expression expression = parseExpression();
                assignmentStatement.setExpression(expression);
                assignmentStatement.setEnd(expression.getEnd());
                return assignmentStatement;
            }
            else if(tokens.match(LEFT_PAREN)) {
                FunctionCallExpression fce = parseFunctionCallExpression(start);
                FunctionCallStatement functionCallStatement = new FunctionCallStatement(fce);
                return functionCallStatement;
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    private Statement parseFunctionDefinitionStatement() {
        if (tokens.match(FUNCTION)) {
            FunctionDefinitionStatement functionDefinitionStatement = new FunctionDefinitionStatement();
            functionDefinitionStatement.setStart(tokens.consumeToken());
            Token name = require(IDENTIFIER, functionDefinitionStatement);
            functionDefinitionStatement.setName(name.getStringValue());
            require(LEFT_PAREN, functionDefinitionStatement);
            //empty parameter list
            if (tokens.getCurrentToken().getType() == RIGHT_PAREN) {
                tokens.consumeToken();
            }
            else { // 1 or more params
                //flag for more params
                boolean flag = true;
                do {
                    Token argName = require(IDENTIFIER, functionDefinitionStatement);
                    if (tokens.getCurrentToken().getType() == COLON) { //explicit param type
                        tokens.consumeToken();
                        TypeLiteral type = parseTypeExpression();
                        functionDefinitionStatement.addParameter(argName.getStringValue(), type);
                    } else { //implicit param type, validate later
                        functionDefinitionStatement.addParameter(argName.getStringValue(), null);
                    }
                    if(tokens.getCurrentToken().getType() == COMMA) {
                        tokens.consumeToken();
                    }
                    else {
                        flag = false;
                    }
                } while(flag && tokens.getCurrentToken().getType() != EOF);
                require(RIGHT_PAREN, functionDefinitionStatement, ErrorType.UNTERMINATED_ARG_LIST);
            }
            //params done by this point
            if(tokens.getCurrentToken().getType() == COLON) {
                tokens.consumeToken();
                TypeLiteral type = parseTypeExpression();
                functionDefinitionStatement.setType(type);
            }
            else {
                TypeLiteral type = new TypeLiteral();
                type.setType(CatscriptType.VOID);
                functionDefinitionStatement.setType(type);
            }

            //Now need body statements
            require(LEFT_BRACE, functionDefinitionStatement);
            //empty function body
            if(tokens.getCurrentToken().getType() == RIGHT_BRACE) {
                List<Statement> statements = new ArrayList<>();
                functionDefinitionStatement.setBody(statements);
                functionDefinitionStatement.setEnd(tokens.getCurrentToken());
                tokens.consumeToken();
            }
            else { // 1 or more body statement
                List<Statement> statements = new ArrayList<>();
                do {
                    Statement statement = parseProgramStatement();
                    statements.add(statement);
                } while (tokens.getCurrentToken().getType() != RIGHT_BRACE && tokens.getCurrentToken().getType() != EOF);
                functionDefinitionStatement.setBody(statements);
                functionDefinitionStatement.setEnd(require(RIGHT_BRACE, functionDefinitionStatement, ErrorType.UNTERMINATED_ARG_LIST));
            }
            return functionDefinitionStatement;
        }
        else {
            return null;
        }
    }

    private Statement parseReturnStatement() {
        if (tokens.match(RETURN)) {
            ReturnStatement returnStatement = new ReturnStatement();
            Token start = tokens.getCurrentToken();
            tokens.consumeToken();
            returnStatement.setStart(start);
            if(tokens.getCurrentToken().getType() == RIGHT_BRACE) { //no expression
                returnStatement.setEnd(start);
            }
            else { //yes expression
                Expression expression = parseExpression();
                returnStatement.setExpression(expression);
                returnStatement.setEnd(expression.getEnd());
            }

            return returnStatement;
        }
        else {
            return null;
        }
    }


    //============================================================
    //  Expressions
    //============================================================

    private Expression parseExpression() {
        return parseEqualityExpression();
    }

    private Expression parseEqualityExpression() {
        Expression expression = parseComparisonExpression();
        while (tokens.match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseComparisonExpression();
            EqualityExpression equalityExpression = new EqualityExpression(operator, expression, rightHandSide);
            equalityExpression.setStart(expression.getStart());
            equalityExpression.setEnd(rightHandSide.getEnd());
            expression = equalityExpression;
        }
        return expression;
    }

    private Expression parseComparisonExpression() {
        Expression expression = parseAdditiveExpression();
        while (tokens.match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseAdditiveExpression();
            ComparisonExpression comparisonExpression = new ComparisonExpression(operator, expression, rightHandSide);
            comparisonExpression.setStart(expression.getStart());
            comparisonExpression.setEnd(rightHandSide.getEnd());
            expression = comparisonExpression;
        }
        return expression;
    }

    private Expression parseAdditiveExpression() {
        Expression expression = parseFactorExpression(); //fix to factor
        while (tokens.match(PLUS, MINUS)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseFactorExpression(); //fix to factor
            AdditiveExpression additiveExpression = new AdditiveExpression(operator, expression, rightHandSide);
            additiveExpression.setStart(expression.getStart());
            additiveExpression.setEnd(rightHandSide.getEnd());
            expression = additiveExpression;
        }
        return expression;
    }

    private Expression parseFactorExpression() {
        Expression expression = parseUnaryExpression(); //fix to factor
        while (tokens.match(SLASH, STAR)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseUnaryExpression(); //fix to factor
            FactorExpression factorExpression = new FactorExpression(operator, expression, rightHandSide);
            factorExpression.setStart(expression.getStart());
            factorExpression.setEnd(rightHandSide.getEnd());
            expression = factorExpression;
        }
        return expression;
    }

    private Expression parseUnaryExpression() {
        if (tokens.match(MINUS, NOT)) {
            Token token = tokens.consumeToken();
            Expression rhs = parseUnaryExpression();
            UnaryExpression unaryExpression = new UnaryExpression(token, rhs);
            unaryExpression.setStart(token);
            unaryExpression.setEnd(rhs.getEnd());
            return unaryExpression;
        } else {
            return parsePrimaryExpression();
        }
    }

    private Expression parsePrimaryExpression() {
        if (tokens.match(INTEGER)) {
            Token integerToken = tokens.consumeToken();
            IntegerLiteralExpression integerExpression = new IntegerLiteralExpression(integerToken.getStringValue());
            integerExpression.setToken(integerToken);
            return integerExpression;
        } else if (tokens.match(STRING)) {
            Token stringToken = tokens.consumeToken();
            StringLiteralExpression stringExpression = new StringLiteralExpression(stringToken.getStringValue());
            stringExpression.setToken(stringToken);
            return stringExpression;
        }
        else if (tokens.match(TRUE)) {
            Token boolToken = tokens.consumeToken();
            BooleanLiteralExpression booleanLiteralExpression = new BooleanLiteralExpression(true);
            booleanLiteralExpression.setToken(boolToken);
            return booleanLiteralExpression;
        }
        else if (tokens.match(FALSE)) {
            Token boolToken = tokens.consumeToken();
            BooleanLiteralExpression booleanLiteralExpression = new BooleanLiteralExpression(false);
            booleanLiteralExpression.setToken(boolToken);
            return booleanLiteralExpression;
        }
        else if (tokens.match(NULL)) {
            Token nullToken = tokens.consumeToken();
            NullLiteralExpression nullLiteralExpression = new NullLiteralExpression();
            nullLiteralExpression.setToken(nullToken);
            return nullLiteralExpression;
        }
        else if (tokens.match(LEFT_BRACKET)) {
            Token leftBracket = tokens.consumeToken();
            ArrayList<Expression> values = new ArrayList<>();
            if (tokens.match(RIGHT_BRACKET)) {
                Token rightBracket = tokens.consumeToken();
                ListLiteralExpression listLiteralExpression = new ListLiteralExpression(values);
                listLiteralExpression.setStart(leftBracket);
                listLiteralExpression.setEnd(rightBracket);
                return listLiteralExpression;
            }
            else {
                do {
                    Expression expr = parseExpression();
                    values.add(expr);
                } while (tokens.matchAndConsume(COMMA));
                ListLiteralExpression listLiteralExpression = new ListLiteralExpression(values);
                listLiteralExpression.setStart(leftBracket);
                if (!tokens.match(RIGHT_BRACKET)) {
                    listLiteralExpression.addError(ErrorType.UNTERMINATED_LIST);
                }
                else {
                    Token rightParen = tokens.consumeToken();
                    listLiteralExpression.setEnd(rightParen);
                }
                return listLiteralExpression;

            }

        }
        else if (tokens.match(LEFT_PAREN)) {
            Token leftParen = tokens.consumeToken();
            Expression expr = parseExpression();
            ParenthesizedExpression parenthesizedExpression = new ParenthesizedExpression(expr);
            parenthesizedExpression.setStart(leftParen);
            Token endToken = require(RIGHT_PAREN, parenthesizedExpression);
            parenthesizedExpression.setEnd(endToken);
            return parenthesizedExpression;
        }
        else if (tokens.match(IDENTIFIER)) {
            Token identifier = tokens.consumeToken();
            if (tokens.match(LEFT_PAREN)) {
                //function call
                return parseFunctionCallExpression(identifier);
            }
            else {
                //just identifier
                IdentifierExpression identifierExpression = new IdentifierExpression(identifier.getStringValue());
                identifierExpression.setToken(identifier);
                return identifierExpression;
            }

        }
        else {
            SyntaxErrorExpression syntaxErrorExpression = new SyntaxErrorExpression(tokens.consumeToken());
            return syntaxErrorExpression;
        }
    }

    private FunctionCallExpression parseFunctionCallExpression(Token identifier) {
        Token leftParen = tokens.consumeToken();
        ArrayList<Expression> argument_list = new ArrayList<>();
        if (tokens.match(RIGHT_PAREN)) { //empty args
            Token rightParen = tokens.consumeToken();
            FunctionCallExpression functionCallExpression = new FunctionCallExpression(identifier.getStringValue(), argument_list);
            functionCallExpression.setStart(identifier);
            functionCallExpression.setEnd(rightParen);
            return functionCallExpression;
        }
        else {
            do {
                Expression expr = parseExpression();
                argument_list.add(expr);
            } while (tokens.matchAndConsume(COMMA));
            FunctionCallExpression functionCallExpression = new FunctionCallExpression(identifier.getStringValue(), argument_list);
            functionCallExpression.setStart(identifier);
            if (!tokens.match(RIGHT_PAREN)) {
                functionCallExpression.addError(ErrorType.UNTERMINATED_ARG_LIST);
            }
            else {
                Token rightParen = tokens.consumeToken();
                functionCallExpression.setEnd(rightParen);
            }


            return functionCallExpression;
        }
    }

    private TypeLiteral parseTypeExpression() {
        TypeLiteral typeLiteral = new TypeLiteral();
        if(tokens.match("int")) {
            Token token = tokens.consumeToken();
            typeLiteral.setToken(token);
            typeLiteral.setType(CatscriptType.INT);
        }
        else if(tokens.match("string")) {
            Token token = tokens.consumeToken();
            typeLiteral.setToken(token);
            typeLiteral.setType(CatscriptType.STRING);
        }
        else if(tokens.match("bool")) {
            Token token = tokens.consumeToken();
            typeLiteral.setToken(token);
            typeLiteral.setType(CatscriptType.BOOLEAN);
        }
        else if(tokens.match("object")) {
            Token token = tokens.consumeToken();
            typeLiteral.setToken(token);
            typeLiteral.setType(CatscriptType.OBJECT);
        }
        else if(tokens.match("null")) {
            Token token = tokens.consumeToken();
            typeLiteral.setToken(token);
            typeLiteral.setType(CatscriptType.NULL);
        }
        else if(tokens.match("void")) {
            Token token = tokens.consumeToken();
            typeLiteral.setToken(token);
            typeLiteral.setType(CatscriptType.VOID);
        }
        else if(tokens.match("list")) {
            Token token = tokens.consumeToken();
            typeLiteral.setToken(token);
            if(tokens.match(LESS)) {
                tokens.consumeToken();
                typeLiteral.setToken(token);
                TypeLiteral typeLiteral2 = parseTypeExpression();
                require(GREATER, typeLiteral2);
                typeLiteral.setType(CatscriptType.ListType.getListType(typeLiteral2.getType()));

            }

        }
        return typeLiteral;
    }


    //============================================================
    //  Parse Helpers
    //============================================================
    private Token require(TokenType type, ParseElement elt) {
        return require(type, elt, ErrorType.UNEXPECTED_TOKEN);
    }

    private Token require(TokenType type, ParseElement elt, ErrorType msg) {
        if(tokens.match(type)){
            return tokens.consumeToken();
        } else {
            elt.addError(msg, tokens.getCurrentToken());
            return tokens.getCurrentToken();
        }
    }

}
