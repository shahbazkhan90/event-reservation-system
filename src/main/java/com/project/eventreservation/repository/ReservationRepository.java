    package com.project.eventreservation.repository;

    import com.project.eventreservation.model.Event;
    import com.project.eventreservation.model.Reservation;
    import jakarta.persistence.LockModeType;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Lock;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;
    import org.springframework.stereotype.Repository;

    import java.util.Optional;

    @Repository
    public interface ReservationRepository extends JpaRepository<Reservation,Long> {
        @Query("Select COALESCE(SUM(r.seatsBooked),0) from Reservation r where r.eventId=:eventId and r.status=:status")
        Integer sumSeatBookedByEventIdAndStatus(@Param("eventId") Long eventId,@Param("status") String status);

//        @Lock(LockModeType.PESSIMISTIC_WRITE)


    }
