package logicaltruth.flow.impl.builder;

import com.sun.javaws.exceptions.InvalidArgumentException;
import logicaltruth.flow.api.Flow;

import static logicaltruth.flow.api.FlowBuilderDsl.*;

import logicaltruth.flow.impl.SimpleFlow;
import logicaltruth.flow.impl.SimpleFlowStep;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class FlowBuilder<TState, TStep extends Enum<?>, TRoute extends Enum<?>> implements FluentFlowBuilder<TState, TStep, TRoute> {

  //builder outcome
  private final String name;
  private TStep initialState = null;
  private Function<TState, TStep> initialRouter = null;
  private final Map<TStep, SimpleFlowStep> steps = new HashMap<>();

  //temp state
  private TStep currentStep;
  private TRoute currentRoute;

  private Boolean isDefaultRoute = false;

  private Function<TState, ?> inputAdapter;
  private Function<?, ?> functionHandler;
  private BiConsumer<?, TState> outputAdapter;

  private FlowBuilder(final String name, final TStep initialState) {
    this.name = name;
    this.initialState = initialState;
  }

  private FlowBuilder(final String name, final Function<TState, TStep> initialRouter) {
    this.name = name;
    this.initialRouter = initialRouter;
  }

  public static <TState, TStep extends Enum<?>, TRoute extends Enum<?>> AfterIn<TState, TStep, TRoute> start(String name, TStep initialState) {
    FlowBuilder<TState, TStep, TRoute> flowBuilder = new FlowBuilder<TState, TStep, TRoute>(name, initialState);
    flowBuilder.step(initialState);
    return flowBuilder;
  }

  public static <TState, TStep extends Enum<?>, TRoute extends Enum<?>> In<TState, TStep, TRoute> start(String name, Function<TState, TStep> initialRouter) {
    FlowBuilder<TState, TStep, TRoute> flowBuilder = new FlowBuilder<TState, TStep, TRoute>(name, initialRouter);
    return flowBuilder;
  }

  public static <TState, TStep extends Enum<?>, TRoute extends Enum<?>> AfterIn<TState, TStep, TRoute> start(TStep initialState) {
    FlowBuilder<TState, TStep, TRoute> flowBuilder = new FlowBuilder<TState, TStep, TRoute>("", initialState);
    flowBuilder.step(initialState);
    return flowBuilder;
  }

  public AfterIn<TState, TStep, TRoute> step(TStep state) {
    addStep(state);
    currentStep = state;
    currentRoute = null;
    return this;
  }

  private void addStep(TStep state) {
    if(!steps.containsKey(state))
      steps.put(state, new SimpleFlowStep(state));
  }

  public When<TState, TStep, TRoute> evaluate(Function<TState, TRoute> router) {
    if(currentStep != null)
      getCurrentStep().setRouter(router);
    return this;
  }

  private SimpleFlowStep getCurrentStep() {
    if(currentStep == null)
      return null;

    return steps.get(currentStep);
  }

  public AfterWhen<TState, TStep, TRoute> when(TRoute route) {
    currentRoute = route;
    return this;
  }

  public AfterGoTo<TState, TStep, TRoute> next(TStep target) {
    addStep(target);

    if(currentRoute != null) {
      getCurrentStep().setRouteTarget(currentRoute, target);
      currentRoute = null;
    } else if(isDefaultRoute) {
      getCurrentStep().setDefaultRouteTarget(target);
      isDefaultRoute = false;
    } else {
      getCurrentStep().setNextStep(target);
    }
    return this;
  }

  public GoTo<TState, TStep, TRoute> execute(Consumer<TState> handler) {
    if(currentRoute != null) {
      getCurrentStep().setRouteHandler(currentRoute, handler);
    } else {
      getCurrentStep().setHandler(handler);
    }
    return this;
  }

  public GoTo<TState, TStep, TRoute> flow(Flow flow) {
    getCurrentStep().setHandler(flow);
    return this;
  }

  public <I, O> GoTo<TState, TStep, TRoute> execute(Function<TState, I> pre, BiConsumer<O, TState> post, Function<I, O> h) {
    Consumer<TState> handler = c -> pre.andThen(h).andThen(o -> {
      post.accept(o, c);
      return null;
    }).apply(c);

    return execute(handler);
  }

  @Override
  public <I, O> AfterAdapters<TState, TStep, TRoute> withAdapters(Function<TState, I> pre, BiConsumer<O, TState> post) {
    inputAdapter = pre;
    outputAdapter = post;
    return this;
  }

  @Override
  public <I, O> GoTo<TState, TStep, TRoute> execute(Function<I, O> h) {
    return execute((Function<TState, I>) inputAdapter, (BiConsumer<O, TState>) outputAdapter, h);
  }

  @Override
  public <I> AfterExtract<TState, TStep, TRoute> extract(Function<TState, I> pre) {
    inputAdapter = pre;
    outputAdapter = (i, c) -> {
    };
    return this;
  }

  @Override
  public <I> GoTo<TState, TStep, TRoute> thenExecute(Consumer<I> h) {
    return execute((I i) -> {
      h.accept(i);
      return null;
    });
  }

  @Override
  public <I, O> AfterExecuteFunction<TState, TStep, TRoute> thenExecute(Function<I, O> h) {
    this.functionHandler = h;
    return this;
  }

  @Override
  public <I> When<TState, TStep, TRoute> thenEvaluate(Function<I, TRoute> router) {
    if(currentStep != null)
      getCurrentStep().setRouter(((Function<TState, I>) inputAdapter).andThen(router));
    return this;
  }

  @Override
  public <I, O> GoTo<TState, TStep, TRoute> merge(BiConsumer<O, TState> outputAdapter) {
    return execute((Function<TState, I>) inputAdapter, outputAdapter, (Function<I, O>) functionHandler);
  }

  public Flow<TState, TStep, TRoute> build() {
    if(initialState != null)
      return new SimpleFlow(name, initialState, steps);
    if(initialRouter != null)
      return new SimpleFlow(name, initialRouter, steps);
    throw new FlowBuilderException("Cannot build flow");
  }

  @Override
  public AfterOnError<TState, TStep, TRoute> onError(BiConsumer<TState, Throwable> errorHandler) {
    getCurrentStep().setErrorHandler(errorHandler);
    return this;
  }

  @Override
  public AfterOnError<TState, TStep, TRoute> onError(Consumer<Throwable> errorHandler) {
    return onError((c, t) -> errorHandler.accept(t));
  }

  @Override
  public AfterOnError<TState, TStep, TRoute> onErrorThrow() {
    return onError(SimpleFlowStep.DEFAULT_ERROR_HANDLER);
  }

  @Override
  public GoTo<TState, TStep, TRoute> orElse(Consumer<TState> handler) {
    isDefaultRoute = true;
    getCurrentStep().setDefaultRouteHandler(handler);
    return this;
  }
}
