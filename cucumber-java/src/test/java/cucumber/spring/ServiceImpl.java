package cucumber.spring;

import org.springframework.stereotype.Service;

@Service
public class ServiceImpl implements SpringService {

	public String hello() {
		return "Have a cuke, Duke";
	}

}
