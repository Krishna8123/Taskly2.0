package TaskManagerApp.Taskly.Repository;

import TaskManagerApp.Taskly.Model.Task;
import TaskManagerApp.Taskly.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Must be imported
import org.springframework.data.repository.query.Param; // Must be imported

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Get all tasks for a specific user
    List<Task> findByUser(User user);

    // Filter tasks by priority for a user
   /* List<Task> findByUserAndPriority(User user, String priority);

    // Filter tasks by category for a user
    List<Task> findByUserAndCategory(User user, String category);*/

    @Query("SELECT t FROM Task t WHERE t.user = :user AND LOWER(t.category) = LOWER(:category)")
    List<Task> findByUserAndCategory(User user, String category);

    @Query("SELECT t FROM Task t WHERE t.user = :user AND LOWER(t.priority) = LOWER(:priority)")
    List<Task> findByUserAndPriority(User user, String priority);


    // --- New Methods for Counting and Grouping ---

    // Counts tasks by priority (for sidebar badges)
    long countByUserAndPriority(User user, String priority);

    // Counts tasks by category (for sidebar badges)
    long countByUserAndCategory(User user, String category);

    // Finds tasks due on a specific date (for 'Due Today' logic)
    /*List<Task> findByUserAndDueDate(User user, LocalDate dueDate);*/
    List<Task> findByUserAndDueDateLessThanEqual(User user, LocalDate date);

    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.dueDate = :today AND DATE(t.createdOn) < :today")
    List<Task> findTasksDueTodayAndAddedBefore(@Param("user") User user, @Param("today") LocalDate today);



    // **CRITICAL FIX:** Native Query to force date-only comparison in MySQL
    // This is the method the TaskService calls.
    /*@Query(value = "SELECT t.* FROM tasks t WHERE t.user_id = :userId AND DATE(t.created_on) = :todayDate", nativeQuery = true)
    List<Task> findTasksAddedTodayNative(@Param("userId") Long userId, @Param("todayDate") LocalDate todayDate);*/

    /*@Query(value = "SELECT t.* FROM tasks t WHERE t.user_id = :userId AND DATE(t.createdOn) = :todayDate", nativeQuery = true)
    List<Task> findTasksAddedTodayNative(@Param("userId") Long userId, @Param("todayDate") LocalDate todayDate);*/
    @Query(value = "SELECT t.* FROM tasks t WHERE t.user_id = :userId AND DATE(t.created_on) = :todayDate", nativeQuery = true)
    List<Task> findTasksAddedTodayNative(@Param("userId") Long userId, @Param("todayDate") LocalDate todayDate);

    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.completed = false AND t.dueDate < :today")
    List<Task> findOverdueTasks(@Param("user") User user, @Param("today") LocalDate today);


    Optional<Task> findByIdAndUser(Long id, User user);

    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.createdOn BETWEEN :startOfDay AND :endOfDay ORDER BY t.createdOn DESC")
    List<Task> findTasksCreatedToday(@Param("user") User user,
                                     @Param("startOfDay") LocalDateTime startOfDay,
                                     @Param("endOfDay") LocalDateTime endOfDay);

    /*@Query("SELECT t FROM Task t WHERE t.user = :user AND t.dueDate = :today AND DATE(t.createdOn) < :today")
    List<Task> findTasksDueTodayOlder(@Param("user") User user, @Param("today") LocalDate today);*/

    @Query("""
    SELECT t FROM Task t 
    WHERE t.user = :user 
      AND t.dueDate = :today 
      AND t.createdOn < :startOfToday
    ORDER BY t.dueDate ASC
""")
    List<Task> findTasksDueTodayOlder(
            @Param("user") User user,
            @Param("today") LocalDate today,
            @Param("startOfToday") LocalDateTime startOfToday);



}
