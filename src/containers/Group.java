package containers;

import java.io.Serializable;

public class Group implements Serializable {

    private String groupName;

    public Group(String groupName) {
        this.groupName = groupName;
    }

    /**
     * default constructor for when we want to send the message to all.
     */
    public Group() {}

  public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return this.getGroupName();
    }
}
