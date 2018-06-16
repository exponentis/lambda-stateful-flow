package logicaltruth.flow.api;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public interface FlowBuilderDsl {

  interface In<TState, TStep extends Enum<?>, TRoute extends Enum<?>> {
    AfterIn<TState, TStep, TRoute> in(TStep state);
  }

  interface Route<TState, TStep extends Enum<?>, TRoute extends Enum<?>> {
    When<TState, TStep, TRoute> choice(Function<TState, TRoute> router);
  }

  interface When<TState, TStep extends Enum<?>, TRoute extends Enum<?>> {
    AfterWhen<TState, TStep, TRoute> when(TRoute eventId);
  }

  interface Execute<TState, TStep extends Enum<?>, TRoute extends Enum<?>> {
    GoTo<TState, TStep, TRoute> execute(Consumer<TState> h);
    GoTo<TState, TStep, TRoute> flow(Flow flow);
    <I, O> GoTo<TState, TStep, TRoute> execute(Function<TState, I> pre, BiConsumer<O, TState> post, Function<I, O> h);
    <I, O> AfterAdapters<TState, TStep, TRoute> withAdapters(Function<TState, I> pre, BiConsumer<O, TState> post);
    <I> AfterExtract<TState, TStep, TRoute> extract(Function<TState, I> pre);
  }

  interface AfterExtract<TState, TStep extends Enum<?>, TRoute extends Enum<?>> {
    <I> GoTo<TState, TStep, TRoute> thenExecute(Consumer<I> h);
    <I, O> AfterExecuteFunction<TState, TStep, TRoute> thenExecute(Function<I, O> h);
  }

  interface AfterExecuteFunction<TState, TStep extends Enum<?>, TRoute extends Enum<?>> extends
    GoTo<TState, TStep, TRoute>,
    Merge<TState, TStep, TRoute> {
  }

  interface Merge<TState, TStep extends Enum<?>, TRoute extends Enum<?>> extends
    GoTo<TState, TStep, TRoute> {
    <I, O> GoTo<TState, TStep, TRoute> merge(BiConsumer<O, TState> outputAdapter);
  }

  interface AfterAdapters<TState, TStep extends Enum<?>, TRoute extends Enum<?>> {
    <I, O> GoTo<TState, TStep, TRoute> execute(Function<I, O> h);
  }

  interface GoTo<TState, TStep extends Enum<?>, TRoute extends Enum<?>> {
    AfterGoTo<TState, TStep, TRoute> next(TStep target);
  }

  interface OnError<TState, TStep extends Enum<?>, TRoute extends Enum<?>> {
    AfterOnError<TState, TStep, TRoute> onError(BiConsumer<TState, Throwable> errorHandler);
    AfterOnError<TState, TStep, TRoute> onError(Consumer<Throwable> errorHandler);
    AfterOnError<TState, TStep, TRoute> onErrorThrow();
  }

  interface Build<TState, TStep extends Enum<?>, TRoute extends Enum<?>> {
    Flow<TState, TStep, TRoute> build();
  }

  interface AfterIn<TState, TStep extends Enum<?>, TRoute extends Enum<?>> extends
    Route<TState, TStep, TRoute>,
    Execute<TState, TStep, TRoute> {
  }

  interface AfterWhen<TState, TStep extends Enum<?>, TRoute extends Enum<?>> extends
    Execute<TState, TStep, TRoute>,
    GoTo<TState, TStep, TRoute> {
  }

  interface AfterGoTo<TState, TStep extends Enum<?>, TRoute extends Enum<?>> extends
    OnError<TState, TStep, TRoute>,
    When<TState, TStep, TRoute>,
    In<TState, TStep, TRoute>,
    Build<TState, TStep, TRoute> {
  }

  interface AfterOnError<TState, TStep extends Enum<?>, TRoute extends Enum<?>> extends
    When<TState, TStep, TRoute>,
    In<TState, TStep, TRoute>,
    Build<TState, TStep, TRoute> {
  }

  interface FluentFlowBuilder<TState, TStep extends Enum<?>, TRoute extends Enum<?>> extends
    In<TState, TStep, TRoute>,
    Route<TState, TStep, TRoute>,
    When<TState, TStep, TRoute>,
    Execute<TState, TStep, TRoute>,
    AfterAdapters<TState, TStep, TRoute>,
    AfterExecuteFunction<TState, TStep, TRoute>,
    AfterExtract<TState, TStep, TRoute>,
    Merge<TState, TStep, TRoute>,
    GoTo<TState, TStep, TRoute>,
    Build<TState, TStep, TRoute>,
    AfterIn<TState, TStep, TRoute>,
    AfterWhen<TState, TStep, TRoute>,
    AfterGoTo<TState, TStep, TRoute>,
    AfterOnError<TState, TStep, TRoute> {
  }
}
