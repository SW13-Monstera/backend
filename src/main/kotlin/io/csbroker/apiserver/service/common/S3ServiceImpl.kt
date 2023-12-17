package io.csbroker.apiserver.service.common

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.ObjectCannedAcl
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class S3ServiceImpl(
    private val s3Client: S3Client,
    @Value("\${aws.s3-bucket}")
    private val bucketName: String,
) : S3Service {
    override suspend fun uploadProfileImg(multipartFile: MultipartFile): String {
        val s3FileName = createS3FileName(multipartFile)
        s3Client.putObject(
            PutObjectRequest {
                bucket = bucketName
                key = s3FileName
                body = ByteStream.fromBytes(multipartFile.bytes)
                acl = ObjectCannedAcl.PublicRead
            },
        )
        return getFullPath(s3FileName)
    }

    private fun getFullPath(s3FileName: String) =
        "https://$bucketName.s3.ap-northeast-2.amazonaws.com/$s3FileName"

    private fun createS3FileName(multipartFile: MultipartFile) =
        "static/${UUID.randomUUID()}-${multipartFile.originalFilename}"
}
