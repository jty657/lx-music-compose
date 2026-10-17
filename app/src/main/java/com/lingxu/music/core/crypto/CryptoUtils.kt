package com.lingxu.music.core.crypto

import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/** 与原版 user-api-preload.js 的 native utils 一致的加密实现 */
object CryptoUtils {

    fun str2b64(data: String): String =
        android.util.Base64.encodeToString(data.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)

    fun b642buf(base64: String): ByteArray =
        android.util.Base64.decode(base64, android.util.Base64.NO_WRAP)

    /** 与原版一致：对 encodeURIComponent 后的字符串做 URL decode 再 md5 */
    fun str2md5(uriEncoded: String): String {
        val str = java.net.URLDecoder.decode(uriEncoded, "UTF-8")
        return md5(str.toByteArray(Charsets.UTF_8))
    }

    fun md5(data: ByteArray): String =
        MessageDigest.getInstance("MD5").digest(data).joinToString("") { "%02x".format(it) }

    fun md5(str: String): String = md5(str.toByteArray(Charsets.UTF_8))

    /**
     * AES 加密。mode 直接透传给 [Cipher.getInstance]（与原版 AES.java 一致）：
     * "AES/CBC/PKCS7Padding" 或 "AES"——注意 Java 里 `Cipher.getInstance("AES")` 实际是
     * AES/ECB/PKCS5Padding，并非 NoPadding（曾因此把网易 eapi 的 params 加密错，搜索返回空）。
     */
    fun aesEncrypt(dataB64: String, keyB64: String, ivB64: String, mode: String): String {
        val data = b642buf(dataB64)
        val key = b642buf(keyB64)
        val cipher = Cipher.getInstance(mode)
        val spec = SecretKeySpec(key, "AES")
        if (ivB64.isEmpty()) {
            cipher.init(Cipher.ENCRYPT_MODE, spec)
        } else {
            val iv = b642buf(ivB64)
            val finalIv = ByteArray(16)
            System.arraycopy(iv, 0, finalIv, 0, minOf(iv.size, 16))
            cipher.init(Cipher.ENCRYPT_MODE, spec, IvParameterSpec(finalIv))
        }
        val out = cipher.doFinal(data)
        return android.util.Base64.encodeToString(out, android.util.Base64.NO_WRAP)
    }

    /** AES 解密，mode 同样透传给 [Cipher.getInstance] */
    fun aesDecrypt(dataB64: String, keyB64: String, ivB64: String, mode: String): String {
        val data = b642buf(dataB64)
        val key = b642buf(keyB64)
        val cipher = Cipher.getInstance(mode)
        val spec = SecretKeySpec(key, "AES")
        if (ivB64.isEmpty()) {
            cipher.init(Cipher.DECRYPT_MODE, spec)
        } else {
            val iv = b642buf(ivB64)
            val finalIv = ByteArray(16)
            System.arraycopy(iv, 0, finalIv, 0, minOf(iv.size, 16))
            cipher.init(Cipher.DECRYPT_MODE, spec, IvParameterSpec(finalIv))
        }
        val out = cipher.doFinal(data)
        return String(out, Charsets.UTF_8)
    }

    /** RSA 解密 */
    fun rsaDecrypt(dataB64: String, privateKeyB64: String, padding: String): String {
        val data = b642buf(dataB64)
        val keyBytes = b642buf(privateKeyB64)
        val keyFactory = java.security.KeyFactory.getInstance("RSA")
        val privateKey = keyFactory.generatePrivate(java.security.spec.PKCS8EncodedKeySpec(keyBytes))
        val mode = if (padding.contains("NoPadding")) "RSA/ECB/NoPadding" else "RSA/ECB/OAEPWithSHA1AndMGF1Padding"
        val cipher = Cipher.getInstance(mode)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val out = cipher.doFinal(data)
        return String(out, Charsets.UTF_8)
    }

    fun sha1(str: String): String =
        MessageDigest.getInstance("SHA-1").digest(str.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    fun rsaEncrypt(dataB64: String, publicKeyB64: String, padding: String): String {
        val data = b642buf(dataB64)
        val keyBytes = b642buf(publicKeyB64)
        val keyFactory = java.security.KeyFactory.getInstance("RSA")
        val publicKey = keyFactory.generatePublic(java.security.spec.X509EncodedKeySpec(keyBytes))
        val mode = if (padding.contains("NoPadding")) "RSA/ECB/NoPadding" else "RSA/ECB/OAEPWithSHA1AndMGF1Padding"
        val cipher = Cipher.getInstance(mode)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val out = cipher.doFinal(data)
        return android.util.Base64.encodeToString(out, android.util.Base64.NO_WRAP)
    }
}
