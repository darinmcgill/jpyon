package com.x5e.jpyon;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    public static Object parse(byte[] bytes) {
        return new Parser(bytes).readValue();
    }
    public static Object parse(String s) {
        return new Parser(s).readValue();
    }
    List<Token> tokens;
    private Parser(byte[] bytes) {
        tokens = Token.readMany(bytes);
    }
    private Parser(String s) {
        tokens = Token.readMany(s);
    }
    public Object readList() {
        List<Object> list = new ArrayList<Object>();
        while (true) {
            if (tokens.isEmpty())
                throw new RuntimeException("ran out of tokens inside list");
            Token first = tokens.get(0);
            if (first.kind == ']') {
                tokens.remove(0);
                return list;
            }
            if (first.kind == ',') {
                tokens.remove(0);
                continue;
            }
            list.add(readValue());
        }
    }
    public Object readMap() {
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        while (true) {
            if (tokens.isEmpty())
                throw new RuntimeException("ran out of tokens inside map");
            Token first = tokens.remove(0);
            if (first.kind == '}') {
                return out;
            }
            if (first.kind == ',') continue;
            String key = first.getScalar().toString();
            Token sep = tokens.remove(0);
            if (sep.kind != ':') throw new RuntimeException("bad map");
            Object value = readValue();
            out.put(key,value);
        }
    }
    public Object readPyob(Token bare) {
        Pyob out = new Pyob(bare.value.toString());
        while (true) {
            Token first = tokens.get(0);
            if (first.kind == ')') {
                tokens.remove(0);
                return out;
            }
            if (first.kind == ',') {
                tokens.remove(0);
                continue;
            }
            Token second = tokens.get(1);
            if (first.kind == Token.BAREWORD && second.kind == '=') {
                tokens.remove(0);
                tokens.remove(0);
                String key = first.value.toString();
                Object value = readValue();
                out.mapped.put(key,value);
                continue;
            }
            out.ordered.add(readValue());
        }
    }
    public Object readValue() {
        if (tokens.isEmpty())
            throw new RuntimeException("out of tokens");
        Token first = tokens.remove(0);
        if (first.kind == '[') return readList();
        if (first.kind == '{') return readMap();
        if (first.kind == Token.BAREWORD) {
            if ((!tokens.isEmpty()) && tokens.get(0).kind == '(') {
                tokens.remove(0);
                return readPyob(first);
            }
        }
        return first.getScalar();
    }
}
