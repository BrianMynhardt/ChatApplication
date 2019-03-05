package containers.payload;

import containers.Payload;

import java.io.Serializable;

public class JoinGroup implements Serializable, Payload {

  private String password;
  private String groupName;


  public JoinGroup(final String password, final String groupName) {
    this.password = password;
    this.groupName = groupName;
  }

  public JoinGroup(final String groupName) {
    this.groupName = groupName;
  }

  public String getGroupName() {
    return groupName;
  }

  public String getPassword() {
    return password;
  }

  @Override
  public String toString() {
    return " join group";
  }
}
