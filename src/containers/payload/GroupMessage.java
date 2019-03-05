package containers.payload;

public class GroupMessage extends TextMessage {

  private String groupName;

  public GroupMessage(String message, String groupName) {
    super(message);
    this.groupName = groupName;
  }

  public String getGroupName() {
    return groupName;
  }
}
