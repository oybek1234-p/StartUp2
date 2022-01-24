package com.imagekit.android.data

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import com.imagekit.android.ImageKitCallback
import com.imagekit.android.R
import com.imagekit.android.entity.SignatureResponse
import com.imagekit.android.entity.UploadError
import com.imagekit.android.entity.UploadResponse
import com.imagekit.android.retrofit.NetworkManager
import com.imagekit.android.util.LogUtil
import com.imagekit.android.util.SharedPrefUtil
import retrofit2.HttpException
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class Repository @Inject constructor(
    private val context: Context,
    private val sharedPrefUtil: SharedPrefUtil
) {
    companion object {
        private const val DURATION_EXPIRY_MINUTES = 45L
    }

    @SuppressLint("CheckResult")
    fun upload(
        file: Any,
        fileName: String,
        useUniqueFilename: Boolean,
        tags: Array<String>?,
        folder: String?,
        isPrivateFile: Boolean?,
        customCoordinates: String?,
        responseFields: String?,
        imageKitCallback: ImageKitCallback
    ) {
        val expire = ((System.currentTimeMillis() / 1000) + TimeUnit.MINUTES.toSeconds(
            DURATION_EXPIRY_MINUTES
        )).toString()

        val endPoint = sharedPrefUtil.getClientAuthenticationEndpoint()

        if (endPoint.isBlank()) {
            LogUtil.logError("Upload failed! Authentication endpoint is missing!")
            return imageKitCallback.onError(
                UploadError(
                    exception = true,
                    message = context.getString(R.string.error_authentication_endpoint_is_missing)
                )
            )
        }

        val publicKey = sharedPrefUtil.getClientPublicKey()
        if (publicKey.isBlank()) {
            LogUtil.logError("Upload failed! Public Key is missing!")
            return imageKitCallback.onError(
                UploadError(
                    exception = true,
                    message = context.getString(R.string.error_public_key_is_missing)
                )
            )
        }

        NetworkManager.getSignature(endPoint, expire)
            .doOnError { _ ->
                imageKitCallback.onError(
                    UploadError(
                        true,
                        message = context.getString(R.string.error_signature_generation_failed)
                    )
                )
            }
            .flatMap { signatureResponse: SignatureResponse ->
                NetworkManager.getFileUploadCall(
                    publicKey,
                    signatureResponse,
                    file,
                    fileName,
                    useUniqueFilename,
                    tags,
                    folder,
                    isPrivateFile,
                    customCoordinates,
                    responseFields
                ).doOnError { e ->
                    if (e is HttpException) {
                        e.response()?.let {
                            try {
                                imageKitCallback.onError(
                                    Gson().fromJson(
                                        it.errorBody()!!.string(),
                                        UploadError::class.java
                                    )
                                )
                            } catch (exception: IllegalStateException) {
                                imageKitCallback.onError(
                                    UploadError(
                                        exception = true,
                                        statusNumber = e.code(),
                                        message = e.message()
                                    )
                                )
                            }
                        }
                    } else {
                        e.message?.let {
                            imageKitCallback.onError(UploadError(true, message = it))
                        } ?: run {
                            imageKitCallback.onError(UploadError(true))
                        }
                    }
                }
            }
            .subscribe({ response ->
                imageKitCallback.onSuccess(
                    Gson().fromJson(
                        response.string(),
                        UploadResponse::class.java
                    )
                )
            }, { e ->
                LogUtil.logError(e)
            })
    }

}