package logicaltruth.flow.sample;

import logicaltruth.flow.api.FlowExecutionInfo;
import org.junit.Test;

public class CreditTests {

  @Test
  public void test_conditionals_complete_flow() {
    CreditService.CreditFlowState state = new CreditService.CreditFlowState();
    state.setCustomerId("John");

    FlowExecutionInfo<CreditService.CreditFlowState, CreditService.CREDIT_STEPS, CreditService.CREDIT_ROUTES> info = CreditService.creditDecisionFlow.execute(state);
    System.out.println(info);

    assert (null != state.getCustomer());
    assert (null != state.getCreditDecision());
  }

  @Test
  public void test_conditionals_incomplete_flow() {
    CreditService.CreditFlowState state = new CreditService.CreditFlowState();
    state.setCustomerId("");

    FlowExecutionInfo<CreditService.CreditFlowState, CreditService.CREDIT_STEPS, CreditService.CREDIT_ROUTES> info = CreditService.creditDecisionFlow.execute(state);
    System.out.println(info);

    assert (null == state.getCustomer());
  }
}
