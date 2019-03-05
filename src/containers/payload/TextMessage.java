package containers.payload;

import containers.Payload;

import java.io.Serializable;

public class TextMessage implements Payload, Serializable {

  private String message;

  public TextMessage(final String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return String.format(" text message %s", this.message);
  }

}
