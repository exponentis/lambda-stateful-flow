package logicaltruth.flow.impl;

import logicaltruth.flow.api.Flow;
import logicaltruth.flow.api.FlowExecutionInfo;

import java.time.Instant;
import java.util.Map;

public class SimpleFlow<TState, TStep extends Enum<?>, TRoute extends Enum<?>> implements //extends SimpleFlowStep<TState, TStep, TRoute>
  Flow<TState, TStep, TRoute> {

  private final TStep initialState;
  private String name;
  private Map<TStep, SimpleFlowStep> steps;

  public SimpleFlow(String name, TStep initialState, Map<TStep, SimpleFlowStep> steps) {
    this.name = name;
    this.initialState = initialState;
    this.steps = steps;
  }

  public FlowExecutionInfo<TState, TStep, TRoute> execute(TState context) {
    FlowExecutionInfoImpl<TState, TStep, TRoute> flowExecutionInfo = new FlowExecutionInfoImpl<TState, TStep, TRoute>();
    flowExecutionInfo.setName(name);
    flowExecutionInfo.setStartTime(Instant.now());
    flowExecutionInfo.seTState(context);

    TStep state = initialState;
    while(state != null) {
      SimpleFlowStep currentFlowStep = steps.get(state);
      if(currentFlowStep == null)
        break;
      FlowExecutionInfo<TState, TStep, TRoute> stepExecutionInfo = currentFlowStep.execute(context);
      flowExecutionInfo.addChildExecutionInfo(stepExecutionInfo);
      state = stepExecutionInfo.getNextStep();
    }

    flowExecutionInfo.setEndTime(Instant.now());

    return flowExecutionInfo;
  }
}
