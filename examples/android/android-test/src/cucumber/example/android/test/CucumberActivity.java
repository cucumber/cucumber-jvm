package cucumber.example.android.test;

import android.app.Activity;
import android.os.Bundle;
import cucumber.android.test.R;

public class CucumberActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}
