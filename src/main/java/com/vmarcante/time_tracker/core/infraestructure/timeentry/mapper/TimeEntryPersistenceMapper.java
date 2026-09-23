package com.vmarcante.time_tracker.core.infraestructure.timeentry.mapper;

import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntry;
import com.vmarcante.time_tracker.core.domain.timeentry.model.TimeEntrySession;
import com.vmarcante.time_tracker.core.infraestructure.timeentry.persistence.TagJpaEntity;
import com.vmarcante.time_tracker.core.infraestructure.timeentry.persistence.TimeEntryJpaEntity;
import com.vmarcante.time_tracker.core.infraestructure.timeentry.persistence.TimeEntrySessionJpaEntity;
import com.vmarcante.time_tracker.core.infraestructure.timeentry.persistence.TimeEntryTagJpaEntity;

public class TimeEntryPersistenceMapper {

    public static TimeEntry toDomain(TimeEntryJpaEntity entity) {
        TimeEntry entry = new TimeEntry();
        entry.setId(entity.getId());
        entry.setSeqId(entity.getSeqId());
        entry.setUserId(entity.getUserId());
        entry.setCompanyId(entity.getCompanyId());
        entry.setProjectId(entity.getProjectId());
        entry.setName(entity.getName());
        entry.setDescription(entity.getDescription());
        entry.setActive(entity.getActive());
        entry.setCreatedAt(entity.getCreatedAt());
        entry.setUpdatedAt(entity.getUpdatedAt());
        entry.setCreatedBy(entity.getCreatedBy());
        entry.setUpdatedBy(entity.getUpdatedBy());
        return entry;
    }

    public static TimeEntryJpaEntity toEntity(TimeEntry entry) {
        TimeEntryJpaEntity entity = new TimeEntryJpaEntity();
        entity.setId(entry.getId());
        entity.setUserId(entry.getUserId());
        entity.setCompanyId(entry.getCompanyId());
        entity.setProjectId(entry.getProjectId());
        entity.setName(entry.getName());
        entity.setDescription(entry.getDescription());
        entity.setActive(entry.getActive());
        entity.setCreatedBy(entry.getCreatedBy());
        entity.setUpdatedBy(entry.getUpdatedBy());
        return entity;
    }

    public static TimeEntrySession toDomain(TimeEntrySessionJpaEntity entity) {
        TimeEntrySession session = new TimeEntrySession();
        session.setId(entity.getId());
        session.setSeqId(entity.getSeqId());
        session.setEntryId(entity.getEntryId());
        session.setUserId(entity.getUserId());
        session.setStartTime(entity.getStartTime());
        session.setEndTime(entity.getEndTime());
        session.setDescription(entity.getDescription());
        session.setActive(entity.getActive());
        session.setCreatedAt(entity.getCreatedAt());
        session.setUpdatedAt(entity.getUpdatedAt());
        session.setCreatedBy(entity.getCreatedBy());
        session.setUpdatedBy(entity.getUpdatedBy());
        return session;
    }

    public static TimeEntrySessionJpaEntity toEntity(TimeEntrySession session) {
        TimeEntrySessionJpaEntity entity = new TimeEntrySessionJpaEntity();
        entity.setId(session.getId());
        entity.setEntryId(session.getEntryId());
        entity.setUserId(session.getUserId());
        entity.setStartTime(session.getStartTime());
        entity.setEndTime(session.getEndTime());
        entity.setDescription(session.getDescription());
        entity.setActive(session.getActive());
        entity.setCreatedBy(session.getCreatedBy());
        entity.setUpdatedBy(session.getUpdatedBy());
        return entity;
    }

    public static Tag toDomain(TagJpaEntity entity) {
        Tag tag = new Tag();
        tag.setId(entity.getId());
        tag.setSeqId(entity.getSeqId());
        tag.setUserId(entity.getUserId());
        tag.setName(entity.getName());
        tag.setColor(entity.getColor());
        tag.setActive(entity.getActive());
        tag.setCreatedAt(entity.getCreatedAt());
        tag.setUpdatedAt(entity.getUpdatedAt());
        tag.setCreatedBy(entity.getCreatedBy());
        tag.setUpdatedBy(entity.getUpdatedBy());
        return tag;
    }

    public static TagJpaEntity toEntity(Tag tag) {
        TagJpaEntity entity = new TagJpaEntity();
        entity.setId(tag.getId());
        entity.setUserId(tag.getUserId());
        entity.setName(tag.getName());
        entity.setColor(tag.getColor());
        entity.setActive(tag.getActive());
        entity.setCreatedBy(tag.getCreatedBy());
        entity.setUpdatedBy(tag.getUpdatedBy());
        return entity;
    }

    public static TimeEntryTagJpaEntity toLinkEntity(UUID entryId, UUID tagId, UUID actorId) {
        TimeEntryTagJpaEntity entity = new TimeEntryTagJpaEntity();
        entity.setEntryId(entryId);
        entity.setTagId(tagId);
        entity.setActive(true);
        entity.setCreatedBy(actorId);
        entity.setUpdatedBy(actorId);
        return entity;
    }
}
