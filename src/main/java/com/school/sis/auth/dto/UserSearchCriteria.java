package com.school.sis.auth.dto;

import java.util.UUID;

public record UserSearchCriteria(String search, UUID roleId, UUID facultyId, Boolean active) {}
