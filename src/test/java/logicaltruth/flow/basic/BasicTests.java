package logicaltruth.flow.basic;

import logicaltruth.flow.api.Flow;
import logicaltruth.flow.api.FlowExecutionInfo;
import org.junit.Test;
import static org.junit.Assert.*;

import logicaltruth.flow.impl.builder.FlowBuilder;

import java.util.HashMap;
import java.util.Map;

public class BasicTests {

  enum GENERIC_STEPS {
    A, B, C, X, Y, Z
  }

  enum EMPTY {
  }

  Flow<String, GENERIC_STEPS, EMPTY> printStringLength = FlowBuilder.<String, GENERIC_STEPS, EMPTY>
    start("STRLEN", GENERIC_STEPS.A).execute(c -> System.out.println(c.length())).next(GENERIC_STEPS.B)
    .build();

  Flow<Integer, GENERIC_STEPS, EMPTY> printIntegerSquare = FlowBuilder.<Integer, GENERIC_STEPS, EMPTY>
    start("SQUARE", GENERIC_STEPS.A).execute(c -> System.out.println(c * c)).next(GENERIC_STEPS.B)
    .build();

  Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> steps = FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
    start("STEPS", GENERIC_STEPS.A).execute(c -> {
    String s = (String) c.get("input");
    c.put("output", s.length());
  }).next(GENERIC_STEPS.B)
    .in(GENERIC_STEPS.B).execute(c -> printIntegerSquare.execute((Integer) c.get("output"))).next(GENERIC_STEPS.C)
    .build();

  Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> nested = FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
    start("NESTED", GENERIC_STEPS.X).execute(c -> {
    String s = (String) c.get("input");
    c.put("output", s.length());
  }).next(GENERIC_STEPS.Y)
    .in(GENERIC_STEPS.Y).flow(printIntegerSquare.<Map>with(c -> ((String) c.get("input")).length())).next(GENERIC_STEPS.Z)
    .build();

  Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> extract = FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
    start("STEPS", GENERIC_STEPS.A)
    .extract(c -> (String) c.get("input"))
    .<String>thenExecute(s -> System.out.println(s.length())).next(GENERIC_STEPS.B)
    .build();

  Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> merge = FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
    start("STEPS", GENERIC_STEPS.A)
    .extract(c -> (String) c.get("input"))
    .<String, Integer>thenExecute(s -> s.length())
    .merge((i, c) -> c.put("length", i)).next(GENERIC_STEPS.B)
    .build();

  Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> adapters = FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
    start("STEPS", GENERIC_STEPS.A)
    .withAdapters(c -> (String) c.get("input"), (i, c) -> c.put("length", i)).<String, Integer>execute(s -> s.length()).next(GENERIC_STEPS.B)
    .build();

  Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> chain = FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
    start("STEPS", GENERIC_STEPS.A)
    .execute(c -> (String) c.get("input"), (i, c) -> c.put("length", i), s -> s.length()).next(GENERIC_STEPS.B)
    .build();

  @Test
  public void test_flow_simple() {
    printStringLength.execute("abc");
    printIntegerSquare.execute(5);
    printIntegerSquare.with((String s) -> s.length()).execute("abc");
  }

  @Test
  public void test_flow_steps() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = steps.execute(state);
    System.out.println(info);
  }

  @Test
  public void test_flow_nested() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = nested.execute(state);
    System.out.println(info);
  }

  @Test
  public void test_step_extract() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = extract.execute(state);
    System.out.println(info);
  }

  @Test
  public void test_step_merge() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "ABC");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = merge.execute(state);
    System.out.println(info);

    assertEquals(3, state.get("length"));
  }

  @Test
  public void test_step_adapters() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "ABC");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = adapters.execute(state);
    System.out.println(info);

    assertEquals(3, state.get("length"));
  }

  @Test
  public void test_step_chain() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "ABC");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = chain.execute(state);
    System.out.println(info);

    assertEquals(3, state.get("length"));
  }
}
