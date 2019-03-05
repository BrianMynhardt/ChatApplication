package containers.payload;

import containers.Payload;

import java.io.Serializable;

public class ConnectionAcceptedMessage implements Payload, Serializable {
  @Override
  public String toString() {
    return " Connection accepted message";
  }
}
