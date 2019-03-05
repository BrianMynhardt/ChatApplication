package containers;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * object containing information about the clients.
 */
public class Client implements Serializable {

  private String name;

  private transient ObjectOutputStream outputStream;

  private transient ObjectInputStream inputStream;

  public Client(final String name) {
    this.name = name;
  }

  public Client() {}

  public String getName() {
    return name;
  }

  public void setOutputStream(ObjectOutputStream outputStream) {
    this.outputStream = outputStream;
  }

  public ObjectOutputStream getOutputStream() {
    return outputStream;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public ObjectInputStream getInputStream() {
    return inputStream;
  }

  public void setInputStream(ObjectInputStream inputStream) {
    this.inputStream = inputStream;
  }

  @Override
  public String toString() {
    return this.name;
  }

}
