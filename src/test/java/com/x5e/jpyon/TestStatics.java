package com.x5e.jpyon;

import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;

import static org.testng.Assert.*;


class A {
    public int i = 3;
    public Integer i2 = 2;
    private int j = 7;
    public String toString() {
        return "A(" + Integer.toString(i) + "," +
                Integer.toString(i2) + "," + Integer.toString(j) + ")";
    }
}

class B extends A {
    B(int k) {
        i = k;
    }
    private Integer q = null;
    public static int m = 44;
    public transient int n = -1;
}

class C {
    Set<Character> chars;
}

public class TestStatics {
    @Test
    public void testRepr() throws Exception {
        String actual = Statics.repr("hello world\tagain\n");
        String expected = "'hello world\\tagain\\n'";
        assertEquals(actual,expected);
    }

    @Test
    public void toPyobTest() throws Exception {
        A a = new A();
        Pyob b = Statics.toPyob(a);
        System.out.println(b.toString());
        assertEquals(b.toString(),"A(i=3,i2=2,j=7)");
    }

    @Test
    public void toPyobTest2() throws Exception {
        B b = new B(99);
        Pyob p = Statics.toPyob(b);
        System.out.println(p.toString());
        assertEquals(p.toString(),"B(q=null,i=99,i2=2,j=7)");
    }


    @Test
    public void toPyobTest3() throws Exception {
        Statics.register(A.class);
        Pyob pyob1 = (Pyob) Parser.parse("A(i=3,i2=2,j=7)");
        Object obj = Statics.fromPyob(pyob1);
        assertTrue(obj instanceof A);
        System.out.println(obj.toString());
    }

    @Test
    public void testCoerceTo() throws Exception {
        assertEquals(Statics.coerceTo(int.class,56),56);
        assertEquals(Statics.coerceTo(Long.class,3),3L);
        Instant instant = Instant.now();
        assertEquals(Statics.coerceTo(Instant.class,instant.toString()),instant);
        assertEquals(Statics.coerceTo(char.class,"j"),'j');
        assertEquals(Statics.coerceTo(int.class,"3"),3);
    }

    @Test
    public void testCoerceToArray() throws Exception {
        Object input = Parser.parse("['hello','world']");
        String[] out = (String[]) Statics.coerceTo(String[].class,input);
        assertEquals(out[0],"hello");
        assertEquals(out[1],"world");
    }


    @Test
    public void testCoerceToCollection() throws Exception {
        Statics.register(C.class);
        Object input = Parser.parse("C(chars=['a','b'])");
        C out = (C) Statics.coerceTo(C.class,input);
        Set<Character> s = out.chars;
        assertTrue(s.contains('a'));
        assertTrue(s.contains('b'));
    }


    @Test
    public void testCoerceArray() throws Exception {
        Statics.register(C.class);
        Object input = new String[] {"A","B"};
        char[] out =  (char[]) Statics.coerceTo(char[].class,input);
        assertEquals(out[0],'A');
        assertEquals(out[1],'B');
    }

    @Test
    public void testCast() throws Exception {
        String input = "Instant('2007-12-03T10:15:30.00Z')";
        Pyob pyob = (Pyob) Parser.parse(input);
        Object out = Statics.fromPyob(pyob);
        assertTrue(out instanceof Instant);
    }
}