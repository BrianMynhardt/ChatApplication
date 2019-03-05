package containers.payload;

import containers.Payload;

import java.io.Serializable;

public class CreateGroup implements Payload, Serializable {

  public CreateGroup(final String password, final String groupName) {
    this.password = password;
    this.groupName = groupName;
  }

  public CreateGroup(final String groupName) {
    this.groupName = groupName;
  }

  private String password;
  private String groupName;

  @Override
  public String toString() {
    return " create group";
  }

  public String getGroupName() {
    return groupName;
  }

  public String getPassword() {
    return password;
  }
}
