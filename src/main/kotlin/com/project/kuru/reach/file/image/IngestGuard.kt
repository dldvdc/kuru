package com.project.kuru.reach.file.image

import com.project.kuru.core.Dimension

/** Garde-fous techniques à l’ingest (reach uniquement). */
data class IngestGuard(
    val dimensions: Dimension,
)
