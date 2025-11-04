package TaskManagerApp.Taskly.Controller;

        import TaskManagerApp.Taskly.Service.EmailService;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.RequestParam;
        import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailTestController {

    private final EmailService emailService;

    // Constructor injection
    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Test email endpoint.
     * Example: http://localhost:8082/send-test-email?to=someone@gmail.com
     */
    @GetMapping("/send-test-email")
    public String sendTestEmail(@RequestParam String to) {
        try {
            String subject = "✅ Taskly Email Integration Test";
            String body = """
                    Hey there 👋,
                    
                    This is a test email sent from your Taskly application!
                    Your mail setup is working perfectly with Spring Boot. 🚀
                    
                    — Taskly Notifications
                    """;

            emailService.sendEmail(to, subject, body);
            return "✅ Email sent successfully to: " + to;
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Failed to send email: " + e.getMessage();
        }
    }
}
