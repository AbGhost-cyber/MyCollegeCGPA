package com.crushtech.myccgpa.notification

import com.crushtech.myccgpa.utils.Constants.NO_EMAIL
import com.onesignal.OneSignal
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class OneSignalHelper {
    companion object {
        private val helper = object : OneSignal.OSExternalUserIdUpdateCompletionHandler {
            override fun onSuccess(results: JSONObject?) {
                Timber.d("Set external user id done with results")
                try {
                    results?.let {
                        if (it.has("push")
                            && it.getJSONObject("push").has("success")
                        ) {
                            val isPushSuccess = it.getJSONObject("push")
                                .getBoolean("success")
                            Timber.d("set external user id for push status: $isPushSuccess")
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(error: OneSignal.ExternalIdError?) {
                Timber.d("Set external user id done with error: ${error.toString()}")
            }
        }

        fun setUserExternalId(userId: String?) {
            if (userId == null || userId.isNullOrEmpty() || userId.isBlank()
                || userId == NO_EMAIL
            ) {
                Timber.d("User Id can't be null, skipping process")
            } else {
                OneSignal.setExternalUserId(userId, this.helper)
            }
        }

        fun removeUserExternalId() {
            OneSignal.removeExternalUserId()
        }
    }
}