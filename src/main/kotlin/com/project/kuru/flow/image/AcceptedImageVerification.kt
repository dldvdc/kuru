package com.project.kuru.flow.image

import com.project.kuru.core.image.VerifiedImage

fun AcceptedImage.toVerifiedImage(): VerifiedImage =
    VerifiedImage(
        originalFileName = originalFileName,
        mime = mime,
        extension = extension,
        sizeBytes = sizeBytes,
        contentSha256 = contentSha256,
        dimensions = dimensions,
        color = color,
        animated = animated,
    )
