package TaskManagerApp.Taskly.Service;

        import java.time.LocalDateTime;

public record ParsedTask(String title, LocalDateTime dueDateTime, String category) {}

