package com.csbroker.apiserver.service

import org.springframework.web.multipart.MultipartFile

interface S3Service {
    suspend fun uploadProfileImg(multipartFile: MultipartFile) : String
}
