package logicaltruth.flow.samples.credit;

import logicaltruth.flow.api.FlowExecutionInfo;
import org.junit.Test;


import java.util.HashMap;
import java.util.Map;

public class CreditTests {


  @Test
  public void test_conditionals_partial_flow() {
    Map<String, Object> state1 = new HashMap<String, Object>() {{
      put("userId", "ABC");
    }};

    FlowExecutionInfo<Map<String, Object>, CreditService.CREDIT_STEPS, CreditService.CREDIT_ROUTES> info = CreditService.creditDecisionFlow.execute(state1);
    System.out.println(info);
    assert (null == state1.get("user"));
  }

  @Test
  public void test_conditionals_full_flow() {
    Map<String, Object> state2 = new HashMap<String, Object>() {{
      put("userId", "AQBC");
    }};

    FlowExecutionInfo<Map<String, Object>, CreditService.CREDIT_STEPS, CreditService.CREDIT_ROUTES> info = CreditService.creditDecisionFlow.execute(state2);
    System.out.println(info);

    User user = (User) state2.get("user");
    System.out.println(user.getCreditDecision());

    assert (null != user);
  }
}
