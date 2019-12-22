package com.flover.ocrapplication

object OCRData {
    const val localhost: String = "192.168.1.101"
    const val uploadPostUrl: String = "http://$localhost:3000/api/results"
    const val getUrl: String = "http://$localhost:3000/api/image_results"
    const val ocrPostUrl: String = "http://$localhost:3000/api/result"
}