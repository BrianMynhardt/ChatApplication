package exceptions;

public class EmptyTextException extends MalformedMessageException {

  public EmptyTextException() {
    super("text is empty");
  }
}
