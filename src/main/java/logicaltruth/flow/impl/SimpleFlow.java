package logicaltruth.flow.impl;

import logicaltruth.flow.api.Flow;
import logicaltruth.flow.api.FlowExecutionInfo;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;

public class SimpleFlow<TState, TStep extends Enum<?>, TRoute extends Enum<?>> implements
  Flow<TState, TStep, TRoute> {

  private TStep initialStep;
  private Function<TState, TStep> initialRouter;
  private String name;
  private Map<TStep, SimpleFlowStep> steps;

  public SimpleFlow(String name, TStep initialStep, Map<TStep, SimpleFlowStep> steps) {
    this.name = name;
    this.initialStep = initialStep;
    this.steps = steps;
  }

  public SimpleFlow(String name, Function<TState, TStep> initialRouter, Map<TStep, SimpleFlowStep> steps) {
    this.name = name;
    this.initialRouter = initialRouter;
    this.steps = steps;
  }

  public FlowExecutionInfo<TState, TStep, TRoute> execute(TState context) {
    FlowExecutionInfoImpl<TState, TStep, TRoute> flowExecutionInfo = new FlowExecutionInfoImpl<TState, TStep, TRoute>();
    flowExecutionInfo.setName(name);
    flowExecutionInfo.setStartTime(Instant.now());
    flowExecutionInfo.seTState(context);

    TStep step = initialStep;

    if(step == null && initialRouter != null) {
      step = initialRouter.apply(context);
    }

    boolean isComplete = true;
    while(step != null) {
      if(!isComplete)
        break;

      SimpleFlowStep currentFlowStep = steps.get(step);
      flowExecutionInfo.setStep(step);
      if(currentFlowStep == null)
        break;
      FlowExecutionInfo<TState, TStep, TRoute> stepExecutionInfo = currentFlowStep.execute(context);
      flowExecutionInfo.addChildExecutionInfo(stepExecutionInfo);
      isComplete = isComplete && stepExecutionInfo.isComplete();
      step = stepExecutionInfo.getNextStep();
    }
    flowExecutionInfo.setComplete(isComplete);
    flowExecutionInfo.setEndTime(Instant.now());

    return flowExecutionInfo;
  }
}
