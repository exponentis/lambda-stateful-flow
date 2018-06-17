package logicaltruth.flow.basic;

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

  Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> error(boolean withError1, boolean withError2) {
    return FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
      start("STEPS", GENERIC_STEPS.A).execute(c -> {
      String s = (String) c.get("input");
      if(withError1) throw new RuntimeException(("ERROR"));
      c.put("output", s.length());
    }).next(GENERIC_STEPS.B)
      .in(GENERIC_STEPS.B).execute(c -> {
        if(withError2) throw new RuntimeException(("ERROR"));
        printIntegerSquare.execute((Integer) c.get("output"));
      }).next(GENERIC_STEPS.C)
      .build();
  }

  Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> error_handled1(boolean withError) {
    return FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
      start("STEPS", GENERIC_STEPS.A).execute(c -> {
      String s = (String) c.get("input");
      if(withError) throw new RuntimeException(("ERROR"));
      c.put("output", s.length());
    }).next(GENERIC_STEPS.B).onError((c, t) -> c.put("output", 1000))
      .in(GENERIC_STEPS.B).execute(c -> {
        printIntegerSquare.execute((Integer) c.get("output"));
      }).next(GENERIC_STEPS.C)
      .build();
  }

  Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> error_handled2(boolean withError) {
    return FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
      start("STEPS", GENERIC_STEPS.A).execute(c -> {
      String s = (String) c.get("input");
      c.put("output", s.length());
    }).next(GENERIC_STEPS.B)
      .in(GENERIC_STEPS.B).execute(c -> {
        if(withError) throw new RuntimeException(("ERROR"));
        printIntegerSquare.execute((Integer) c.get("output"));
      }).next(GENERIC_STEPS.C).onError((c, t) -> System.out.println(t))
      .build();
  }

  Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> error_rethrown1(boolean withError) {
    return FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
      start("STEPS", GENERIC_STEPS.A).execute(c -> {
      String s = (String) c.get("input");
      c.put("output", s.length());
    }).next(GENERIC_STEPS.B)
      .in(GENERIC_STEPS.B).execute(c -> {
        if(withError) throw new RuntimeException(("ERROR"));
        printIntegerSquare.execute((Integer) c.get("output"));
      }).next(GENERIC_STEPS.C).onError((c, t) -> { throw new FlowExecutionException(t); })
      .build();
  }

  Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> error_rethrown2(boolean withError) {
    return FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
      start("STEPS", GENERIC_STEPS.A).execute(c -> {
      String s = (String) c.get("input");
      c.put("output", s.length());
    }).next(GENERIC_STEPS.B)
      .in(GENERIC_STEPS.B).execute(c -> {
        if(withError) throw new RuntimeException(("ERROR"));
        printIntegerSquare.execute((Integer) c.get("output"));
      }).next(GENERIC_STEPS.C).onError(t -> { throw new FlowExecutionException(t); })
      .build();
  }

  Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> error_ignored(boolean withError) {
    return FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
      start("STEPS", GENERIC_STEPS.A).execute(c -> {
      String s = (String) c.get("input");
      c.put("output", s.length());
    }).next(GENERIC_STEPS.B)
      .in(GENERIC_STEPS.B).execute(c -> {
        if(withError) throw new RuntimeException(("ERROR"));
        printIntegerSquare.execute((Integer) c.get("output"));
      }).next(GENERIC_STEPS.C).onErrorIgnore()
      .build();
  }

  Flow<Map<String, Object>, GENERIC_STEPS, EMPTY> nested_error(boolean withError) {
    return FlowBuilder.<Map<String, Object>, GENERIC_STEPS, EMPTY>
      start("NESTED", GENERIC_STEPS.X).execute(c -> {
      String s = (String) c.get("input");
      c.put("output", s.length());
    }).next(GENERIC_STEPS.Y)
      .in(GENERIC_STEPS.Y).flow(printIntegerSquare.<Map>with(c -> {
        if(withError) throw new RuntimeException(("ERROR"));
        return ((String) c.get("input")).length();
      })).next(GENERIC_STEPS.Z).onError(t -> System.out.println(t))
      .build();
  }

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

  @Test(expected = FlowExecutionException.class)
  public void test_flow_error1() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = error(false, false).execute(state);
    System.out.println(info);

    info = error(true, false).execute(state);
    System.out.println(info);
  }

  @Test(expected = FlowExecutionException.class)
  public void test_flow_error2() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = error(false, false).execute(state);
    System.out.println(info);

    info = error(false, true).execute(state);
    System.out.println(info);
  }

  @Test(expected = FlowExecutionException.class)
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

  @Test
  public void test_flow_error_ignored() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = error_ignored(false).execute(state);
    System.out.println(info);

    info = error_ignored(true).execute(state);
    System.out.println(info);
  }

  @Test
  public void test_flow_nested_error() {
    Map<String, Object> state = new HashMap<String, Object>() {{
      put("input", "Hello world");
    }};

    FlowExecutionInfo<Map<String, Object>, GENERIC_STEPS, EMPTY> info = nested_error(false).execute(state);
    System.out.println(info);

    info = nested_error(true).execute(state);
    System.out.println(info);
  }
}
