package TaskManagerApp.Taskly.Controller;

        import TaskManagerApp.Taskly.Model.User;
        import TaskManagerApp.Taskly.Service.UserService;
        import org.springframework.stereotype.Controller;
        import org.springframework.ui.Model;
        import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Login page
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Thymeleaf template login.html
    }

    // Registration page
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register"; // Thymeleaf template register.html
    }

    // Handle registration form submission
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        // Check if username or email already exists
        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }

        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("error", "Email already exists");
            return "register";
        }

        // Register the user
        userService.registerUser(user);

        // Redirect to login with a "registered" flag
        return "redirect:/login?registered";
    }
}

