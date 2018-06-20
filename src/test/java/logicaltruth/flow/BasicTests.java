package logicaltruth.flow;

import logicaltruth.flow.api.Flow;
import logicaltruth.flow.api.FlowExecutionInfo;
import logicaltruth.flow.impl.FlowExecutionException;
import org.junit.Test;

import static org.junit.Assert.*;

import logicaltruth.flow.impl.builder.FlowBuilder;

import java.util.HashMap;
import java.util.Map;

public class BasicTests {

  enum GENERIC_STEPS {
    START, A, B, C, X, Y, Z
  }

  enum EMPTY {
  }

  enum LEVELS {
    ZERO(0), ONE(1), TWO(2), THREE(3), FOUR(4);

    private Integer value;

    LEVELS(Integer i) {
      value = i;
    }

    public static LEVELS fromValue(Integer value) {
      for (LEVELS l : LEVELS.values()) {
        if (l.value.equals(value)) {
          return l;
        }
      }
      return null;
    }
  }

  static final Flow<String, GENERIC_STEPS, EMPTY> printStringLength = FlowBuilder.<String, GENERIC_STEPS, EMPTY>
    start("STRLEN", GENERIC_STEPS.A).execute(c -> System.out.println(c.length())).next(GENERIC_STEPS.B)
    .build();

  static final Flow<Integer, GENERIC_STEPS, EMPTY> printIntegerSquare = FlowBuilder.<Integer, GENERIC_STEPS, EMPTY>
    start("SQUARE", GENERIC_STEPS.A).execute(c -> System.out.println(c * c)).next(GENERIC_STEPS.B)
    .build();

  static final Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> steps = FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
    start("STEPS", GENERIC_STEPS.X).execute(c -> {
    String s = (String) c.get("input");
    c.put("output", s.length());
  }).next(GENERIC_STEPS.Y)
    .step(GENERIC_STEPS.Y).execute(c -> printIntegerSquare.execute((Integer) c.get("output"))).next(GENERIC_STEPS.Z)
    .build();

  static final Flow<String, GENERIC_STEPS, EMPTY> stateMachine = FlowBuilder.<String, GENERIC_STEPS, EMPTY>
    start("SM", state -> {
    if(state.contains("a"))
      return GENERIC_STEPS.A;
    else if(state.contains("b"))
      return GENERIC_STEPS.B;
    else return GENERIC_STEPS.C;
  }).in(GENERIC_STEPS.A).execute(state -> System.out.println("A")).next(GENERIC_STEPS.X)
    .in(GENERIC_STEPS.B).execute(state -> System.out.println("B")).next(GENERIC_STEPS.Y)
    .in(GENERIC_STEPS.C).execute(state -> System.out.println("C")).next(GENERIC_STEPS.Z)
    .build();

  static final Flow<String, GENERIC_STEPS, EMPTY> stateMachine2 = FlowBuilder.<String, GENERIC_STEPS, EMPTY>
    start("SM", GENERIC_STEPS.START).evaluateNext(state -> {
    if(state.contains("a"))
      return GENERIC_STEPS.A;
    else if(state.contains("b"))
      return GENERIC_STEPS.B;
    else return GENERIC_STEPS.C;
  }).in(GENERIC_STEPS.A).execute(state -> System.out.println("A")).next(GENERIC_STEPS.X)
    .in(GENERIC_STEPS.B).execute(state -> System.out.println("B")).next(GENERIC_STEPS.Y)
    .in(GENERIC_STEPS.C).execute(state -> System.out.println("C")).next(GENERIC_STEPS.Z)
    .build();

  static final Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> nested = FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
    start("NESTED", GENERIC_STEPS.X).execute(c -> {
    String s = (String) c.get("input");
    c.put("output", s.length());
  }).next(GENERIC_STEPS.Y)
    .step(GENERIC_STEPS.Y).flow(printIntegerSquare.<Map>with(c -> (Integer) c.get("output"))).next(GENERIC_STEPS.Z)
    .build();

  static final Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> extract = FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
    start("STEPS", GENERIC_STEPS.A)
    .extract(c -> (String) c.get("input"))
    .<String>thenExecute(s -> System.out.println(s.length())).next(GENERIC_STEPS.B)
    .build();

  static final Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> merge = FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
    start("STEPS", GENERIC_STEPS.A)
    .extract(c -> (String) c.get("input"))
    .<String, Integer>thenExecute(s -> s.length())
    .merge((i, c) -> c.put("length", i)).next(GENERIC_STEPS.B)
    .build();

  static final Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> adapters = FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
    start("STEPS", GENERIC_STEPS.A)
    .withAdapters(c -> (String) c.get("input"), (i, c) -> c.put("length", i)).<String, Integer>execute(s -> s.length()).next(GENERIC_STEPS.B)
    .build();

