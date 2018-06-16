package logicaltruth.flow.api;

import java.time.Instant;
import java.util.List;

public interface FlowExecutionInfo<TState, TStep, TRoute> {
  String getName();

  TState geState();

  Instant getStartTime();

  Instant getEndTime();

  TStep getStep();

  TRoute getRoute();

  Throwable getError();

  TStep getNextStep();

  List<FlowExecutionInfo<TState, TStep, TRoute>> getChildExecutionInfo();
}
