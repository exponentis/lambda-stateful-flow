package logicaltruth.flow.impl;

public class FlowExecutionException extends RuntimeException {
  public FlowExecutionException(String s) {
    super(s);
  }

  public FlowExecutionException(Throwable t) {
    super(t);
  }
}
