package com.ccat.api.scheduler;

import com.ccat.api.model.entity.User;
import com.ccat.api.repository.UserRepository;
import com.ccat.api.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Sends an inactivity reminder to users who haven't started a session
 * in the last INACTIVITY_DAYS days. Runs every day at 10:00 AM server time.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private static final int INACTIVITY_DAYS = 3;

    private final UserRepository userRepository;
    private final EmailService   emailService;

    @Scheduled(cron = "0 0 10 * * *")
    public void sendInactivityReminders() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(INACTIVITY_DAYS);
        List<User> inactive  = userRepository.findInactiveUsersSince(cutoff);

        log.info("Reminder scheduler: {} inactive user(s) found (cutoff={})", inactive.size(), cutoff);

        for (User user : inactive) {
            try {
                // Compute actual days since last session (approximate via cutoff boundary)
                int daysSince = INACTIVITY_DAYS;
                emailService.sendReminderEmail(user.getStrEmail(), user.getStrFirstName(), daysSince);
            } catch (Exception e) {
                log.warn("Failed to queue reminder for {}: {}", user.getStrEmail(), e.getMessage());
            }
        }
    }
}
