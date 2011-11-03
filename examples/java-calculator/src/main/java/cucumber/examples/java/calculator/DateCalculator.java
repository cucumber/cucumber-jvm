package cucumber.examples.java.calculator;

import java.util.Date;

public class DateCalculator {
	
	private Date now;

	public DateCalculator(Date now) {
		super();
		this.now = now;
	}

	public String isDateInThePast(Date date) {
		if(date.before(now))
			return "yes";
		else
			return "no";
	}
	
	

}
