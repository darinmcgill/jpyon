package com.x5e.jpyon;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            stringBuilder.append('\'');
            stringBuilder.append(obj.toString());
            stringBuilder.append('\'');
        }
        if (seen.contains(obj))
            throw new RuntimeException("recursive object");
        seen.add(obj);
        if (obj instanceof List) {
            stringBuilder.append('[');
            boolean first = true;
            List<? extends Object> list = (List<? extends Object>) obj;
            for (Object element : list) {
                if (! first) stringBuilder.append(",");
                first = false;
                toPyon(element,stringBuilder,seen);
            }
            stringBuilder.append(']');
            return;
        }
        if (obj instanceof Map) {
            stringBuilder.append('{');
            // FIXME: deal with maps of things other than <String,Object>
            Map<String,Object> map = (Map<String,Object>) obj;
            boolean seenOne = false;
            for (Map.Entry<String,Object> entry : map.entrySet()) {
                if (seenOne) stringBuilder.append(',');
                seenOne = true;
                stringBuilder.append(repr(entry.getKey()));
                stringBuilder.append(':');
                toPyon(entry.getValue(),stringBuilder,seen);
            }
            stringBuilder.append('}');
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
