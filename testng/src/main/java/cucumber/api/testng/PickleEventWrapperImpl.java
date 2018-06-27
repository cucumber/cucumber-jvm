package cucumber.api.testng;

import io.cucumber.messages.Messages.Pickle;

class PickleEventWrapperImpl implements PickleEventWrapper {

    private final Pickle pickle;

    PickleEventWrapperImpl(Pickle pickle) {
        this.pickle = pickle;
    }

    public Pickle getPickle() {
        return pickle;
    }

    @Override
    public String toString() {
        return "\"" + pickle.getName() + "\"";
    }
}
