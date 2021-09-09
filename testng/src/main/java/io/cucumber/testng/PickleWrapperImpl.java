package io.cucumber.testng;

final class PickleWrapperImpl implements PickleWrapper {

    private final Pickle pickle;

    PickleWrapperImpl(Pickle pickle) {
        this.pickle = pickle;
    }

    public Pickle getPickle() {
        return pickle;
    }

    @Override
    public String toString() {
        return "\"" + pickle.getPickle().getName() + "\"";
    }

}
