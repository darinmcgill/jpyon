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
}
