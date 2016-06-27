package com.x5e.jpyon;
import org.testng.annotations.Test;

import java.util.List;
import static org.testng.Assert.*;

public class TestToken {

    @Test
    public void testOne() throws Exception {
        String input = "['hello',37]";
        byte[] bytes = input.getBytes();
        Token token = Token.readOne(bytes, 0);
        System.out.println(token.toString());
    }

    @Test
    public void testMany() throws Exception {
        String input = "[37,-3,+1,[]]";
        byte[] bytes = input.getBytes();
        List<Token> tokens = Token.readMany(bytes);
        assertEquals(tokens.get(0), new Token('['));
        assertEquals(tokens.get(1), new Token(Token.NUMBER, 37));
        assertEquals(tokens.get(2), new Token(','));
        assertEquals(tokens.get(3), new Token(Token.NUMBER, -3));
        assertEquals(tokens.get(5), new Token(Token.NUMBER, 1));
        assertEquals(tokens.get(7), new Token('['));
        assertEquals(tokens.get(8), new Token(']'));
        assertEquals(tokens.get(9), new Token(']'));
    }

    @Test
    public void testSyntax() throws Exception {
        List<Token> tokens = Token.readMany("[]{},:=()");
        assertEquals(tokens.remove(0), new Token('['));
        assertEquals(tokens.remove(0), new Token(']'));
        assertEquals(tokens.remove(0), new Token('{'));
        assertEquals(tokens.remove(0), new Token('}'));
        assertEquals(tokens.remove(0), new Token(','));
        assertEquals(tokens.remove(0), new Token(':'));
        assertEquals(tokens.remove(0), new Token('='));
        assertEquals(tokens.remove(0), new Token('('));
        assertEquals(tokens.remove(0), new Token(')'));
    }


    @Test
    public void testComments() throws Exception {
        List<Token> tokens = Token.readMany("[4#3\n,9/* foo \n */ 0 // cheese fries \n22 ");
        assertEquals(tokens.remove(0), new Token('['));
        assertEquals(tokens.remove(0), new Token(Token.NUMBER, 4));
        assertEquals(tokens.remove(0), new Token(','));
        assertEquals(tokens.remove(0), new Token(Token.NUMBER, 9));
        assertEquals(tokens.remove(0), new Token(Token.NUMBER, 0));
        assertEquals(tokens.remove(0), new Token(Token.NUMBER, 22));
        assertEquals(tokens.remove(0), new Token(Token.END));
    }


    @Test
    public void testNumbers() throws Exception {
        List<Token> tokens = Token.readMany("7 +2 -3 .2 -19.03 1.");
        for (Token token : tokens) System.out.println(token.toString());
        assertEquals(tokens.remove(0), new Token(Token.NUMBER, 7));
        assertEquals(tokens.remove(0), new Token(Token.NUMBER, 2));
        assertEquals(tokens.remove(0), new Token(Token.NUMBER, -3));
        assertEquals(tokens.remove(0), new Token(Token.NUMBER, 0.2));
        assertEquals(tokens.remove(0), new Token(Token.NUMBER, -19.03));
        assertEquals(tokens.remove(0), new Token(Token.NUMBER, 1.0));
        assertEquals(tokens.remove(0), new Token(Token.END));
    }

    @Test
    public void testExp() throws Exception {
        List<Token> tokens = Token.readMany("7e0 +2e-1 -3e2 .2E1 -19.03e+5 1.E-2");
        for (Token token : tokens) System.out.println(token.toString());
        assertEquals(tokens.remove(0), new Token(Token.NUMBER, 7.0));
        assertEquals(tokens.remove(0), new Token(Token.NUMBER, 0.2));
        assertEquals(tokens.remove(0), new Token(Token.NUMBER, -300.));
        assertEquals(tokens.remove(0), new Token(Token.NUMBER, 2.0));
        assertEquals(tokens.remove(0), new Token(Token.NUMBER, -1903000.));
        assertEquals(tokens.remove(0), new Token(Token.NUMBER, .01));
    }

    @Test
    public void testStrings() throws Exception {
        List<Token> tokens = Token.readMany("'foo bar' '' \"'\" '\\n' '\t\\t'");
        for (Token token : tokens) {
            System.out.println(token.toString());
        }
        assertEquals(tokens.remove(0), new Token(Token.QUOTED, "foo bar"));
        assertEquals(tokens.remove(0), new Token(Token.QUOTED, ""));
        assertEquals(tokens.remove(0), new Token(Token.QUOTED, "'"));
        //assertEquals(tokens.remove(0), new Token(Token.QUOTED, "\\n"));
        Token t = tokens.remove(0);
        assertEquals(t.value, "\n");
        assertEquals(tokens.remove(0), new Token(Token.QUOTED, "\t\t"));
    }

    @Test
    public void testStringHex() throws Exception {
        List<Token> tokens = Token.readMany("'\\x7aF' '\\X7A0' '\\u007a'");
        assertEquals(tokens.remove(0), new Token(Token.QUOTED, "zF"));
        assertEquals(tokens.remove(0), new Token(Token.QUOTED, "z0"));
        assertEquals(tokens.remove(0), new Token(Token.QUOTED, "z"));
    }

    @Test
    public void testBare() throws Exception {
        List<Token> tokens = Token.readMany("true True x00 _please.3(null)");
        assertEquals(tokens.remove(0), new Token(Token.BAREWORD, "true"));
        assertEquals(tokens.remove(0), new Token(Token.BAREWORD, "True"));
        assertEquals(tokens.remove(0), new Token(Token.BAREWORD, "x00"));
        assertEquals(tokens.remove(0), new Token(Token.BAREWORD, "_please.3"));
        assertEquals(tokens.remove(0), new Token('('));
        assertEquals(tokens.remove(0), new Token(Token.BAREWORD, "null"));
        assertEquals(tokens.remove(0), new Token(')'));
    }
}