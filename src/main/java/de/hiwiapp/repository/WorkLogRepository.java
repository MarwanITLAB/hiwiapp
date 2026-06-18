package de.hiwiapp.repository;

import de.hiwiapp.model.User;
import de.hiwiapp.model.WorkLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {

    // Diese Zeile musst du hinzufügen:
    List<WorkLog> findByUser(User user);
}