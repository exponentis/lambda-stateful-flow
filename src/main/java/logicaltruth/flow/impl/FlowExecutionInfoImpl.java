package logicaltruth.flow.impl;

import logicaltruth.flow.api.FlowExecutionInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FlowExecutionInfoImpl<TState, TStep extends Enum<?>, TRoute extends Enum<?>> implements FlowExecutionInfo<TState, TStep, TRoute> {
  private List<FlowExecutionInfo<TState, TStep, TRoute>> stepExecutionInfo = new ArrayList<>();
  private Instant startTime;
  private Instant endTime;
  private TState context;

  private String name;
  private TStep step;
  private TRoute route;
  private Throwable error;
  private TStep nextStep;

  private static String getStackTrace(Throwable error) {
    StringWriter writer = new StringWriter();
    PrintWriter printWriter = new PrintWriter(writer);
    error.printStackTrace(printWriter);
    printWriter.flush();

    return writer.toString();
  }

  public void addChildExecutionInfo(FlowExecutionInfo<TState, TStep, TRoute> stepInfo) {
    stepExecutionInfo.add(stepInfo);
  }

  public Instant getStartTime() {
    return startTime;
  }

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
  }

  public List<FlowExecutionInfo<TState, TStep, TRoute>> getChildExecutionInfo() {
    return stepExecutionInfo;
  }

  public TState geState() {
    return context;
  }

  public void seTState(TState context) {
    this.context = context;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public TStep getStep() {
    return step;
  }

  public void setStep(TStep step) {
    this.step = step;
  }

  public TRoute getRoute() {
    return route;
  }

  public void setRoute(TRoute route) {
    this.route = route;
  }

  public Throwable getError() {
    return error;
  }

  public void setError(Throwable error) {
    this.error = error;
  }

  public TStep getNextStep() {
    return nextStep;
  }

  public void setNextStep(TStep nextStep) {
    this.nextStep = nextStep;
  }

  @Override
  public String toString() {
    List<FlowExecutionInfo<TState, TStep, TRoute>> children = getChildExecutionInfo();
    return "\n {" +
      (Objects.nonNull(name) ?
        "\n\t \"flow\" : \"" + name + "\"," : "") +
      (Objects.nonNull(step) ?
        "\n\t \"step\" : \"" + step + "\"," : "") +
      "\n\t \"startTime\" : \"" + getStartTime() + "\"" +
      ",\n\t \"endTime\" : \"" + getEndTime() + "\"" +
      (Objects.nonNull(route) ?
        ",\n\t \"route\" : \"" + route + "\"" : "") +
      (Objects.nonNull(error) ?
        ",\n\t \"error\" : \"" + (error != null ? getStackTrace(error) : error) + "\"" : "") +
      (!children.isEmpty() ?
        ",\n\t \"children\" : " + children : "") +
      (Objects.nonNull(nextStep) ?
        ",\n\t \"nextStep\" : \"" + nextStep + "\"" : "") +
      "\n }";
  }
}
