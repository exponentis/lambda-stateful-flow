package logicaltruth.flow.samples.credit;

public class Customer {
  private String userId;
  private Integer creditScore;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String username) {
    this.userId = username;
  }

  public Integer getCreditScore() {
    return creditScore;
  }

  public void setCreditScore(Integer creditScore) {
    this.creditScore = creditScore;
  }
}
