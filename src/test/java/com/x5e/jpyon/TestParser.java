package com.x5e.jpyon;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.testng.Assert.*;


public class TestParser {

    @Test
    public void testReadList() throws Exception {
        String input = "['hello',37,true,[1]]";
        Object val = Parser.parse(input);
        assertTrue(val instanceof ArrayList);
        ArrayList<Object> list = (ArrayList<Object>) val;
        assertEquals(list.get(0).toString(), "hello");
        assertEquals(list.get(1).toString(), "37");
        assertEquals(list.get(2).toString(), "true");
        assertTrue(list.get(3) instanceof ArrayList);
        assertEquals(Statics.toPyon(val), input);
    }

    @Test
    public void testReadMap() throws Exception {
        String input = "{'foo':'bar','cheese':1,'fries':{}}";
        Object val = Parser.parse(input);
        assertTrue(val instanceof Map);
        Map<String, Object> map = (Map<String, Object>) val;
        assertEquals(map.get("foo").toString(), "bar");
        assertEquals(map.get("cheese").toString(), "1");
        assertEquals(Statics.toPyon(val), input);
    }

    @Test
    public void testReadPyob() throws Exception {
        String input = "cheese(null,7,foo=true,bar=snack())";
        Object val = Parser.parse(input);
        assertTrue(val instanceof Pyob);
        Pyob pyob = (Pyob) val;
        assertEquals(pyob.get(0),null);
        assertEquals(pyob.get(1).toString(),"7");
        assertEquals(pyob.get("foo").toString(),"true");
        assertTrue(pyob.get("bar") instanceof Pyob);
        assertEquals(pyob.get("bar").toString(),"snack()");

    }

    @Test
    public void testReadScalar() throws Exception {
    	String scalar_int = "42";
    	String scalar_float = "3.14";
    	String scalar_bool = "true";
    	String scalar_null = "null";
    	String scalar_string = "'derp'";
    	
        Object int_val = Parser.parse(scalar_int);
        Object float_val = Parser.parse(scalar_float);
        Object bool_val = Parser.parse(scalar_bool);
        Object null_val = Parser.parse(scalar_null);
        Object string_val = Parser.parse(scalar_string);
        
        assertEquals(int_val, 42);
        assertEquals(float_val, 3.14);
        assertEquals(bool_val, true);
        assertEquals(null_val, null);
        assertEquals(string_val, "derp");
    }
    
    @Test
    public void testAll() throws Exception {
    	String input_list = "[15,false,'derp',[1,2,3]]";
    	String input_map = "{'a':1,'b':true,'c':[1,2,3],'d':{'0':1}}";
    	String input_pyob = "RelativeValue('6A^','6C^',useMax = true)";
    	String input_scalar = "42";
    	
        Object list_val = Parser.parse(input_list);
        assertTrue(list_val instanceof ArrayList);
        ArrayList<Object> list = (ArrayList<Object>) list_val;
        assertEquals(list.get(0).toString(), "15");
        assertEquals(list.get(1).toString(), "false");
        assertEquals(list.get(2).toString(), "derp");
        assertTrue(list.get(3) instanceof ArrayList);
        assertEquals(Statics.toPyon(list_val), input_list);    	

        Object map_val = Parser.parse(input_map);
        assertTrue(map_val instanceof Map);
        Map<String, Object> map = (Map<String, Object>) map_val;
        assertEquals(map.get("a").toString(), "1");
        assertEquals(map.get("b").toString(), "true");
        assertEquals(map.get("c").toString(), "[1, 2, 3]");
        assertEquals(map.get("d").toString(), "{0=1}");
        assertEquals(Statics.toPyon(map_val), input_map);

        Object pyob_val = Parser.parse(input_pyob);
        assertTrue(pyob_val instanceof Pyob);
        Pyob pyob = (Pyob) pyob_val;
        assertEquals(pyob.get(0).toString(),"6A^");
        assertEquals(pyob.get(1).toString(),"6C^");
        assertEquals(pyob.get("useMax").toString(),"true");
        
        Object scalar_val = Parser.parse(input_scalar);
        assertEquals(scalar_val, 42);
    }
}
