package io.cucumber.examples.spring.txn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model) {
        Optional<User> user = userRepository.findById(id);

        if (!user.isPresent()) {
            throw new IllegalArgumentException("" + id);
        }

        model.addAttribute("user", user.get());

        return "user";
    }

}
