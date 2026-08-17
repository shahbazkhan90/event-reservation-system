    package com.project.eventreservation.repository;

    import com.project.eventreservation.model.Reservation;
    import jakarta.persistence.LockModeType;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Lock;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;
    import org.springframework.stereotype.Repository;

    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.Optional;

    @Repository
    public interface ReservationRepository extends JpaRepository<Reservation,Long> {

        @Query("SELECT COALESCE(SUM(r.seatsBooked), 0) FROM Reservation r WHERE r.eventId = :eventId AND r.status IN :statuses")
        Integer sumSeatBookedByEventIdAndStatuses(@Param("eventId") Long eventId, @Param("statuses") List<String> statuses);

        @Query("SELECT r FROM Reservation r WHERE r.status = 'PENDING' AND r.createdAt < :cutoffTime")
        List<Reservation> findExpiredPendingReservations(@Param("cutoffTime") LocalDateTime cutoffTime);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT r FROM Reservation r WHERE r.id = :id")
        Optional<Reservation> findByIdWithLock(@Param("id") Long id);


    }
