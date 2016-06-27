package com.x5e.jpyon;

import java.io.CharArrayReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by darin on 6/26/16.
 */
public class Token {
    byte kind;
    Object value;
    int after;

    static final byte END = 0;
    static final byte QUOTED = 1;
    static final byte BAREWORD = 2;
    static final byte NUMBER = 3;

    Token(byte kind, Object value, int after) {
        this.kind = kind;
        this.value = value;
        this.after = after;
    }

    Token(byte kind) {
        this(kind,null,0);
    }

    Token(char kind) {
        this((byte) kind,null,0);
    }

    Token() {
        this((byte) 0,null,0);
    }

    Token(byte kind, Object value) {
        this(kind,value,0);
    }

    public boolean equals(Object obj) {
        return (obj != null && (obj instanceof Token) && obj.toString().equals(this.toString()));
    }

    public static Token readBareWord(byte[] input, int start, Token token) {
        return null;
    }

    public static Token readQuoted(byte[] input, int start, Token token) {
        return null;
    }

    public static Token readNumber(byte[] input, int start, Token token) {
        int last = input.length - 1;
        int sign = +1;
        if (input[start] == '+')
            start += 1;
        if (input[start] == '-') {
            start += 1;
            sign = -1;
        }
        long intPart = 0;
        while (start <= last && input[start] >= '0' && input[start] <= '9') {
            intPart *= 10;
            intPart += (input[start] - '0');
            start += 1;
        }
        token.kind = NUMBER;
        token.value = sign * intPart;
        token.after = start;
        return token;
    }

    public static int readComment(byte[] input, int start) {
        int last = input.length - 1;
        if (start == last) return last + 1;
        byte val1 = input[start];
        byte val2 = input[start+1];
        if (val1 == '/' && val2 == '*') {
            start += 2;
            while (true) {
                if (start + 1 > last) return (last+1);
                if (input[start] == '*' && input[start+1] == '/') {
                    return start + 2;
                }
                start += 1;
            }
        }
        while (true) {
            start += 1;
            if (start >= last || input[start] == '\n')
                return start + 1;
        }
    }
    public static List<Token> readMany(String input) {
        return readMany(input.getBytes());
    }

    public static List<Token> readMany(byte[] input) {
        List<Token> tokens = new ArrayList<Token>();
        int start = 0;
        while (true) {
            Token token = Token.readOne(input, start);
            tokens.add(token);
            if (token.kind == Token.END) break;
            start = token.after;
        }
        return tokens;
    }

    public static Token readOne(byte[] input, int start) {
        Token out = new Token();
        while (true) {
            if (start >= input.length) {
                out.kind = END;
                return out;
            }
            byte first = input[start];
            switch (first) {
                case 32:
                case '\n':
                case '\t':
                case '\r':
                    start += 1;
                    continue;
                case '[':
                case ']':
                case '(':
                case ')':
                case ',':
                case '{':
                case '}':
                case '=':
                case ':':
                    out.kind = first;
                    out.after = start + 1;
                    return out;
                case '/':
                case '#':
                    start = readComment(input,start);
                    continue;
                case '"':
                case '\'':
                    return readQuoted(input,start,out);
                case '+':
                case '-':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '.':
                    return readNumber(input,start,out);
                default:
                    return readBareWord(input,start,out);
            }
        }
    }

    public String toString() {
        switch(kind) {
            case END: return "End()";
            case NUMBER:
                return "Number(" + value.toString() + ")";
            case QUOTED:
                return "Quoted('" + value.toString() + "')";
            case BAREWORD:
                return "BareWord('" + value.toString() + "')";
            default:
                return "Syntax('" + Character.toString((char)kind) + "')";
        }
    }

}
