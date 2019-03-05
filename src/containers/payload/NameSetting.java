package containers.payload;

import containers.Payload;

import java.io.Serializable;

public class NameSetting implements Payload, Serializable {

  private String name;

  public NameSetting(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return " changing name to " + getName();
  }
}
