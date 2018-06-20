package logicaltruth.flow;

public class Context<Input, Output, Temp> {
  private Input input;
  private Output output;
  private Temp temp;
  private StringBuffer log = new StringBuffer();

  public Context(Input input, Temp temp) {
    this.input = input;
    this.temp = temp;
  }

  public Context(Input input) {
    this.input = input;
  }

  public Input input() {
    return input;
  }

  public Temp temp() {
    return temp;
  }

  public Output output() {
    return output;
  }

  public void setOutput(Output output) {
    this.output = output;
  }

  public void log(String txt) {
    log.append(txt+"\n");
  }

  public String log() {
    return log.toString();
  }
}
