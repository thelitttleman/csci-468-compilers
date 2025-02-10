package edu.montana.csci.csci468.tokenizer;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;

public class CatScriptTokenizer {

    TokenList tokenList;
    String src;
    int postion = 0;
    int line = 1;
    int lineOffset = 0;

    public CatScriptTokenizer(String source) {
        src = source;
        tokenList = new TokenList(this);
        tokenize();
    }

    private void tokenize() {
        consumeWhitespace();
        while (!tokenizationEnd()) {
            scanToken();
            consumeWhitespace();
        }
        tokenList.addToken(EOF, "<EOF>", postion, postion, line, lineOffset);
    }

    private void scanToken() {
        if(scanNumber()) {
            return;
        }
        if(scanString()) {
            return;
        }
        if(scanIdentifier()) {
            return;
        }
        scanSyntax();
    }

    private boolean scanString() {
        // TODO implement string scanning here!
        //if backslash, consume and insert next char
        if(peek() == '"') {
            int curline = line;
            takeChar();
            int start = postion;

            while((peek() != '"' && postion != src.length())) {
                if(peek() == '\\') {
                    if(postion == src.length() - 1) {
                        takeChar();
                        tokenList.addToken(ERROR, "", start, postion, line, lineOffset);
                        return true;
                    }
                    StringBuilder sb = new StringBuilder(src);
                    sb.deleteCharAt(postion);
                    src = sb.toString();
                    //this is problem because of how the tokens are made (from start to postion), needs fixing or token add rework
                    //If splicing, eliminate this index
                }
                    takeChar();
                    lineOffset++;
            }
            if (postion == src.length()) {
                tokenList.addToken(ERROR, "", start, postion, line, lineOffset);
            }
            else {
                String value = src.substring(start, postion);
                tokenList.addToken(STRING, value, start, postion, curline, lineOffset);
                takeChar();
            }
            return true;
        }

        return false;
    }

    private boolean scanIdentifier() {
        if( isAlpha(peek())) {
            int start = postion;
            while (isAlphaNumeric(peek())) {
                lineOffset++;
                takeChar();
            }
            String value = src.substring(start, postion);
            if (KEYWORDS.containsKey(value)) {
                tokenList.addToken(KEYWORDS.get(value), value, start, postion, line, lineOffset);
            } else {
                tokenList.addToken(IDENTIFIER, value, start, postion, line, lineOffset);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean scanNumber() {
        if(isDigit(peek())) {
            int start = postion;
            while (isDigit(peek())) {
                lineOffset++;
                takeChar();
            }
            tokenList.addToken(INTEGER, src.substring(start, postion), start, postion, line, lineOffset);
            return true;
        } else {
            return false;
        }
    }

    private void scanSyntax() {
        // TODO - implement rest of syntax scanning
        //      - implement comments
        int start = postion;
        if(matchAndConsume('+')) {
            lineOffset++;
            tokenList.addToken(PLUS, "+", start, postion, line, lineOffset);
        } else if(matchAndConsume('-')) {
            lineOffset++;
            tokenList.addToken(MINUS, "-", start, postion, line, lineOffset);
        } else if(matchAndConsume('/')) {
            if (matchAndConsume('/')) { //comments are ignored
                while (peek() != '\n' && !tokenizationEnd()) {
                    takeChar();
                }
            } else {
                lineOffset++;
                tokenList.addToken(SLASH, "-", start, postion, line, lineOffset);
            }
        } else if(matchAndConsume('=')) {
            if (matchAndConsume('=')) {
                lineOffset+=2;
                tokenList.addToken(EQUAL_EQUAL, "==", start, postion, line, lineOffset);
            } else {
                lineOffset++;
                tokenList.addToken(EQUAL, "=", start, postion, line, lineOffset);
            }
        } //add rest here
        else if(matchAndConsume('(')) {
            lineOffset++;
            tokenList.addToken(LEFT_PAREN, "-", start, postion, line, lineOffset);
        }
        else if(matchAndConsume(')')) {
            lineOffset++;
            tokenList.addToken(RIGHT_PAREN, "-", start, postion, line, lineOffset);
        }
        else if(matchAndConsume('{')) {
            lineOffset++;
            tokenList.addToken(LEFT_BRACE, "-", start, postion, line, lineOffset);
        }
        else if(matchAndConsume('}')) {
            lineOffset++;
            tokenList.addToken(RIGHT_BRACE, "-", start, postion, line, lineOffset);
        }
        else if(matchAndConsume('[')) {
            lineOffset++;
            tokenList.addToken(LEFT_BRACKET, "-", start, postion, line, lineOffset);
        }
        else if(matchAndConsume(']')) {
            lineOffset++;
            tokenList.addToken(RIGHT_BRACKET, "-", start, postion, line, lineOffset);
        }
        else if(matchAndConsume(':')) {
            lineOffset++;
            tokenList.addToken(COLON, "-", start, postion, line, lineOffset);
        }
        else if(matchAndConsume(',')) {
            lineOffset++;
            tokenList.addToken(COMMA, "-", start, postion, line, lineOffset);
        }
        else if(matchAndConsume('.')) {
            lineOffset++;
            tokenList.addToken(DOT, "-", start, postion, line, lineOffset);
        }
        else if(matchAndConsume('*')) {
            lineOffset++;
            tokenList.addToken(STAR, "-", start, postion, line, lineOffset);
        }
        else if(matchAndConsume('!')) {
            if (matchAndConsume('=')) {
                lineOffset += 2;
                tokenList.addToken(BANG_EQUAL, "==", start, postion, line, lineOffset);
            }
        }
        else if(matchAndConsume('>')) {
            if (matchAndConsume('=')) {
                lineOffset+=2;
                tokenList.addToken(GREATER_EQUAL, "==", start, postion, line, lineOffset);
            } else {
                lineOffset++;
                tokenList.addToken(GREATER, "=", start, postion, line, lineOffset);
            }
        }
        else if(matchAndConsume('<')) {
            if (matchAndConsume('=')) {
                lineOffset+=2;
                tokenList.addToken(LESS_EQUAL, "==", start, postion, line, lineOffset);
            } else {
                lineOffset++;
                tokenList.addToken(LESS, "=", start, postion, line, lineOffset);
            }
        }
        else {
            tokenList.addToken(ERROR, "<Unexpected Token: [" + takeChar() + "]>", start, postion, line, lineOffset);
        }
    }

    private void consumeWhitespace() {
        // TODO update line and lineOffsets
        while (!tokenizationEnd()) {
            char c = peek();
            if (c == ' ' || c == '\r' || c == '\t') {
                lineOffset++;
                postion++;
                continue;
            } else if (c == '\n') {
                postion++;
                line++;
                lineOffset = 0;
                continue;
            }
            break;
        }
    }

    //===============================================================
    // Utility functions
    //===============================================================

    private char peek() {
        if (tokenizationEnd()) return '\0';
        return src.charAt(postion);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private char takeChar() {
        char c = src.charAt(postion);
        postion++;
        return c;
    }

    private boolean tokenizationEnd() {
        return postion >= src.length();
    }

    public boolean matchAndConsume(char c) {
        if (peek() == c) {
            takeChar();
            return true;
        }
        return false;
    }

    public TokenList getTokens() {
        return tokenList;
    }

    @Override
    public String toString() {
        if (tokenizationEnd()) {
            return src + "-->[]<--";
        } else {
            return src.substring(0, postion) + "-->[" + peek() + "]<--" +
                    ((postion == src.length() - 1) ? "" :
                            src.substring(postion + 1, src.length() - 1));
        }
    }
}