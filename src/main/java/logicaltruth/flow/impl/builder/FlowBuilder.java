package logicaltruth.flow.impl.builder;

import logicaltruth.flow.api.Flow;
import logicaltruth.flow.api.FlowBuilderDsl;
import logicaltruth.flow.impl.SimpleFlow;
import logicaltruth.flow.impl.SimpleFlowStep;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class FlowBuilder<TState, TStep extends Enum<?>, TRoute extends Enum<?>> implements FlowBuilderDsl.FluentFlowBuilder<TState, TStep, TRoute> {

  //builder outcome
  private final String name;
  private final TStep initialState;
  private final Map<TStep, SimpleFlowStep> steps = new HashMap<>();

  //temp state
  private TStep currentStep;
  private TRoute currentRoute;

  private Function<TState, ?> inputAdapter;
  private Function<?, ?> functionHandler;
  private BiConsumer<?, TState> outputAdapter;

  private FlowBuilder(final String name, final TStep initialState) {
    this.name = name;
    this.initialState = initialState;
  }

  public static <TState, TStep extends Enum<?>, TRoute extends Enum<?>> FlowBuilderDsl.AfterIn<TState, TStep, TRoute> start(String name, TStep initialState) {
    FlowBuilder<TState, TStep, TRoute> flowBuilder = new FlowBuilder<TState, TStep, TRoute>(name, initialState);
    flowBuilder.in(initialState);
    return flowBuilder;
  }

  public static <TState, TStep extends Enum<?>, TRoute extends Enum<?>> FlowBuilderDsl.AfterIn<TState, TStep, TRoute> start(TStep initialState) {
    FlowBuilder<TState, TStep, TRoute> flowBuilder = new FlowBuilder<TState, TStep, TRoute>("", initialState);
    flowBuilder.in(initialState);
    return flowBuilder;
  }

  public FlowBuilderDsl.AfterIn<TState, TStep, TRoute> in(TStep state) {
    addStep(state);
    currentStep = state;
    currentRoute = null;
    return this;
  }

  private void addStep(TStep state) {
    if(!steps.containsKey(state))
      steps.put(state, new SimpleFlowStep(state));
  }

  public FlowBuilderDsl.When<TState, TStep, TRoute> choice(Function<TState, TRoute> router) {
    if(currentStep != null)
      getCurrentStep().setRouter(router);
    return this;
  }

  private SimpleFlowStep getCurrentStep() {
    if(currentStep == null)
      return null;

    return steps.get(currentStep);
  }

  public FlowBuilderDsl.AfterWhen<TState, TStep, TRoute> when(TRoute route) {
    currentRoute = route;
    return this;
  }

  public FlowBuilderDsl.AfterGoTo<TState, TStep, TRoute> next(TStep target) {
    addStep(target);

    if(currentRoute != null) {
      getCurrentStep().setRouteTarget(currentRoute, target);
      currentRoute = null;
    } else {
      getCurrentStep().setNextStep(target);
    }
    return this;
  }

  public FlowBuilderDsl.GoTo<TState, TStep, TRoute> execute(Consumer<TState> handler) {
    if(currentRoute != null) {
      getCurrentStep().setRouteHandler(currentRoute, handler);
    } else {
      getCurrentStep().setHandler(handler);
    }
    return this;
  }

  public FlowBuilderDsl.GoTo<TState, TStep, TRoute> flow(Flow flow) {
    getCurrentStep().setHandler(flow);
    return this;
  }

  public <I, O> FlowBuilderDsl.GoTo<TState, TStep, TRoute> execute(Function<TState, I> pre, BiConsumer<O, TState> post, Function<I, O> h) {
    Consumer<TState> handler = c -> pre.andThen(h).andThen(o -> {
      post.accept(o, c);
      return null;
    }).apply(c);

    return execute(handler);
  }

  @Override
  public <I, O> FlowBuilderDsl.AfterAdapters<TState, TStep, TRoute> withAdapters(Function<TState, I> pre, BiConsumer<O, TState> post) {
    inputAdapter = pre;
    outputAdapter = post;
    return this;
  }

  @Override
  public <I, O> FlowBuilderDsl.GoTo<TState, TStep, TRoute> execute(Function<I, O> h) {
    return execute((Function<TState, I>) inputAdapter, (BiConsumer<O, TState>) outputAdapter, h);
  }

  @Override
  public <I> FlowBuilderDsl.AfterExtract<TState, TStep, TRoute> extract(Function<TState, I> pre) {
    inputAdapter = pre;
    outputAdapter = (i, c) -> {};
    return this;
  }

  @Override
  public <I> FlowBuilderDsl.GoTo<TState, TStep, TRoute> thenExecute(Consumer<I> h) {
    return execute((I i) -> {
      h.accept(i);
      return null;
    });
  }

  @Override
  public <I, O> FlowBuilderDsl.AfterExecuteFunction<TState, TStep, TRoute> thenExecute(Function<I, O> h) {
    this.functionHandler = h;
    return this;
  }

  @Override
  public <I, O> FlowBuilderDsl.GoTo<TState, TStep, TRoute> merge(BiConsumer<O, TState> outputAdapter) {
    return execute((Function<TState, I>) inputAdapter, outputAdapter, (Function<I, O>) functionHandler);
  }

  public Flow<TState, TStep, TRoute> build() {
    return new SimpleFlow(name, initialState, steps);
  }
}
