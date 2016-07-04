package com.x5e.jpyon;

import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.Instant;

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
}