package com.cypherfund.bbn.dto;

import com.cypherfund.bbn.dao.entity.Event;
import com.cypherfund.bbn.utils.Enumerations;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link Event}
 */
@Data
public class EventDto implements Serializable {
    Integer id;
    String name;
    private Integer eventTypeTemplateId;
    Enumerations.EVENT_TYPE type;
    String outcome;
    Enumerations.Event_Status status;
    Instant createdAt;
    Instant updatedAt;
    Instant eventDate;
    Long tournamentId;
}
