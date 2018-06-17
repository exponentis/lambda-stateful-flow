package logicaltruth.flow.sample;

import logicaltruth.flow.api.FlowExecutionInfo;
import org.junit.Test;

public class CreditTests {

  @Test
  public void test_conditionals_complete_flow() {
    CreditService.CreditFlowState state2 = new CreditService.CreditFlowState();
    state2.setCustomerId("John");

    FlowExecutionInfo<CreditService.CreditFlowState, CreditService.CREDIT_STEPS, CreditService.CREDIT_ROUTES> info = CreditService.creditDecisionFlow.execute(state2);
    System.out.println(info);

    Customer customer = state2.getCustomer();
    System.out.println(state2.getCreditDecision());

    assert (null != customer);
  }

  @Test
  public void test_conditionals_incomplete_flow() {
    CreditService.CreditFlowState state1 = new CreditService.CreditFlowState();
    state1.setCustomerId("");

    FlowExecutionInfo<CreditService.CreditFlowState, CreditService.CREDIT_STEPS, CreditService.CREDIT_ROUTES> info = CreditService.creditDecisionFlow.execute(state1);
    System.out.println(info);
    assert (null == state1.getCustomer());
  }


}
