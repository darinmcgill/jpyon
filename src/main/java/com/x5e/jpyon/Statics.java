package com.x5e.jpyon;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by darin on 6/26/16.
 */
public class Statics {
    private static Set<Class> viaToString;
    private static Set<Class> quote;
    static {
        viaToString = new HashSet<Class>();
        viaToString.add(Double.class);
        viaToString.add(Integer.class);
        viaToString.add(Byte.class);
        viaToString.add(Float.class);
        viaToString.add(Long.class);
        quote = new HashSet<Class>();
        quote.add(String.class);
        quote.add(Character.class);
        quote.add(Instant.class);
        quote.add(Enum.class);
        quote.add(ZonedDateTime.class);
    }

    public static String toPyon(Object obj) {
        StringBuilder stringBuilder = new StringBuilder();
        toPyon(obj,stringBuilder,new HashSet<Object>());
        return stringBuilder.toString();
    }

    static void toPyon(Object obj,StringBuilder stringBuilder, Set<Object> seen) {
        if (seen.contains(obj))
            throw new RuntimeException("recursive object");
        if (obj == null) {
            stringBuilder.append("null");
            return;
        }
        Class c = obj.getClass();
        if (c == Boolean.class) {
            if (obj.equals(Boolean.TRUE)) {
                stringBuilder.append("true");
            } else {
                stringBuilder.append("false");
            }
            return;
        }
        if (viaToString.contains(c)) {
            stringBuilder.append(obj.toString());
        }
        if (quote.contains(c)) {
            //@TODO // FIXME: 6/26/16 escape special characters
            stringBuilder.append('"');
            stringBuilder.append(obj.toString());
            stringBuilder.append('"');
        }
        seen.add(obj);
        if (obj instanceof Object[]) {

        }
    }

    static String repr(String s) {
        StringBuilder builder = new StringBuilder();
        builder.append("'");
        char[] x = s.toCharArray();
        for (char c : x) {
            if (c == '\'' || c == '"') {
                builder.append('\\');
                builder.append(c);
                continue;
            }
            if (c >= 32 && c < 127) {
                builder.append(c);
            } else {
                String hex = Integer.toHexString((int) c);
                if (c <= 255) {
                    switch (c) {
                        case '\n':
                            builder.append("\\n");
                            break;
                        case '\t':
                            builder.append("\\t");
                            break;
                        case '\r':
                            builder.append("\\r");
                            break;
                        default:
                            builder.append("\\x");
                            if (hex.length() < 2) builder.append('0');
                            builder.append(hex);
                    }
                } else {
                    builder.append("\\u");
                    while (hex.length() < 4) hex = "0" + hex;
                    builder.append(hex);
                }
            }
        }
        builder.append("'");
        return builder.toString();
    }
}
