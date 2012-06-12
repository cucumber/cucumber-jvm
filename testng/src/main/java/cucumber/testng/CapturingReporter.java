package cucumber.testng;

import gherkin.formatter.Reporter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CapturingReporter implements Reporter {
    List<Result> results = new ArrayList<Result>();
    public void result(Result result) {
      results.add(result);
    }
    public Throwable failed(){
      for (Result result : results){
        if (Result.FAILED.equals(result.getStatus())){
          return result.getError();
        }
      }
      return null;
    }
    public Throwable skips(){
      for (Result result : results){
        if (Result.SKIPPED.equals(result.getStatus())){
          return result.getError();
        }
      }
      return null;
    }
    public void match(Match match) {}
    public void embedding(String mimeType, InputStream data) {}
    public void write(String text) {}
	public void after(Match arg0, Result arg1) {}
	public void before(Match arg0, Result arg1) {}
}

