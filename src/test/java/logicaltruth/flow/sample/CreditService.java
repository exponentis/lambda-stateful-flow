package logicaltruth.flow.sample;

import logicaltruth.flow.api.Flow;
import logicaltruth.flow.impl.builder.FlowBuilder;

import java.util.Random;

import static logicaltruth.flow.sample.CreditService.CREDIT_ROUTES.*;
import static logicaltruth.flow.sample.CreditService.CREDIT_STEPS.*;

public class CreditService {

  public static final Flow<CreditFlowState, CREDIT_STEPS, CREDIT_ROUTES> creditDecisionFlow = FlowBuilder.<CreditFlowState, CREDIT_STEPS, CREDIT_ROUTES>
    start("CREDIT_DECISION", VALIDATE_INPUT).choice(state -> validateUserName(state.getCustomerId()) ? VALID_INPUT : INVALID_INPUT)
      .when(VALID_INPUT).next(GET_CUSTOMER_INFO)
      .when(INVALID_INPUT).next(FINISH)
    .in(GET_CUSTOMER_INFO).execute(state -> {
      Customer customer = getCustomer(state.getCustomerId());
      state.setCustomer(customer);
    }).next(GET_CUSTOMER_CREDIT_SCORE)
    .in(GET_CUSTOMER_CREDIT_SCORE).extract(CreditFlowState::getCustomer).thenExecute(CreditService::populateCreditScore).next(ANALYZE_CREDIT_SCORE)
    .in(ANALYZE_CREDIT_SCORE).choice(state -> analyzeScore(state.getCustomer().getCreditScore()))
      .when(CREDIT_LOW).next(DECISION_REJECT)
      .when(CREDIT_HIGH).next(DECISION_APPROVE)
      .when(CREDIT_MEDIUM).next(EXTRA_ASSESMENT)
    .in(EXTRA_ASSESMENT).extract(CreditFlowState::getCustomer).thenExecute(CreditService::makeAssesment).merge((d, state) -> state.setExtraAssesmentResult((Boolean) d)).next(MAKE_DECISION)
    .in(MAKE_DECISION).choice(state -> state.getExtraAssesmentResult() ? ASSESMENT_POSITIVE : ASSESMENT_NEGATIVE)
      .when(ASSESMENT_NEGATIVE).next(DECISION_REJECT)
      .when(ASSESMENT_POSITIVE).next(DECISION_APPROVE)
    .in(DECISION_APPROVE).execute(state -> state.setCreditDecision(true)).next(FINISH)
    .in(DECISION_REJECT).execute(state -> state.setCreditDecision(false)).next(FINISH)
    .build();

  private static CREDIT_ROUTES analyzeScore(Integer creditScore) {
    if(creditScore < 350)
      return CREDIT_ROUTES.CREDIT_LOW;
    else if(creditScore > 650)
      return CREDIT_ROUTES.CREDIT_HIGH;
    else
      return CREDIT_ROUTES.CREDIT_MEDIUM;
  }

  private static boolean makeAssesment(Customer c) {
    return c.getCreditScore() > 350 + new Random().nextInt(300) ? true : false;
  }

  private static Customer getCustomer(String userId) {
    Customer customer = new Customer();
    customer.setCustomerId(userId);
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
    GET_CUSTOMER_INFO,
    GET_CUSTOMER_CREDIT_SCORE,
    ANALYZE_CREDIT_SCORE,
    EXTRA_ASSESMENT,
    MAKE_DECISION,
    DECISION_APPROVE,
    DECISION_REJECT,
    FINISH
  }

  enum CREDIT_ROUTES {
    VALID_INPUT(VALIDATE_INPUT),
    INVALID_INPUT(VALIDATE_INPUT),
    CREDIT_LOW(ANALYZE_CREDIT_SCORE),
    CREDIT_MEDIUM(ANALYZE_CREDIT_SCORE),
    CREDIT_HIGH(ANALYZE_CREDIT_SCORE),
    ASSESMENT_POSITIVE(EXTRA_ASSESMENT),
    ASSESMENT_NEGATIVE(EXTRA_ASSESMENT);

    CREDIT_ROUTES(CREDIT_STEPS step) {
    }
  }

  public static class CreditFlowState {
    private String customerId;
    private Customer customer;
    private Boolean extraAssesmentResult;
    private Boolean creditDecision;

    public String getCustomerId() {
      return customerId;
    }

    public void setCustomerId(String customerId) {
      this.customerId = customerId;
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

    public Boolean getExtraAssesmentResult() {
      return extraAssesmentResult;
    }

    public void setExtraAssesmentResult(Boolean extraAssesmentResult) {
      this.extraAssesmentResult = extraAssesmentResult;
    }
  }
}
