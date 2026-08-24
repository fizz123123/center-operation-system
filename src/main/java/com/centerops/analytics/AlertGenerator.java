package com.centerops.analytics;

import com.centerops.entity.Alert;
import com.centerops.entity.Enrollment;
import com.centerops.entity.EnrollmentStatus;
import com.centerops.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * 根據修課狀態與開始日期，自動建立、更新或移除系統警示。
 *
 * <p>此類別只負責判斷警示內容與 priority；警示清單的排序由
 * {@code MaxHeap} 負責。自動警示以固定前綴識別，因此不會修改
 * {@code sample_data.sql} 建立的 Demo 警示。</p>
 */
@Component
@RequiredArgsConstructor
public class AlertGenerator {

    static final String AUTO_MESSAGE_PREFIX = "[AUTO] ";
    static final int MEDIUM_PRIORITY_DAYS = 30;
    static final int HIGH_PRIORITY_DAYS = 90;

    private final AlertRepository alertRepository;

    /**
     * 依照今天的日期同步指定修課紀錄所對應的自動警示。
     *
     * <ul>
     *     <li>NOT_STARTED：priority 1</li>
     *     <li>IN_PROGRESS 滿 30 天：priority 2</li>
     *     <li>IN_PROGRESS 滿 90 天：priority 3</li>
     *     <li>IN_PROGRESS 未滿 30 天或 COMPLETED：不保留警示</li>
     * </ul>
     *
     * @param enrollment 要檢查的修課紀錄
     */
    public void synchronize(Enrollment enrollment) {
        synchronize(enrollment, LocalDate.now());
    }

    void synchronize(Enrollment enrollment, LocalDate today) {
        Optional<Alert> existingAlert = findExistingAlert(enrollment);
        AlertRuleResult result = evaluate(enrollment, today);

        if (result == null) {
            existingAlert.ifPresent(alertRepository::delete);
            return;
        }

        Alert alert = existingAlert.orElseGet(() -> Alert.builder()
                .person(enrollment.getPerson())
                .course(enrollment.getCourse())
                .resolved(false)
                .build());
        alert.setPriority(result.priority());
        alert.setMessage(AUTO_MESSAGE_PREFIX + result.message());
        alert.setResolved(false);
        alertRepository.save(alert);
    }

    private Optional<Alert> findExistingAlert(Enrollment enrollment) {
        return alertRepository.findFirstByPerson_IdAndCourse_IdAndMessageStartingWith(
                enrollment.getPerson().getId(),
                enrollment.getCourse().getId(),
                AUTO_MESSAGE_PREFIX
        );
    }

    private AlertRuleResult evaluate(Enrollment enrollment, LocalDate today) {
        if (enrollment.getStatus() == EnrollmentStatus.NOT_STARTED) {
            return new AlertRuleResult(1, courseName(enrollment) + " has not started");
        }
        if (enrollment.getStatus() != EnrollmentStatus.IN_PROGRESS
                || enrollment.getStartDate() == null) {
            return null;
        }

        long elapsedDays = ChronoUnit.DAYS.between(enrollment.getStartDate(), today);
        if (elapsedDays >= HIGH_PRIORITY_DAYS) {
            return new AlertRuleResult(
                    3,
                    courseName(enrollment) + " has been in progress for " + elapsedDays + " days"
            );
        }
        if (elapsedDays >= MEDIUM_PRIORITY_DAYS) {
            return new AlertRuleResult(
                    2,
                    courseName(enrollment) + " has been in progress for " + elapsedDays + " days"
            );
        }
        return null;
    }

    private String courseName(Enrollment enrollment) {
        return enrollment.getCourse().getName();
    }

    private record AlertRuleResult(int priority, String message) {
    }
}
