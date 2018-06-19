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

  public static final Consumer<Throwable> DEFAULT_ERROR_HANDLER = t -> {
    throw new FlowExecutionException(t);
  };

  private TStep state;
  private TStep nextStep;
  private Consumer<TState> handler;
  private Flow flowHandler;
  private BiConsumer<TState, Throwable> errorHandler; // = (c, t) -> DEFAULT_ERROR_HANDLER.accept(t);
  private Function<TState, TRoute> router;
  private Map<TRoute, Consumer<TState>> routeHandlerMap = new HashMap<>();
  private Map<TRoute, TStep> routeTargetMap = new HashMap<TRoute, TStep>();
  private Consumer<TState> defaultRouteHandler;
  private TStep defaultRouteTarget;

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
      throw new FlowBuilderException(String.format("Handler for evaluate: %s step state: %s is already declared", route, state));
    routeHandlerMap.put(route, routeHandler);
  }

  public void setDefaultRouteHandler(Consumer<TState> routeHandler) {
    this.defaultRouteHandler = routeHandler;
  }

  public void setRouteTarget(TRoute route, TStep target) {
    if(routeTargetMap.containsKey(route))
      throw new FlowBuilderException(String.format("Target for evaluate: %s step state: %s is already declared", route, state));
    routeTargetMap.put(route, target);
  }


  public void setDefaultRouteTarget(TStep target) {
    defaultRouteTarget = target;
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

    boolean isComplete = true;

    try {
      if(router != null) {
        TRoute route = router.apply(context);
        executionInfo.setRoute(route);
        TStep nextStep = routeTargetMap.get(route);
        if(nextStep != null) {
          executionInfo.setNextStep(nextStep);
        } else if(defaultRouteTarget != null) {
          executionInfo.setNextStep(defaultRouteTarget);
        }
        Consumer<TState> routeHandler = routeHandlerMap.get(route);
        if(routeHandler != null) {
          if(errorHandler != null) {
            try {
              routeHandler.accept(context);
            } catch(Throwable error) {
              errorHandler.accept(context, error);
            }
          } else {
            routeHandler.accept(context);
          }
        } else if(defaultRouteHandler != null) {
          try {
            defaultRouteHandler.accept(context);
          } catch(Throwable error) {
            errorHandler.accept(context, error);
          }
        }
        //executionInfo.setNextStep(routeTargetMap.get(route));
      } else {
        executionInfo.setNextStep(nextStep);
        if(handler != null) {
          if(errorHandler != null) {
            try {
              handler.accept(context);
            } catch(Throwable error) {
              errorHandler.accept(context, error);
            }
          } else {
            handler.accept(context);
          }
        } else if(flowHandler != null) {
          if(errorHandler != null) {
            try {
              FlowExecutionInfo<TState, TStep, TRoute> stepExecutionInfo = flowHandler.execute(context);
              executionInfo.addChildExecutionInfo(stepExecutionInfo);
              isComplete = isComplete && stepExecutionInfo.isComplete();
            } catch(Throwable error) {
              errorHandler.accept(context, error);
            }
          } else {
            FlowExecutionInfo<TState, TStep, TRoute> stepExecutionInfo = flowHandler.execute(context);
            executionInfo.addChildExecutionInfo(stepExecutionInfo);
            isComplete = isComplete && stepExecutionInfo.isComplete();
          }
        }
        //executionInfo.setNextStep(nextStep);
      }

      executionInfo.setComplete(isComplete);
      executionInfo.setEndTime(Instant.now());

    } catch(Throwable error) {
      executionInfo.setError(error);
      if(errorHandler != null) {
        errorHandler.accept(context, error);
      }
    }

    executionInfo.setEndTime(Instant.now());
    return executionInfo;
  }
}
