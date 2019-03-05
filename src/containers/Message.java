package containers;

import java.io.Serializable;

/**
 * Object that is sent between server and client.
 */
public class Message implements Serializable {

  private Client sender;
  private Payload payload;
  private Group reciever;

  public void setSender(Client client) {
    this.sender = client;
  }

  public Client getSender() {
    return sender;
  }

  public void setPayload(Payload payload) {
    this.payload = payload;
  }

  public Payload getPayload() {
    return this.payload;
  }

  public Group getRecievers() {
    return reciever;
  }

  public void setReciever() {
    this.reciever = new Group();
  }

  public void setReciever(final String groupdId) {
    this.reciever = new Group(groupdId);
  }

  public void setReciever(final Group group) {
    this.reciever = group;
  }

  @Override
  public String toString() {
    String format = "Message being sent from %s to %s with the payload %s";
    return String.format(
        format,
        sender,
        reciever,
        payload);
  }
}