  static final Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> chain = FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
    start("STEPS", GENERIC_STEPS.A)
    .execute(c -> (String) c.get("input"), (i, c) -> c.put("length", i), s -> s.length()).next(GENERIC_STEPS.B)
    .build();

  static final Flow<Integer, GENERIC_STEPS, LEVELS> choice = FlowBuilder.<Integer, GENERIC_STEPS, LEVELS>
    start("CHOICE", GENERIC_STEPS.A).evaluate(i ->  LEVELS.fromValue(i % 5))
    .when(LEVELS.ZERO).execute(i -> System.out.println("0")).next(GENERIC_STEPS.X)
    .when(LEVELS.ONE).execute(i -> System.out.println("1")).next(GENERIC_STEPS.Y)
    .orElse(i -> System.out.println("2-4")).next(GENERIC_STEPS.Z)
    .build();

  static final Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> error(boolean withError1, boolean withError2) {
    return FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
      start("STEPS", GENERIC_STEPS.A).execute(c -> {
      String s = (String) c.get("input");
      if(withError1) throw new RuntimeException(("ERROR"));
      c.put("output", s.length());
    }).next(GENERIC_STEPS.B)
      .step(GENERIC_STEPS.B).execute(c -> {
        if(withError2) throw new RuntimeException(("ERROR"));
        printIntegerSquare.execute((Integer) c.get("output"));
      }).next(GENERIC_STEPS.C)
      .build();
  }

  static final Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> error_handled1(boolean withError) {
    return FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
      start("STEPS", GENERIC_STEPS.A).execute(c -> {
      String s = (String) c.get("input");
      if(withError) throw new RuntimeException(("ERROR"));
      c.put("output", s.length());
    }).next(GENERIC_STEPS.B).onError((c, t) -> c.put("output", 1000))
      .step(GENERIC_STEPS.B).execute(c -> {
        printIntegerSquare.execute((Integer) c.get("output"));
      }).next(GENERIC_STEPS.C)
      .build();
  }

  static final Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> error_handled2(boolean withError) {
    return FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
      start("STEPS", GENERIC_STEPS.A).execute(c -> {
      String s = (String) c.get("input");
      c.put("output", s.length());
    }).next(GENERIC_STEPS.B)
      .step(GENERIC_STEPS.B).execute(c -> {
        if(withError) throw new RuntimeException(("ERROR"));
        printIntegerSquare.execute((Integer) c.get("output"));
      }).next(GENERIC_STEPS.C).onError((c, t) -> System.out.println(t))
      .build();
  }

  static final Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> error_rethrown1(boolean withError) {
    return FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
      start("STEPS", GENERIC_STEPS.A).execute(c -> {
      String s = (String) c.get("input");
      c.put("output", s.length());
    }).next(GENERIC_STEPS.B)
      .step(GENERIC_STEPS.B).execute(c -> {
        if(withError) throw new RuntimeException(("ERROR"));
        printIntegerSquare.execute((Integer) c.get("output"));
      }).next(GENERIC_STEPS.C).onError((c, t) -> {
        throw new FlowExecutionException(t);
      })
      .build();
  }

  static final Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> error_rethrown2(boolean withError) {
    return FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
      start("STEPS", GENERIC_STEPS.A).execute(c -> {
      String s = (String) c.get("input");
      c.put("output", s.length());
    }).next(GENERIC_STEPS.B)
      .step(GENERIC_STEPS.B).execute(c -> {
        if(withError) throw new RuntimeException(("ERROR"));
        printIntegerSquare.execute((Integer) c.get("output"));
      }).next(GENERIC_STEPS.C).onError(t -> {
        throw new FlowExecutionException(t);
      })
      .build();
  }

  static final Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> error_rethrown3(boolean withError) {
    return FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
      start("STEPS", GENERIC_STEPS.A).execute(c -> {
      String s = (String) c.get("input");
      c.put("output", s.length());
    }).next(GENERIC_STEPS.B)
      .step(GENERIC_STEPS.B).execute(c -> {
        if(withError) throw new RuntimeException(("ERROR"));
        printIntegerSquare.execute((Integer) c.get("output"));
      }).next(GENERIC_STEPS.C).onErrorThrow()
      .build();
  }

  static final Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> nested_error(boolean withError) {
    return FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
      start("NESTED", GENERIC_STEPS.X).execute(c -> {
      String s = (String) c.get("input");
      c.put("output", s.length());
    }).next(GENERIC_STEPS.Y)
      .step(GENERIC_STEPS.Y).flow(printIntegerSquare.<Map>with(c -> {
        if(withError) throw new RuntimeException(("ERROR"));
        return ((String) c.get("input")).length();
      })).next(GENERIC_STEPS.Z)
      .build();
  }

