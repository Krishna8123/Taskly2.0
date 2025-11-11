package TaskManagerApp.Taskly.Controller;

import TaskManagerApp.Taskly.Model.Task;
import TaskManagerApp.Taskly.Model.User;
import TaskManagerApp.Taskly.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import TaskManagerApp.Taskly.Service.EmailService;

@Controller
public class TaskViewController {

    private final TaskService taskService;
    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    private NLPService nlpService;




    public TaskViewController(TaskService taskService, UserService userService, EmailService emailService) {
        this.taskService = taskService;
        this.userService = userService;
        this.emailService = emailService;
    }
    // Redirect root URL
    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/home";
    }

    // Show tasks for logged-in user with filtering and grouping logic
    @GetMapping("/home")
    public String home(
            Model model,
            Authentication authentication,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String category) {

        model.addAttribute("username", authentication.getName());
        User user = userService.findByUsername(authentication.getName()).orElseThrow();

        LocalDate today = LocalDate.now();

        if (priority != null && !priority.isEmpty()) {
            List<Task> tasks = taskService.getTasksByPriority(user, priority);
            model.addAttribute("tasksFiltered", tasks);
            model.addAttribute("viewMode", "Filtered");
            model.addAttribute("filterType", priority + " Priority");
        } else if (category != null && !category.isEmpty()) {
            List<Task> tasks = taskService.getTasksByCategory(user, category);
            model.addAttribute("tasksFiltered", tasks);
            model.addAttribute("viewMode", "Filtered");
            model.addAttribute("filterType", category + " Tasks");
        } else {
            List<Task> tasksDueTodayOlder = taskService.getTasksDueTodayOlder(user, today);
            List<Task> tasksAddedToday = taskService.getTasksAddedToday(user, today);
            List<Task> overdueTasks = taskService.getOverdueTasks(user, today);


            model.addAttribute("tasksDueTodayOlder", tasksDueTodayOlder);
            model.addAttribute("tasksAddedToday", tasksAddedToday);
            model.addAttribute("overdueTasks", overdueTasks);
            model.addAttribute("viewMode", "Grouped");
        }

        model.addAttribute("highPriorityCount", taskService.countTasksByPriority(user, "High"));
        model.addAttribute("mediumPriorityCount", taskService.countTasksByPriority(user, "Medium"));
        model.addAttribute("lowPriorityCount", taskService.countTasksByPriority(user, "Low"));
        model.addAttribute("workCategoryCount", taskService.countTasksByCategory(user, "Work"));
        model.addAttribute("personalCategoryCount", taskService.countTasksByCategory(user, "Personal"));

        return "tasks";
    }


    // Show add task form
    @GetMapping("/tasks/add")
    public String showAddTaskForm(Model model, Authentication authentication) {
        // 1️⃣ Add username
        model.addAttribute("username", authentication.getName());

        // 2️⃣ Get the logged-in user
        User user = userService.findByUsername(authentication.getName()).orElseThrow();

        // 3️⃣ Add a new empty task object for the form
        model.addAttribute("task", new Task());

        // 4️⃣ Add sidebar counts (same as in /home)
        model.addAttribute("highPriorityCount", taskService.countTasksByPriority(user, "High"));
        model.addAttribute("mediumPriorityCount", taskService.countTasksByPriority(user, "Medium"));
        model.addAttribute("lowPriorityCount", taskService.countTasksByPriority(user, "Low"));
        model.addAttribute("workCategoryCount", taskService.countTasksByCategory(user, "Work"));
        model.addAttribute("personalCategoryCount", taskService.countTasksByCategory(user, "Personal"));

        // 5️⃣ Return the view
        return "add-task";
    }


    // Add new task


    @PostMapping("/tasks/add")
    public String addTask(@ModelAttribute Task task, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();

        // 🔍 Check if NLP should analyze (e.g., no due date or "natural" sentence)
        if ((task.getDueDate() == null || task.getDueTime() == null) &&
                task.getTitle() != null && task.getTitle().matches(".*\\b(tomorrow|today|next|at|pm|am|after|in)\\b.*")) {

            System.out.println("🧠 Running NLP on input: " + task.getTitle());
            ParsedTask parsed = nlpService.parseTaskDescription(task.getTitle());

            if (parsed != null) {
                if (parsed.title() != null && !parsed.title().isBlank()) {
                    task.setTitle(parsed.title());
                }
                if (parsed.dueDateTime() != null) {
                    task.setDueDate(parsed.dueDateTime().toLocalDate());
                    task.setDueTime(parsed.dueDateTime().toLocalTime());
                }
                if (parsed.category() != null) {
                    task.setCategory(parsed.category());
                }
            }
        }

        // 💾 Save the task normally
        Task saved = taskService.createTask(task, user);

        // 📧 Send email confirmation
        // Format due date and time together
        String dueDateTimeStr;
        if (saved.getDueDate() != null) {
            if (saved.getDueTime() != null) {
                dueDateTimeStr = saved.getDueDate().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"))
                        + " at " + saved.getDueTime().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
            } else {
                dueDateTimeStr = saved.getDueDate().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));
            }
        } else {
            dueDateTimeStr = "No due date";
        }


        String priorityStr = saved.getPriority() != null ? saved.getPriority() : "Not specified";
        String categoryStr = saved.getCategory() != null ? saved.getCategory() : "Not specified";

        String subject = "📝 New Task Added: " + (saved.getTitle() != null ? saved.getTitle() : "Untitled Task");
        String body = String.format("""
        Hello %s 👋,

        You’ve successfully added a new task to your Taskly dashboard!

        📌 Title: %s
        🗓️ Due: %s
        ⏰ Priority: %s
        🗂️ Category: %s

        Keep up the productivity! 🚀

        — Taskly Notifications
        """,
                user.getUsername(),
                saved.getTitle() != null ? saved.getTitle() : "Untitled Task",
                dueDateTimeStr,
                priorityStr,
                categoryStr
        );

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            try {
                emailService.sendEmail(user.getEmail(), subject, body);
                System.out.println("✅ Task creation email sent to: " + user.getEmail());
            } catch (Exception e) {
                System.err.println("❌ Failed to send task creation email: " + e.getMessage());
            }
        }

        return "redirect:/home";
    }


    // Delete task
    @PostMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        taskService.deleteTask(id, user);
        return "redirect:/home";
    }

    // Toggle completion
    @PostMapping("/tasks/toggle/{id}")
    public String toggleTask(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        taskService.getTaskById(id, user).ifPresent(task -> {
            task.setCompleted(!task.isCompleted());
            taskService.updateTask(id, task, user);
        });
        return "redirect:/home";
    }

    // Show edit form

    @GetMapping("/tasks/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        Optional<Task> taskOptional = taskService.getTaskById(id, user);

        if (taskOptional.isEmpty()) {
            return "redirect:/home";
        }

        // ✅ Add the task to the model
        model.addAttribute("task", taskOptional.get());

        // ✅ Add sidebar counts (for Priority and Category)
        model.addAttribute("highPriorityCount", taskService.countTasksByPriority(user, "High"));
        model.addAttribute("mediumPriorityCount", taskService.countTasksByPriority(user, "Medium"));
        model.addAttribute("lowPriorityCount", taskService.countTasksByPriority(user, "Low"));
        model.addAttribute("workCategoryCount", taskService.countTasksByCategory(user, "Work"));
        model.addAttribute("personalCategoryCount", taskService.countTasksByCategory(user, "Personal"));

        return "edit-task";
    }



    // Submit edited task

    @PostMapping("/tasks/edit/{id}")
    public String updateTask(@PathVariable Long id,
                             @ModelAttribute("task") Task updatedTask,
                             Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElseThrow();

        // Fetch existing task for this user
        Optional<Task> existingTaskOptional = taskService.getTaskById(id, user);
        if (existingTaskOptional.isEmpty()) {
            return "redirect:/home";
        }

        Task existingTask = existingTaskOptional.get();

        // ✅ Update only editable fields
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setDueDate(updatedTask.getDueDate());
        existingTask.setDueTime(updatedTask.getDueTime());
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setCategory(updatedTask.getCategory());
        existingTask.setCompleted(updatedTask.isCompleted());

        // ✅ Save the updated task
        taskService.saveTask(existingTask);

        // Redirect back to home after saving
        return "redirect:/home";
    }

}