package edu.montana.csci.csci468.demo;

import edu.montana.csci.csci468.CatscriptTestBase;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.expressions.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PartnerTest extends CatscriptTestBase  {

    @Test
    public void parseListLiteralExpression() {
        ListLiteralExpression expr = parseExpression("[[1, 2], 2, 3]");
        assertEquals(3, expr.getValues().size());
        ListLiteralExpression innerList = (ListLiteralExpression) expr.getValues().get(0);
        assertEquals(2, innerList.getValues().size());
    }

    @Test
    public void multiAdditiveStringCompilationWorks() {
        assertEquals("2a\n", compile("1 + 1 + \"a\""));
        assertEquals("firstsecondthird\n", compile("\"first\" + \"second\" + \"third\""));
    }

    @Test
    public void ComparisonEqualityComboCompilationWorks() {
        assertEquals("true\n", compile("1 <= 1 == 3 > 2"));
        assertEquals("false\n", compile("1 <= 1 == 2 > 3 == 1 < 8"));
    }

    @Test
    public void parseUnexpectedTokenExpression() {
        Expression expr = parseExpression("]", false);
        assertTrue(expr.hasError(ErrorType.UNEXPECTED_TOKEN));
        Expression expr2 = parseExpression(")", false);
        assertTrue(expr2.hasError(ErrorType.UNEXPECTED_TOKEN));
    }

    @Test
    public void parseDoubleParenthesizedExpressionWorks() {
        ParenthesizedExpression outer = parseExpression("((1))", false);
        assertTrue(outer instanceof ParenthesizedExpression);

        // first () is another parenthesized
        Expression middle = outer.getExpression();
        assertTrue(middle instanceof ParenthesizedExpression);

        // nested inside is the integer literal 1
        Expression innerMost = ((ParenthesizedExpression) middle).getExpression();
        assertTrue(innerMost instanceof IntegerLiteralExpression);
        assertEquals(1, ((IntegerLiteralExpression) innerMost).getValue());
    }

    @Test
    public void parseNestedListLiteralExpressionWorks() {
        ListLiteralExpression expr = parseExpression("[[1],[2]]");
        assertEquals(2, expr.getValues().size());
        assertTrue(expr.getValues().get(0) instanceof ListLiteralExpression);
        assertTrue(expr.getValues().get(1) instanceof ListLiteralExpression);
    }

    @Test
    public void parseMultiStringConcatenationExpressionWorks() {
        AdditiveExpression expr = parseExpression("\"foo\" + \"bar\" + \"again\"", false);
        assertTrue(expr.isAdd());
        assertTrue(expr.getLeftHandSide() instanceof AdditiveExpression);
        assertTrue(expr.getRightHandSide() instanceof StringLiteralExpression);
    }

}
