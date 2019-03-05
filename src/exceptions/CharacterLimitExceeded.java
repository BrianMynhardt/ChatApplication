package exceptions;

public class CharacterLimitExceeded extends MalformedMessageException {

    public CharacterLimitExceeded() {
        super("A message can only be up to 10000 characters long");
    }
}
