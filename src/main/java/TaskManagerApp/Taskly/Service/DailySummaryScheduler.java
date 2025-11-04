package TaskManagerApp.Taskly.Service;

        import TaskManagerApp.Taskly.Model.Task;
        import TaskManagerApp.Taskly.Model.User;
        import TaskManagerApp.Taskly.Repository.TaskRepository;
        import TaskManagerApp.Taskly.Repository.UserRepository;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.scheduling.annotation.Scheduled;
        import org.springframework.stereotype.Service;

        import java.time.LocalDate;
        import java.util.List;

@Service
public class DailySummaryScheduler {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Scheduled to run every morning at 9:00 AM (IST)
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata")
    public void sendDailySummaryReport() {
        LocalDate today = LocalDate.now();
        List<User> users = userRepository.findAll();

        for (User user : users) {
            List<Task> userTasks = taskRepository.findByUser(user);

            long completedCount = userTasks.stream().filter(Task::isCompleted).count();
            long overdueCount = userTasks.stream()
                    .filter(t -> !t.isCompleted() && t.getDueDate() != null && t.getDueDate().isBefore(today))
                    .count();
            long remainingCount = userTasks.stream()
                    .filter(t -> !t.isCompleted() && (t.getDueDate() == null || !t.getDueDate().isBefore(today)))
                    .count();

            String subject = "📊 Your Daily Task Summary - " + today;
            String body = String.format("""
                    Good morning %s 👋
                    
                    Here’s your daily productivity summary for %s:
                    
                    ✅ Completed Tasks: %d
                    ⏳ Remaining Tasks: %d
                    ⚠️ Overdue Tasks: %d
                    
                    Stay productive and crush your goals today 💪
                    — Your Taskly Assistant
                    """,
                    user.getUsername(), today, completedCount, remainingCount, overdueCount);

            // Send the email
            emailService.sendEmail(user.getEmail(), subject, body);
        }

        System.out.println("✅ Daily summary emails sent successfully at 9 AM.");
    }
}
