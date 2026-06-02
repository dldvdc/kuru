package com.project.kuru.flow

import com.project.kuru.flow.video.SubmittedVideo

fun interface VideoStore {
    fun store(video: SubmittedVideo, objectKey: String)
}
