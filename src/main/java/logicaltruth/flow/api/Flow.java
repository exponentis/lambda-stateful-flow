package logicaltruth.flow.api;

import java.util.function.Function;

@FunctionalInterface
public interface Flow<TState, TStep extends Enum<?>, TRoute extends Enum<?>> {
  FlowExecutionInfo<TState, TStep, TRoute> execute(TState context);

  default <UState> Flow<UState, TStep, TRoute> with(Function<UState, TState> adapter) {
    return c -> (FlowExecutionInfo<UState, TStep, TRoute>) this.execute(adapter.apply(c));
  }
}
