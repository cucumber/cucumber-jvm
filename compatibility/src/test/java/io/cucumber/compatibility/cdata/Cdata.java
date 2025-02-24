package io.cucumber.compatibility.cdata;

import io.cucumber.java.en.Given;

public class Cdata {

    @Given("I have {int} <![CDATA[cukes]]> in my belly")
    public void iHaveCDATACukesInMyBelly(int count) {
    }
}
