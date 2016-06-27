package com.x5e.jpyon;

import java.util.LinkedList;
import java.util.List;

class Token {
    byte kind;
    Object value;
    int after;

    static final byte END = 0;
    static final byte QUOTED = 1;
    static final byte BAREWORD = 2;
    static final byte NUMBER = 3;

    Object getScalar() {
        if (kind == QUOTED || kind == NUMBER)
            return value;
        if (kind == BAREWORD) {
            String lower = value.toString().toLowerCase();
            if (lower.equals("true"))
                return Boolean.TRUE;
            if (lower.equals("false"))
                return Boolean.FALSE;
            if (lower.equals("null") || lower.equals("none"))
                return null;
        }
        throw new RuntimeException("not a scalar: " + this.toString());
    }

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

    static boolean isBarePart(int i) {
        return Character.isLetterOrDigit(i) || i == '_' || i == '.';
    }

    static boolean isBareStart(int first) {
        return (first >= 'A') || (first <= 'Z') || (first >= 'a') || (first <= 'z') || (first == '_');
    }

    static Token readBareWord(byte[] input, int start, Token token) {
        byte first = input[start];
        StringBuilder builder = new StringBuilder();
        if (isBareStart(first)) {
            builder.append((char) first);
            start += 1;
        } else {
            throw new RuntimeException("unexpected character: " + Character.toString((char) first));
        }
        int last = input.length - 1;
        while (start <= last && isBarePart(input[start])) {
            builder.append((char) input[start]);
            start += 1;
        }
        token.value = builder.toString();
        token.after = start;
        token.kind = BAREWORD;
        return token;
    }

    static int fromHex(byte b) {
        if (b >= '0' && b <= '9')
            return (b - '0');
        if (b >= 'a' && b <= 'f') {
            return (b - 'a' + 10);
        }
        if (b >= 'A' && b <= 'F') {
            return (b - 'A' + 10);
        }
        throw new RuntimeException("unexpected hex byte: " + Character.toString((char) b));
    }

    static char fromHex(byte[] input, int start, int n) {
        int out = 0;
        while (n > 0) {
            out *= 16;
            out += fromHex(input[start]);
            start += 1;
            n -= 1;
        }
        return (char) out;
    }

    static Token readQuoted(byte[] input, int start, Token token) {
        token.kind = QUOTED;
        byte quote = input[start];
        start += 1;
        int last = input.length - 1;
        StringBuilder builder = new StringBuilder();
        while (true) {
            if (start > last) {
                throw new RuntimeException("fell off end off inside string: '" + builder.toString());
            }
            byte b = input[start];
            start += 1;
            if (b == quote) {
                break;
            }
            if (b == '\\') {
                if (start > last) throw new RuntimeException("fell of inside string");
                b = input[start];
                start += 1;
                switch (b) {
                    case '\'': builder.append('\''); break;
                    case '"':  builder.append('"'); break;
                    case 'n':  builder.append('\n'); break;
                    case 't':  builder.append('\t'); break;
                    case '\\': builder.append('\\'); break;
                    case '/':  builder.append('/'); break;
                    case 'f':  builder.append('\f'); break;
                    case 'r':  builder.append('\r'); break;
                    case 'b':  builder.append('\b'); break;
                    case 'u':
                    case 'U':
                        builder.append(fromHex(input,start,4));
                        start += 4;
                        break;
                    case 'x':
                    case 'X':
                        builder.append(fromHex(input,start,2));
                        start += 2;
                        break;
                    default:
                        throw new RuntimeException("non-recognized escape:" + Character.toString((char)b));
                }
                continue;
            }
            builder.append((char) b);
        }
        token.value = builder.toString();
        token.after = start;
        return token;
    }

    static Token readNumber(byte[] input, int start, Token token) {
        token.kind = NUMBER;
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
        if (start <= last && (input[start] == '.' || input[start] == 'e' || input[start] == 'E')){
            double current = intPart;
            double place = 0.1;
            if (input[start] == '.') {
                start += 1;
                while (start <= last && input[start] >= '0' && input[start] <= '9') {
                    current += (input[start] - '0') * place;
                    place = place * 0.1;
                    start += 1;
                }
            }
            current = current * sign;
            if (start < last && (input[start] == 'e' || input[start] == 'E')) {
                start += 1;
                int eSign = 1;
                if (input[start] == '-') {
                    eSign = -1;
                    start += 1;
                } else {
                    if (input[start] == '+')
                        start += 1;
                }
                int eVal = 0;
                while (start <= last && input[start] >= '0' && input[start] <= '9') {
                    eVal *= 10;
                    eVal += input[start] - '0';
                    start += 1;
                }
                eVal *= eSign;
                current *= Math.pow(10,eVal);
            }
            token.value = current;
        } else {
            intPart = sign * intPart;
            if (intPart <= Integer.MAX_VALUE && intPart >= Integer.MIN_VALUE) {
                token.value = (int) intPart;
            } else {
                token.value = intPart;
            }
        }
        token.after = start;
        return token;
    }

    static int readComment(byte[] input, int start) {
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

    static List<Token> readMany(String input) {
        return readMany(input.getBytes());
    }

    static List<Token> readMany(byte[] input) {
        List<Token> tokens = new LinkedList<Token>();
        int start = 0;
        while (true) {
            Token token = Token.readOne(input, start);
            tokens.add(token);
            if (token.kind == Token.END) break;
            start = token.after;
        }
        return tokens;
    }

    static Token readOne(byte[] input, int start) {
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
                if (value != null) {
                    return "Quoted(" + Statics.repr(value.toString()) + ")";
                } else {
                    return "Quoted()";
                }
            case BAREWORD:
                return "BareWord('" + value.toString() + "')";
            default:
                return "Syntax('" + Character.toString((char)kind) + "')";
        }
    }

}
