package io.cucumber.spring.commonglue;

import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("DesignForExtension")
public class AutowiresThirdStepDef {

    @Autowired
    private ThirdStepDef thirdStepDef;

    public ThirdStepDef getThirdStepDef() {
        return thirdStepDef;
    }

}
