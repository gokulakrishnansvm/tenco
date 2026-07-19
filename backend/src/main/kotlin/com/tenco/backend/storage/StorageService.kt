package com.tenco.backend.storage

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.util.UUID

/**
 * Stores uploaded media. Uses S3 when a bucket is configured (tenco.storage.s3-bucket),
 * otherwise writes to a local directory (dev). Returns a URL to the stored object.
 */
@Service
class StorageService(
    @Value("\${tenco.storage.s3-bucket:}") private val bucket: String,
    @Value("\${tenco.storage.region:ap-south-1}") private val region: String,
    @Value("\${tenco.storage.local-dir:./uploads}") private val localDir: String,
    @Value("\${tenco.storage.public-base-url:}") private val publicBaseUrl: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val useS3 get() = bucket.isNotBlank()
    private val s3 by lazy { S3Client.builder().region(Region.of(region)).build() }

    /** Stores [bytes] and returns a URL. [ext] is a file extension like "jpg". */
    fun uploadComplaintPhoto(bytes: ByteArray, contentType: String, ext: String): String {
        val key = "complaints/${UUID.randomUUID()}.$ext"
        return if (useS3) {
            s3.putObject(
                PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType).build(),
                RequestBody.fromBytes(bytes),
            )
            val base = publicBaseUrl.ifBlank { "https://$bucket.s3.$region.amazonaws.com" }
            "$base/$key"
        } else {
            val file = File(localDir, key).apply { parentFile?.mkdirs(); writeBytes(bytes) }
            log.debug("stored photo locally at {}", file.absolutePath)
            "/api/uploads/files/$key"
        }
    }

    /** Reads a locally-stored file (dev fallback serving). */
    fun readLocal(key: String): ByteArray? {
        val file = File(localDir, key)
        return if (file.exists() && file.canonicalPath.startsWith(File(localDir).canonicalPath)) file.readBytes() else null
    }
}
