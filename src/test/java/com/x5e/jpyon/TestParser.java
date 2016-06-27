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
        assertEquals(list.get(0).toString(),"hello");
        assertEquals(list.get(1).toString(),"37");
        assertEquals(list.get(2).toString(),"true");
        assertTrue(list.get(3) instanceof ArrayList);
        assertEquals(Statics.toPyon(val),input);
    }
    @Test
    public void testReadMap() throws Exception {
        String input = "{'foo':'bar','cheese':1,'fries':{}}";
        Object val = Parser.parse(input);
        assertTrue(val instanceof Map);
        Map<String,Object> map = (Map<String,Object>) val;
        assertEquals(map.get("foo").toString(),"bar");
        assertEquals(map.get("cheese").toString(),"1");
        assertEquals(Statics.toPyon(val),input);
    }
}
