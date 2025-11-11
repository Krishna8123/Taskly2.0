package TaskManagerApp.Taskly.Controller;

import TaskManagerApp.Taskly.Model.User;
import TaskManagerApp.Taskly.Service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ✅ Login page
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // ✅ Registration page
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // ✅ Handle registration form submission
    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam(required = false) String email,
                               RedirectAttributes redirectAttributes) {

        try {
            // Check for duplicate username
            if (userService.usernameExists(username)) {
                redirectAttributes.addFlashAttribute("error", "⚠️ Username already exists. Please choose another one.");
                return "redirect:/register";
            }

            // Check for duplicate email
            if (email != null && userService.emailExists(email)) {
                redirectAttributes.addFlashAttribute("error", "⚠️ This email is already registered. Try logging in or use another email.");
                return "redirect:/register";
            }

            // Register user
            boolean success = userService.registerUser(username, password, email);

            if (success) {
                redirectAttributes.addFlashAttribute("success", "🎉 Registration successful! You can now log in.");
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("error", "❌ Something went wrong. Please try again.");
                return "redirect:/register";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Unexpected error: " + e.getMessage());
            return "redirect:/register";
        }
    }
}
