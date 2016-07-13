package com.x5e.jpyon;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.testng.Assert.*;
import java.util.*;

public class TestPyob {

  @Test
  public void testPyobKwargs() throws Exception {
  String input = "RandomForestRegressor";

  Map<String,Integer> map = new HashMap<String,Integer>() {{
  put("n_estimators", 100);
  put("max_depth", 3);
  put("min_samples_leaf", 500);
  }};

  Pyob pyob = new Pyob(input, map);
  String output_string = pyob.toString();

  assertTrue(output_string.contains("RandomForestRegressor"));
  //Assert containment instead of equality since parameter order changes
  assertTrue(output_string.contains("n_estimators=100"));
  assertTrue(output_string.contains("max_depth=3"));
  assertTrue(output_string.contains("min_samples_leaf=500"));
  }

  @Test
  public void testPyobArgs() throws Exception {
  String input = "RandomForestRegressor";

  Map<String,Integer> map = new HashMap<String,Integer>() {{
  put("n_estimators", 100);
  put("max_depth", 3);
  put("min_samples_leaf", 500);
  }};

  Pyob pyob = new Pyob(input, map);
  String output_string = pyob.toString();

  assertTrue(output_string.contains("RandomForestRegressor"));
  //Assert containment instead of equality since parameter order changes
  assertTrue(output_string.contains("n_estimators=100"));
  assertTrue(output_string.contains("max_depth=3"));
  assertTrue(output_string.contains("min_samples_leaf=500"));
  }

  @Test
  public void testPyobArgsAndKwargs() throws Exception {
  String input = "linspace";
  List<String> args = Arrays.asList("lower", "upper");

  Map<String,Integer> kwargs = new HashMap<String,Integer>() {{
  put("n_steps", 100);
  }};

  Pyob pyob = new Pyob(input, args, kwargs);
  String output_string = pyob.toString();

  assertEquals(output_string, "linspace('lower','upper',n_steps=100)");
  }

}