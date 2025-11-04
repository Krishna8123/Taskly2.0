package TaskManagerApp.Taskly.Repository;

        import TaskManagerApp.Taskly.Model.User;
        import org.springframework.data.jpa.repository.JpaRepository;
        import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find a user by username (for login)
    Optional<User> findByUsername(String username);

    // Find a user by email (for registration or notifications)
    Optional<User> findByEmail(String email);

    // Check if username exists
    boolean existsByUsername(String username);

    // Check if email exists
    boolean existsByEmail(String email);
}
