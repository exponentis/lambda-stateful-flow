package logicaltruth.flow;

import logicaltruth.flow.api.FlowBuilderDsl;
import logicaltruth.flow.impl.builder.FlowBuilder;

import java.util.function.Function;

public class CustomFlowBuilder {
  public enum EMPTY {}

  public static <TState, TStep extends Enum<?>> FlowBuilderDsl.AfterIn<TState, TStep, EMPTY> start(String name, TStep initialStep) {
    return FlowBuilder.start(name, initialStep);
  }

  public static <TState, TStep extends Enum<?>> FlowBuilderDsl.In<TState, TStep, EMPTY> start(String name, Function<TState, TStep> initialRouter) {
    return FlowBuilder.start(name, initialRouter);
  }
}
