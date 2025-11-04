package TaskManagerApp.Taskly.Service;

import TaskManagerApp.Taskly.Model.Task;
import TaskManagerApp.Taskly.Model.User;
import TaskManagerApp.Taskly.Repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // --- CRUD Operations ---

    public Task createTask(Task task, User user) {
        task.setUser(user);

        // CRITICAL: Set the exact time of creation now.
        if (task.getCreatedOn() == null) {
            task.setCreatedOn(LocalDateTime.now());
        }

        return taskRepository.save(task);
    }

    public Optional<Task> getTaskById(Long id, User user) {
        return taskRepository.findById(id).filter(task -> task.getUser().equals(user));
    }

    public List<Task> findByUser(User user) {
        return taskRepository.findByUser(user);
    }

    public Optional<Task> updateTask(Long id, Task updatedTask, User user) {
        return getTaskById(id, user).map(task -> {
            task.setTitle(updatedTask.getTitle());
            task.setDescription(updatedTask.getDescription());
            task.setDueDate(updatedTask.getDueDate());
            task.setDueTime(updatedTask.getDueTime());
            task.setCompleted(updatedTask.isCompleted());
            task.setPriority(updatedTask.getPriority());
            task.setCategory(updatedTask.getCategory());
            return taskRepository.save(task);
        });
    }

    public void deleteTask(Long id, User user) {
        getTaskById(id, user).ifPresent(taskRepository::delete);
    }

    // --- Filtering and Counting Methods (remain unchanged) ---

    public List<Task> getTasksByPriority(User user, String priority) {
        return taskRepository.findByUserAndPriority(user, priority);
    }

    public List<Task> getTasksByCategory(User user, String category) {
        return taskRepository.findByUserAndCategory(user, category);
    }

    public long countTasksByPriority(User user, String priority) {
        return taskRepository.countByUserAndPriority(user, priority);
    }

    public long countTasksByCategory(User user, String category) {
        return taskRepository.countByUserAndCategory(user, category);
    }

    // --- Grouping Methods ---

/*
    public List<Task> getTasksDueTodayOlder(User user, LocalDate today) {
        return taskRepository.findByUserAndDueDateLessThanEqual(user, today);
    }*/

    /*public List<Task> getTasksDueTodayOlder(User user, LocalDate today) {
        return taskRepository.findTasksDueTodayOlder(user, today);
    }*/

    public List<Task> getTasksDueTodayOlder(User user, LocalDate today) {
        LocalDateTime startOfToday = today.atStartOfDay();
        return taskRepository.findTasksDueTodayOlder(user, today, startOfToday);
    }



    /**
     * Finds tasks created today, regardless of due date.
     * CRITICAL FIX: Calls the Native SQL Query for time zone reliability.
     */
/*
    public List<Task> getTasksAddedToday(User user, LocalDate today) {
        return taskRepository.findTasksAddedTodayNative(user.getId(), today);
    }*/

    public List<Task> getTasksAddedToday(User user, LocalDate today) {
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        return taskRepository.findTasksCreatedToday(user, startOfDay, endOfDay);
    }

    public List<Task> getOverdueTasks(User user, LocalDate today) {
        return taskRepository.findOverdueTasks(user, today);
    }

    public void saveTask(Task task) {
        taskRepository.save(task);
    }


}
