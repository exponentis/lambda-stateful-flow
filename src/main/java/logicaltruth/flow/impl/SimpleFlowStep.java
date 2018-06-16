package logicaltruth.flow.impl;

import logicaltruth.flow.api.Flow;
import logicaltruth.flow.api.FlowExecutionInfo;
import logicaltruth.flow.impl.builder.FlowBuilderException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class SimpleFlowStep<TState, TStep extends Enum<?>, TRoute extends Enum<?>> implements Flow<TState, TStep, TRoute> {

  private TStep state;
  private TStep nextStep;
  private Consumer<TState> handler;
  private Flow flowHandler;
  private BiConsumer<TState, Throwable> errorHandler;
  private Function<TState, TRoute> router;
  private Map<TRoute, Consumer<TState>> routeHandlerMap = new HashMap<>();
  private Map<TRoute, TStep> routeTargetMap = new HashMap<TRoute, TStep>();

  public SimpleFlowStep(TStep state) {
    this.state = state;
  }

  public void setHandler(Consumer<TState> handler) {
    this.handler = handler;
  }

  public <UStep extends Enum<?>, URoute extends Enum<?>> void setHandler(Flow<TState, UStep, URoute> flow) {
    this.flowHandler = flow;
  }

  public void setRouter(Function<TState, TRoute> router) {
    this.router = router;
  }

  public void setRouteHandler(TRoute route, Consumer<TState> routeHandler) {
    if(routeHandlerMap.containsKey(route))
      throw new FlowBuilderException(String.format("Handler for choice: %s in state: %s is already declared", route, state));
    routeHandlerMap.put(route, routeHandler);
  }

  public void setRouteTarget(TRoute route, TStep target) {
    if(routeTargetMap.containsKey(route))
      throw new FlowBuilderException(String.format("Target for choice: %s in state: %s is already declared", route, state));
    routeTargetMap.put(route, target);
  }

  public void setNextStep(TStep nextStep) {
    this.nextStep = nextStep;
  }

  public void setErrorHandler(BiConsumer<TState, Throwable> errorHandler) {
    this.errorHandler = errorHandler;
  }

  public FlowExecutionInfo<TState, TStep, TRoute> execute(TState context) {
    FlowExecutionInfoImpl<TState, TStep, TRoute> executionInfo = new FlowExecutionInfoImpl<>();
    executionInfo.setStartTime(Instant.now());
    executionInfo.setStep(state);

    try {
      if(router != null) {
        TRoute route = router.apply(context);
        executionInfo.setRoute(route);
        Consumer<TState> routeHandler = routeHandlerMap.get(route);
        if(routeHandler != null) {
          try {
          routeHandler.accept(context);
          } catch(Throwable error) {
            handleError(context, executionInfo, error);
          }
        }
        executionInfo.setNextStep(routeTargetMap.get(route));
      } else {
        if(handler != null) {
          try {
          handler.accept(context);
          } catch(Throwable error) {
            handleError(context, executionInfo, error);
          }
        } else if(flowHandler != null) {
          FlowExecutionInfo<TState, TStep, TRoute> stepExecutionInfo = flowHandler.execute(context);
          executionInfo.addChildExecutionInfo(stepExecutionInfo);
        }
        executionInfo.setNextStep(nextStep);
      }

      executionInfo.setComplete(true);
      executionInfo.setEndTime(Instant.now());

    } catch(Throwable error) {
      handleError(context, executionInfo, error);
    }

    executionInfo.setEndTime(Instant.now());
    return executionInfo;
  }

  private void handleError(TState context, FlowExecutionInfoImpl<TState, TStep, TRoute> executionInfo, Throwable error) {
    if(errorHandler != null) {
      errorHandler.accept(context, error);
    }
    else {
      executionInfo.setError(error);
    }
  }
}