  static final private Flow<Integer, GENERIC_STEPS, EMPTY> printIntegerSquareWithError = FlowBuilder.<Integer, GENERIC_STEPS, EMPTY>
    start("SQUARE", GENERIC_STEPS.A).execute(c -> {
    throw new RuntimeException("OOPS");
  }).next(GENERIC_STEPS.B)
    .build();

  static final Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> nested_error = FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
    start("NESTED", GENERIC_STEPS.X).execute(c -> {
    String s = (String) c.get("input");
    c.put("output", s.length());
  }).next(GENERIC_STEPS.Y)
    .step(GENERIC_STEPS.Y).flow(printIntegerSquareWithError.<Map>with(c -> ((String) c.get("input")).length()))
    .next(GENERIC_STEPS.Z)
    .build();

  static final Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> nested_error_ignored = FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
    start("NESTED", GENERIC_STEPS.X).execute(c -> {
    String s = (String) c.get("input");
    c.put("output", s.length());
  }).next(GENERIC_STEPS.Y)
    .step(GENERIC_STEPS.Y).flow(printIntegerSquareWithError.<Map>with(c -> ((String) c.get("input")).length()))
    .next(GENERIC_STEPS.Z)
    .build();

  static final Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> nested_error_handled = FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
    start("NESTED", GENERIC_STEPS.X).execute(c -> {
    String s = (String) c.get("input");
    c.put("output", s.length());
  }).next(GENERIC_STEPS.Y)
    .step(GENERIC_STEPS.Y).flow(printIntegerSquareWithError.<Map>with(c -> ((String) c.get("input")).length()))
    .next(GENERIC_STEPS.Z).onError(t -> System.out.println(t))
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
  public void test_flow_state_machine() {
    FlowExecutionInfo<String, GENERIC_STEPS, EMPTY> info = stateMachine.execute("a12");
    System.out.println(info);

    info = stateMachine.execute("b12");
    System.out.println(info);

    info = stateMachine.execute("x12");
    System.out.println(info);
  }

  @Test
  public void test_flow_state_machine2() {
    FlowExecutionInfo<String, GENERIC_STEPS, EMPTY> info = stateMachine2.execute("a12");
    System.out.println(info);

    info = stateMachine.execute("b12");
    System.out.println(info);

    info = stateMachine.execute("x12");
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


  @Test
  public void test_choice() {
    FlowExecutionInfo info = choice.execute(0);
    System.out.println(info);

    info = choice.execute(1);
    System.out.println(info);

    info = choice.execute(2);
    System.out.println(info);

    info = choice.execute(3);
    System.out.println(info);

    info = choice.execute(4);
    System.out.println(info);
  }

  @Test
  public void test_flow_error1() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = error(false, false).execute(state);
    System.out.println(info);

    info = error(true, false).execute(state);
    System.out.println(info);
  }

  @Test
  public void test_flow_error2() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = error(false, false).execute(state);
    System.out.println(info);

    info = error(false, true).execute(state);
    System.out.println(info);
  }

  @Test
  public void test_flow_error3() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = error(false, false).execute(state);
    System.out.println(info);

    info = error(true, true).execute(state);
    System.out.println(info);
  }

  @Test
  public void test_flow_error_handled1() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = error_handled1(false).execute(state);
    System.out.println(info);

    info = error_handled1(true).execute(state);
    System.out.println(info);
  }

  @Test
  public void test_flow_error_handled2() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = error_handled2(false).execute(state);
    System.out.println(info);

    info = error_handled2(true).execute(state);
    System.out.println(info);
  }

  @Test(expected = FlowExecutionException.class)
  public void test_flow_error_rethrown1() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = error_rethrown1(false).execute(state);
    System.out.println(info);

    info = error_rethrown1(true).execute(state);
    System.out.println(info);
  }

  @Test(expected = FlowExecutionException.class)
  public void test_flow_error_rethrown2() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = error_rethrown2(false).execute(state);
    System.out.println(info);

    info = error_rethrown2(true).execute(state);
    System.out.println(info);
  }

  @Test(expected = FlowExecutionException.class)
  public void test_flow_error_rethrown3() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = error_rethrown3(false).execute(state);
    System.out.println(info);

    info = error_rethrown3(true).execute(state);
    System.out.println(info);
  }

  @Test
  public void test_flow_nested_error1() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = nested_error(false).execute(state);
    System.out.println(info);

    info = nested_error(true).execute(state);
    System.out.println(info);
  }

  @Test
  public void test_flow_nested_error2() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = nested_error.execute(state);
    System.out.println(info);
  }

  @Test
  public void test_flow_nested_error_ignored() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = nested_error_ignored.execute(state);
    System.out.println(info);
  }

  @Test
  public void test_flow_nested_error_handled() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = nested_error_handled.execute(state);
    System.out.println(info);
  }
}
