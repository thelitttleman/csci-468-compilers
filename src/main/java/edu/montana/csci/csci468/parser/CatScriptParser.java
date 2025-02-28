package edu.montana.csci.csci468.parser;

import edu.montana.csci.csci468.parser.expressions.*;
import edu.montana.csci.csci468.parser.statements.*;
import edu.montana.csci.csci468.tokenizer.CatScriptTokenizer;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenList;
import edu.montana.csci.csci468.tokenizer.TokenType;

import java.lang.reflect.Array;
import java.util.ArrayList;

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
        Statement printStmt = parsePrintStatement();
        if (printStmt != null) {
            return printStmt;
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
