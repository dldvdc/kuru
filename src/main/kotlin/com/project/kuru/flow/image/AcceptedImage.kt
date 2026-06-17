package com.project.kuru.flow.image

data class AcceptedImage(
    val stagingKey: String,
    val entry: CatalogEntry,
) {
    init {
        require(stagingKey.isNotBlank()) { "Clé staging manquante" }
    }
}
