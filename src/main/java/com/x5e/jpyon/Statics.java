package com.x5e.jpyon;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.*;
import java.time.*;
import java.util.*;

public class Statics {
    private static Set<Class> viaToString = new HashSet<Class>();
    private static Set<Class> quote =  new HashSet<Class>();
    private static Map<String,Set<Class>> registered = new HashMap<String, Set<Class>>();
    private static Objenesis objenesis = new ObjenesisStd();
    private static Map<Class,Class> primitives = new HashMap<>();
    static {
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
        primitives.put(int.class,Integer.class);
        primitives.put(long.class,Long.class);
        primitives.put(double.class,Double.class);
        primitives.put(float.class,Float.class);
        primitives.put(char.class,Character.class);
        register(Instant.class);
        register(ZonedDateTime.class);
        register(Character.class);
        register(Duration.class);
        register(LocalDate.class);
        register(LocalTime.class);
        register(LocalDateTime.class);
        register(Period.class);
        register(ZoneId.class);
        register(OffsetDateTime.class);
        register(OffsetTime.class);
    }

    public static void register(Class c) {
        String simple = c.getSimpleName();
        Set<Class> classes = registered.computeIfAbsent(simple,k -> new HashSet<>());
        classes.add(c);
    }

    public static void registerAll() {
        try {
            Field f = ClassLoader.class.getDeclaredField("classes");
            f.setAccessible(true);
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            @SuppressWarnings("unchecked")
            Vector<Class> classes1 = (Vector<Class>) f.get(classLoader);
            Object[] classes = classes1.toArray();
            for (Object c : classes) {
                register((Class) c);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void clear() {
        registered.clear();
    }

    public static Class getClass(String name) throws ClassNotFoundException {
        Set<Class> classes = registered.get(name);
        if (classes != null) {
            int size = classes.size();
            assert size >= 1;
            if (size == 1) {
                return classes.iterator().next();
            } else {
                Iterator<Class> it = classes.iterator();
                String error = "multiple classes match " + name;
                error = error + " " + it.next().toString();
                error = error + " " + it.next().toString();
                throw new RuntimeException(error);
            }
        }
        return Class.forName(name);
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
            return;
        }
        if (quote.contains(c)) {
            stringBuilder.append(repr(obj.toString()));
            return;
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
            return;
        }
        if (obj instanceof Pyob) {
            Pyob pyob = (Pyob) obj;
            stringBuilder.append(pyob.kind);
            stringBuilder.append('(');
            boolean seenOne = false;
            for (Object o : pyob.ordered) {
                if (seenOne) stringBuilder.append(',');
                seenOne = true;
                toPyon(o,stringBuilder,seen);
            }
            for (Map.Entry entry : pyob.mapped.entrySet()) {
                if (seenOne) stringBuilder.append(',');
                seenOne = true;
                stringBuilder.append(entry.getKey().toString());
                stringBuilder.append('=');
                toPyon(entry.getValue(),stringBuilder,seen);
            }
            stringBuilder.append(')');
            return;
        }
        throw new RuntimeException("don't know how to convert to Pyon:" + obj.getClass().toString());
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

    static Pyob toPyob(Object obj){
        Class aClass = obj.getClass();
        Pyob out = new Pyob(aClass.getSimpleName());
        while (aClass != null) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isTransient(field.getModifiers())) continue;
                if (Modifier.isStatic(field.getModifiers())) continue;
                field.setAccessible(true);
                String name = field.getName();
                if (name.equals("$assertionsDisabled")) continue;
                try {
                    out.mapped.put(name, field.get(obj));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            aClass = aClass.getSuperclass();
        }
        return out;
    }


    static Object coerceTo(Type type, Object obj) {
        Class target = null;
        Type[] args = null;
        if (type instanceof Class) {
            target = (Class) type;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            target = (Class) pType.getRawType();
            args = pType.getActualTypeArguments();
        }
        if (target == null) throw new RuntimeException("unexpected type");
        if (obj instanceof Pyob) {
            try {
                obj = fromPyob((Pyob) obj);
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        Class objClass = obj.getClass();
        if (target.isPrimitive()) {
            target = primitives.get(target);
        }
        if (objClass.equals(target)) return obj;
        if (target.isAssignableFrom(objClass)) {
            return obj;
        }
        if (Number.class.isAssignableFrom(target)) {
            if (obj instanceof Number) {
                Number num = (Number) obj;
                if (target.equals(Long.class)) return num.longValue();
                if (target.equals(Integer.class)) return num.intValue();
                if (target.equals(Double.class)) return num.doubleValue();
                if (target.equals(Float.class)) return num.floatValue();
                if (target.equals(Short.class)) return num.shortValue();
                if (target.equals(Byte.class)) return num.byteValue();
            }
        }
        if (obj instanceof String) {
            if (target.equals(Character.class)) return obj.toString().charAt(0);
            try {
                Method method = target.getDeclaredMethod("parse",CharSequence.class);
                return method.invoke(target,obj.toString());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {

            }
            try {
                Method method = target.getDeclaredMethod("valueOf",String.class);
                return method.invoke(target,obj.toString());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {

            }
        }
        if (target.isArray()){
            Class componentType = target.getComponentType();
            if (objClass.isArray()) {
                int length = Array.getLength(obj);
                Object out = Array.newInstance(componentType, length);
                for (int i=0;i<length;i++) {
                    Array.set(out,i,coerceTo(componentType,Array.get(obj,i)));
                }
                return out;
            }
            if (obj instanceof Collection) {
                Collection<? extends Object> collection = (Collection<? extends Object>) obj;
                Object out = Array.newInstance(componentType, collection.size());
                Iterator<? extends Object> it = collection.iterator();
                for (int i=0;it.hasNext();i++) {
                    Array.set(out,i,coerceTo(componentType,it.next()));
                }
                return out;
            }
        }

        if (Collection.class.isAssignableFrom(target)) {
            while (target.isInterface()) {
                if (target.equals(Set.class)) {
                    target = LinkedHashSet.class;
                    break;
                }
                if (target.equals(List.class) || target.equals(RandomAccess.class)) {
                    target = ArrayList.class;
                    break;
                }
                if (target.equals(Deque.class) || target.equals(Queue.class)) {
                    target = LinkedList.class;
                    break;
                }
                if (target.equals(SortedSet.class) || target.equals(NavigableSet.class)) {
                    target = TreeSet.class;
                    break;
                }
                if (target.equals(Collection.class)) {
                    target = ArrayList.class;
                    break;
                }
                throw new RuntimeException("don't know how to instantiate: " + target.toString());
            }
            try {
                Collection outCol = (Collection) target.newInstance();
                if (args != null && args.length == 1) {
                    if (obj instanceof Collection) {
                        Collection srcCol = (Collection) obj;
                        for (Object thing : srcCol) {
                            outCol.add(coerceTo(args[0],thing));
                        }
                        return outCol;
                    }
                }
            } catch (IllegalAccessException | InstantiationException e) {

            }
        }


        throw new RuntimeException("don't know how to coerce " +
                objClass.toString() + " to " + target.toString());
    }

    static Object fromPyob(Pyob pyob)
            throws ClassNotFoundException,NoSuchFieldException,IllegalAccessException
    {
        String name = pyob.kind;
        Class c = getClass(name);
        if (pyob.mapped.isEmpty() && pyob.ordered.size() == 1) {
            return coerceTo(c,pyob.ordered.get(0));
        }
        Object out = objenesis.newInstance(c);
        for (Map.Entry<String,Object> entry : pyob.mapped.entrySet()) {
            String key = entry.getKey();
            Field field = c.getDeclaredField(key);
            field.setAccessible(true);
            Object value = coerceTo(field.getGenericType(),entry.getValue());
            field.set(out,value);
        }
        return out;
    }
}
