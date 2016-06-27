package com.x5e.jpyon;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Created by darin on 6/27/16.
 */
public class TestStatics {
    @Test
    public void testRepr() throws Exception {
        assertEquals(Statics.repr("hello world\tagain\n"),"hello world\\tagain\\n");
    }
}
