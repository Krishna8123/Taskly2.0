package TaskManagerApp.Taskly.Controller;

import TaskManagerApp.Taskly.Model.Task;
import TaskManagerApp.Taskly.Model.User;
import TaskManagerApp.Taskly.Service.TaskService;
import TaskManagerApp.Taskly.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;



import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    private final UserService userService;
    //private final EmailService emailService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }




    /**
     * Retrieves all tasks for the authenticated user.
     * Uses findByUser from the updated TaskService.
     */
    @GetMapping
    public List<Task> getAllTasks(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        // Use the updated service method name
        return taskService.findByUser(user);
    }

    /**
     * Retrieves a single task by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        Optional<Task> task = taskService.getTaskById(id, user);
        return task.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new task.
     * The service handles setting the 'createdOn' and 'user' fields.
     */
    @PostMapping
    public Task createTask(@RequestBody Task task, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        return taskService.createTask(task, user);
    }

    /**
     * Updates an existing task by ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();

        // TaskService.updateTask returns the updated Optional<Task>
        Optional<Task> updatedTask = taskService.updateTask(id, task, user);
        return updatedTask.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes a task by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();

        // Attempt to get the task first to ensure it belongs to the user
        Optional<Task> taskToDelete = taskService.getTaskById(id, user);

        if (taskToDelete.isPresent()) {
            // TaskService.deleteTask handles the delete operation
            taskService.deleteTask(id, user);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    /**
     * Get tasks filtered by priority for the logged-in user
     */
    @GetMapping("/priority/{priority}")
    public List<Task> getTasksByPriority(@PathVariable String priority, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        return taskService.getTasksByPriority(user, priority);
    }

    /**
     * Get tasks filtered by category for the logged-in user
     */
    @GetMapping("/category/{category}")
    public List<Task> getTasksByCategory(@PathVariable String category, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        return taskService.getTasksByCategory(user, category);
    }


}
