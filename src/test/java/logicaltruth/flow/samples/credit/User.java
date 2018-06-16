package logicaltruth.flow.samples.credit;

public class User {
  private String userName;
  private Integer creditScore;
  private Boolean creditDecision;

  public String getUserName() {
    return userName;
  }

  public void setUserName(String username) {
    this.userName = username;
  }

  public Integer getCreditScore() {
    return creditScore;
  }

  public void setCreditScore(Integer creditScore) {
    this.creditScore = creditScore;
  }

  public Boolean getCreditDecision() {
    return creditDecision;
  }

  public void setCreditDecision(Boolean creditDecision) {
    this.creditDecision = creditDecision;
  }
}
