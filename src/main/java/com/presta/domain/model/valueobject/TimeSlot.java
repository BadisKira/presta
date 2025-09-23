package com.presta.domain.model.valueobject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public record TimeSlot(
        LocalDateTime startDateTime,
        int duration // en minutes
) {
    public TimeSlot {
        if (startDateTime == null) {
            throw new IllegalArgumentException("La date/heure de début est obligatoire");
        }
        if (duration <= 0 || duration > 480) {
            throw new IllegalArgumentException(
                    "La durée doit être entre 1 et 480 minutes"
            );
        }
    }

    public LocalDateTime getEndDateTime() {
        return startDateTime.plusMinutes(duration);
    }

    public boolean isInPast() {
        return startDateTime.isBefore(LocalDateTime.now());
    }

    public boolean isInFuture() {
        return startDateTime.isAfter(LocalDateTime.now());
    }

     public Boolean isInMoreThan24h(){
         LocalDateTime startDateTime = this.getEndDateTime().minus(Duration.ofMinutes(duration()));
         return LocalDateTime.now().isAfter(startDateTime.minusHours(24));
     }

    public boolean contains(LocalDateTime dateTime) {
        return !dateTime.isBefore(startDateTime) && !dateTime.isAfter(getEndDateTime());
    }

    public boolean overlaps(TimeSlot other) {
        return !this.getEndDateTime().isBefore(other.startDateTime) &&
                !other.getEndDateTime().isBefore(this.startDateTime);
    }

    public boolean overlaps(LocalDateTime otherStart, int otherDuration) {
        LocalDateTime otherEnd = otherStart.plusMinutes(otherDuration);
        return !this.getEndDateTime().isBefore(otherStart) &&
                !otherEnd.isBefore(this.startDateTime);
    }
}

//public record TimeSlot(
//        UUID contractorId,
//        LocalDateTime startDateTime,
//        LocalDateTime endDateTime,
//        int duration,
//        boolean isAvailable
//) {
//    public TimeSlot {
//        if (contractorId == null || startDateTime == null || endDateTime == null) {
//            throw new IllegalArgumentException("Tous les champs sont obligatoires");
//        }
//        if (!endDateTime.isAfter(startDateTime)) {
//            throw new IllegalArgumentException("La fin doit être après le début");
//        }
//        if (duration <= 0) {
//            throw new IllegalArgumentException("La durée doit être positive");
//        }
//    }
//
//    public boolean contains(LocalDateTime dateTime) {
//        return !dateTime.isBefore(startDateTime) && !dateTime.isAfter(endDateTime);
//    }
//
//    public boolean overlaps(TimeSlot other) {
//        return !this.endDateTime.isBefore(other.startDateTime) &&
//                !other.endDateTime.isBefore(this.startDateTime);
//    }
//}

