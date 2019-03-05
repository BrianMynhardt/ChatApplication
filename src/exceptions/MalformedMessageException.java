package exceptions;

public class MalformedMessageException extends RuntimeException {

  public MalformedMessageException(String message) {
    throw new RuntimeException(message);
  }
}
