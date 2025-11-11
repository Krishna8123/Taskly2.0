package TaskManagerApp.Taskly.Service;

        import org.springframework.stereotype.Service;

        import java.time.*;
        import java.time.format.DateTimeFormatter;
        import java.util.Locale;
        import java.util.regex.Matcher;
        import java.util.regex.Pattern;

@Service
public class NLPService {

    // Regex pattern for time like "5pm", "10:30 am", etc.
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(\\d{1,2})(?::(\\d{2}))?\\s?(am|pm)?",
            Pattern.CASE_INSENSITIVE
    );

    // Regex for days like "Monday", "next Friday"
    private static final Pattern DAY_PATTERN = Pattern.compile(
            "(today|tomorrow|next\\s+\\w+|\\w+day)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Parses a user's natural task input and extracts title, due date, and time.
     *
     * Examples:
     *  - "Submit report tomorrow 5pm"
     *  - "Meeting with mentor next monday"
     */
    public ParsedTask parseTaskDescription(String input) {
        if (input == null || input.isBlank()) {
            return new ParsedTask(null, null, null);
        }

        String text = input.trim().toLowerCase(Locale.ENGLISH);

        LocalDate date = null;
        LocalTime time = null;

        // --- 1️⃣ Extract Date ---
        Matcher dayMatcher = DAY_PATTERN.matcher(text);
        if (dayMatcher.find()) {
            String day = dayMatcher.group(1);
            date = interpretDate(day);
            text = text.replace(day, "").trim(); // remove matched phrase from title
        }

        // --- 2️⃣ Extract Time ---
        Matcher timeMatcher = TIME_PATTERN.matcher(text);
        if (timeMatcher.find()) {
            int hour = Integer.parseInt(timeMatcher.group(1));
            int minute = (timeMatcher.group(2) != null) ? Integer.parseInt(timeMatcher.group(2)) : 0;
            String ampm = timeMatcher.group(3);

            if (ampm != null) {
                if (ampm.equalsIgnoreCase("pm") && hour < 12) hour += 12;
                else if (ampm.equalsIgnoreCase("am") && hour == 12) hour = 0;
            }

            time = LocalTime.of(hour, minute);
            text = text.replace(timeMatcher.group(), "").trim(); // remove time from title
        }

        // --- 3️⃣ Detect Category ---
        String category = detectCategory(text);

        // --- 4️⃣ Clean title ---
        String title = cleanTitle(text);

        // --- 5️⃣ Construct ParsedTask ---
        LocalDateTime dateTime = null;
        if (date != null && time != null)
            dateTime = LocalDateTime.of(date, time);
        else if (date != null)
            dateTime = date.atStartOfDay();

        return new ParsedTask(capitalize(title), dateTime, category);
    }

    private LocalDate interpretDate(String phrase) {
        LocalDate today = LocalDate.now();
        phrase = phrase.toLowerCase();

        if (phrase.contains("today")) {
            return today;
        } else if (phrase.contains("tomorrow")) {
            return today.plusDays(1);
        } else if (phrase.startsWith("next")) {
            // Example: "next monday"
            String[] parts = phrase.split(" ");
            if (parts.length > 1) {
                return nextDayOfWeek(parts[1]);
            }
        } else {
            // Example: "monday", "friday"
            return nextDayOfWeek(phrase);
        }

        return null;
    }

    private LocalDate nextDayOfWeek(String dayName) {
        try {
            DayOfWeek targetDay = DayOfWeek.valueOf(dayName.toUpperCase(Locale.ENGLISH));
            LocalDate today = LocalDate.now();
            int daysUntil = (targetDay.getValue() - today.getDayOfWeek().getValue() + 7) % 7;
            return daysUntil == 0 ? today.plusWeeks(1) : today.plusDays(daysUntil);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String detectCategory(String input) {
        if (input == null) return "Personal";
        input = input.toLowerCase();

        if (input.contains("project") || input.contains("meeting") || input.contains("work"))
            return "Work";
        if (input.contains("bill") || input.contains("payment") || input.contains("budget"))
            return "Finance";
        if (input.contains("doctor") || input.contains("gym") || input.contains("health"))
            return "Health";
        if (input.contains("study") || input.contains("assignment"))
            return "Study";

        return "Personal";
    }
    private String cleanTitle(String text) {
        return text
                // Remove date-related words
                .replaceAll("\\b(today|tomorrow|next\\s+\\w+|\\w+day)\\b", "")
                // Remove time-related patterns (e.g., 10am, 5:30 pm)
                .replaceAll("\\b(\\d{1,2})(?::\\d{2})?\\s?(am|pm)?\\b", "")
                // Remove connectors like "by", "at", "on", "after", "in"
                .replaceAll("\\b(by|at|on|after|in)\\b", "")
                // Clean extra spaces
                .replaceAll("\\s+", " ")
                .trim();
    }


    private String capitalize(String str) {
        if (str == null || str.isBlank()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
