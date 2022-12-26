package com.dzenis_ska.lostandfound.ui.db.firebase

import android.net.Uri
import android.util.Log
import com.dzenis_ska.lostandfound.ui.utils.mathUtils.divideToPercent
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class FBStorage {

    //Storage
    private val storage = Firebase.storage
    private val ref = storage.getReference(STORAGE_NODE)

    fun addPhotoToStorage(
        byteArray: ByteArray,
        currUserEmail: String,
        statusUpload: (statusUpload: String) -> Unit,
        callbackUri: (uri: Uri?) -> Unit,
        exception: (uri: String?) -> Unit,
    ) {
        val imStorageRef = ref
            .child(currUserEmail)
//            .child(key)
            .child("${System.currentTimeMillis().toString().substring(5)}_@_image")
        val uploadTask = imStorageRef.putBytes(byteArray)
        uploadTask.addOnProgressListener {
            val per = it.totalByteCount.divideToPercent(it.bytesTransferred).toString()
            statusUpload(per)
        }.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    Log.d("!!!Photo not Uploaded", "$it")
                    throw it
                }
            } else {
                imStorageRef.downloadUrl
            }
        }.addOnSuccessListener { uri ->
            callbackUri(uri)
        }.addOnFailureListener { exception->
            callbackUri(null)
            exception(exception.message)
        }
    }

    fun deletePhoto(url: String, callback: (isPhotoDeleted: Boolean) -> Unit){
        ref.storage
            .getReferenceFromUrl(url)
            .delete()
            .addOnSuccessListener{
                Log.d("!!!Photo deleted", "successful")
                callback(true)
            }
            .addOnFailureListener {
                Log.d("!!!Photo deleted", "${it.message}")
                callback(false)
            }
    }

    fun deletePhotoWithoutListener(url: String){
        Log.d("!!!deletePhotoWithoutListener", "${url}")

        ref.storage
            .getReferenceFromUrl(url)
            .delete()
    }

    companion object {
        const val STORAGE_NODE = "lost_and_found"
    }

}