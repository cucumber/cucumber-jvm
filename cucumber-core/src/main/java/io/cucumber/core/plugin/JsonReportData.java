package io.cucumber.core.plugin;

import io.cucumber.messages.types.Envelope;
import io.cucumber.query.Query;

class JsonReportData {

    private final Query query = new Query();

    void collect(Envelope envelope) {
        query.update(envelope);
    }
}
