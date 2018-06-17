package logicaltruth.flow.samples.credit;

import logicaltruth.flow.api.FlowExecutionInfo;
import org.junit.Test;

import static logicaltruth.flow.samples.credit.CreditService.CreditFlowState;

public class CreditTests {

  @Test
  public void test_conditionals_complete_flow() {
    CreditFlowState state2 = new CreditFlowState();
    state2.setUserId("AQBC");

    FlowExecutionInfo<CreditFlowState, CreditService.CREDIT_STEPS, CreditService.CREDIT_ROUTES> info = CreditService.creditDecisionFlow.execute(state2);
    System.out.println(info);

    Customer customer = state2.getCustomer();
    System.out.println(state2.getCreditDecision());

    assert (null != customer);
  }

  @Test
  public void test_conditionals_incomplete_flow() {
    CreditFlowState state1 = new CreditFlowState();
    state1.setUserId("ABC");

    FlowExecutionInfo<CreditFlowState, CreditService.CREDIT_STEPS, CreditService.CREDIT_ROUTES> info = CreditService.creditDecisionFlow.execute(state1);
    System.out.println(info);
    assert (null == state1.getCustomer());
  }


}
