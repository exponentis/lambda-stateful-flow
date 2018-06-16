package logicaltruth.flow.samples.credit;

import logicaltruth.flow.api.Flow;
import logicaltruth.flow.impl.builder.FlowBuilder;

import java.util.Map;
import java.util.Random;

import static logicaltruth.flow.samples.credit.CreditService.CREDIT_ROUTES.*;
import static logicaltruth.flow.samples.credit.CreditService.CREDIT_STEPS.*;

public class CreditService {

  public static final Flow<Map<String, Object>, CREDIT_STEPS, CREDIT_ROUTES> creditDecisionFlow = FlowBuilder.<Map<String, Object>, CREDIT_STEPS, CREDIT_ROUTES>
    start("CREDIT_DECISION", VALIDATE_INPUT).choice(c -> validateUserName((String) c.get("userId")) ? VALID_INPUT : INVALID_INPUT)
    .when(VALID_INPUT).next(GET_USER_INFO)
    .when(INVALID_INPUT).next(FINISH)
    .in(GET_USER_INFO).execute(c -> {
      User user = getUser((String) c.get("userId"));
      c.put("user", user);
    }).next(MAKE_DECISION)
    .in(MAKE_DECISION).extract(c -> c.get("user")).<User>thenExecute(u -> {
      u.setCreditDecision(makeDecision(u));
    }).next(FINISH)
    .build();

  private static boolean makeDecision(User u) {
    return u.getCreditScore() > 500 ? true : false;
  }

  private static User getUser(String userName) {
    User user = new User();
    user.setUserName(userName);
    user.setCreditScore(new Random().nextInt(800));
    return user;
  }

  private static boolean validateUserName(String userName) {
    return userName != null && userName.contains("Q");
  }

  enum CREDIT_STEPS {
    VALIDATE_INPUT,
    GET_USER_INFO,
    MAKE_DECISION,
    FINISH
  }

  enum CREDIT_ROUTES {
    VALID_INPUT(VALIDATE_INPUT), INVALID_INPUT(VALIDATE_INPUT);

    CREDIT_ROUTES(CREDIT_STEPS step) {
    }
  }
}
