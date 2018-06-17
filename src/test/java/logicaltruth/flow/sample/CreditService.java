package logicaltruth.flow.sample;

import logicaltruth.flow.api.Flow;
import logicaltruth.flow.impl.builder.FlowBuilder;

import java.util.Random;

import static logicaltruth.flow.sample.CreditService.CREDIT_ROUTES.*;
import static logicaltruth.flow.sample.CreditService.CREDIT_STEPS.*;

public class CreditService {

  public static final Flow<CreditFlowState, CREDIT_STEPS, CREDIT_ROUTES> creditDecisionFlow = FlowBuilder.<CreditFlowState, CREDIT_STEPS, CREDIT_ROUTES>
    start("CREDIT_DECISION", VALIDATE_INPUT).choice(state -> validateUserName((String) state.getUserId()) ? VALID_INPUT : INVALID_INPUT)
    .when(VALID_INPUT).next(GET_USER_INFO)
    .when(INVALID_INPUT).next(FINISH)
    .in(GET_USER_INFO).execute(state -> {
      Customer customer = getUser(state.getUserId());
      state.setCustomer(customer);
    }).next(GET_USER_CREDIT_SCORE)
    .in(GET_USER_CREDIT_SCORE).extract(CreditFlowState::getCustomer).thenExecute(CreditService::populateCreditScore).next(MAKE_DECISION)
    .in(MAKE_DECISION).extract(CreditFlowState::getCustomer).thenExecute(CreditService::makeDecision).merge((d, state) -> state.setCreditDecision((Boolean) d)).next(FINISH)
    .build();

  private static boolean makeDecision(Customer u) {
    return u.getCreditScore() > 500 ? true : false;
  }

  private static Customer getUser(String userId) {
    Customer customer = new Customer();
    customer.setUserId(userId);
    return customer;
  }

  private static void populateCreditScore(Customer c) {
    Integer score = new Random().nextInt(800);
    c.setCreditScore(score);
  }

  private static boolean validateUserName(String userName) {
    return userName != null && !userName.isEmpty();
  }

  enum CREDIT_STEPS {
    VALIDATE_INPUT,
    GET_USER_INFO,
    GET_USER_CREDIT_SCORE,
    MAKE_DECISION,
    FINISH
  }

  enum CREDIT_ROUTES {
    VALID_INPUT(VALIDATE_INPUT), INVALID_INPUT(VALIDATE_INPUT);

    CREDIT_ROUTES(CREDIT_STEPS step) {
    }
  }

  public static class CreditFlowState {
    private String userId;
    private Customer customer;
    private Boolean creditDecision;

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }

    public Customer getCustomer() {
      return customer;
    }

    public void setCustomer(Customer customer) {
      this.customer = customer;
    }

    public Boolean getCreditDecision() {
      return creditDecision;
    }

    public void setCreditDecision(Boolean creditDecision) {
      this.creditDecision = creditDecision;
    }
  }
}
