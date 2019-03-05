package containers.payload;

import containers.Payload;

import java.io.Serializable;
import java.util.List;

/**
 * payload for when someone queries who is around.
 */
public class WhosHere implements Serializable, Payload {

  List<String> clients;

  public WhosHere() {
  }

  public WhosHere(final List<String> clients) {
    this.clients = clients;
  }

  public List<String> getClients() {
    return this.clients;
  }

  @Override
  public String toString() {
    return " who's here";
  }
}
