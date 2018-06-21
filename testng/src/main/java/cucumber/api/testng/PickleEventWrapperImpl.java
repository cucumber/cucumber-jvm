package cucumber.api.testng;

import cucumber.messages.Pickles.Pickle;

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
