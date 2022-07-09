package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.Tag
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TagRepository : JpaRepository<Tag, UUID>
