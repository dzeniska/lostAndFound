package com.dzenis_ska.lostandfound.ui.db.firebase

import android.net.Uri
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class FBAuth {
    val auth = Firebase.auth

    fun uid() = auth.uid

    val currentUser: FirebaseUser?
        get() {
            return auth.currentUser
        }

    fun signInAnonymously(callback: (currentUser: FirebaseUser?) -> Unit,failure: (error: String?) -> Unit) {
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("!!!signInAnonymously2", "${auth.currentUser}")
                    callback(auth.currentUser)
                } else {
                    Log.d("!!!signInAnonymously", "signInAnonymously:failure", task.exception)
                    failure(task.exception?.message)
                }
            }
    }

    fun signInFirebaseWithGoogle(
        account: GoogleSignInAccount,
        firebaseUser: (currentUser: FirebaseUser?) -> Unit,
        failure: (error: String?) -> Unit
    ) {
        Log.d("!!!signInFirebaseWithGoogle1", "${account.photoUrl}")
        Log.d("!!!signInFirebaseWithGoogle1", "${currentUser}")
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        if (currentUser == null) {
            failure("signInFirebaseWithGoogle: error")
            signInAnonymously({},{})
        }
        currentUser?.linkWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    updateProfile(account.photoUrl)
                        ?.addOnSuccessListener { firebaseUser(auth.currentUser) }
                        ?.addOnFailureListener { firebaseUser(user) }
                    Log.d("!!!signInFirebaseWithGoogle2", "${user?.email}")
                } else {
                    if (task.exception?.message == "This credential is already associated with a different user account.") {
                        auth.signInWithCredential(credential)
                            .addOnSuccessListener { authResult ->
                                updateProfile(account.photoUrl)
                                    ?.addOnSuccessListener { firebaseUser(auth.currentUser) }
                                    ?.addOnFailureListener { firebaseUser(authResult.user) }
                                Log.d("!!!signInFirebaseWithGoogle3", "${authResult.user?.email}")
                            }.addOnFailureListener {
                                failure(task.exception?.message)
                                Log.d(
                                    "!!!signInFirebaseWithGoogle1",
                                    "linkWithCredential:failure ${task.exception?.message}"
                                )
                            }
                    } else {
                        failure(task.exception?.message)
                    }
                    failure(task.exception?.message)
                    Log.d(
                        "!!!signInFirebaseWithGoogle2exception",
                        "linkWithCredential:failure ${task.exception?.message}"
                    )
                }
            }
    }

    private fun updateProfile(photoUrl: Uri?): Task<Void>? {
        Log.d("!!!updateProfile", " ${photoUrl}")
        return auth.currentUser?.updateProfile(userProfileChangeRequest {
            photoUri = photoUrl
        })
    }

    fun signOut(callback: () -> Unit) {
        Firebase.auth.signOut()
        callback()
    }

    fun signOutGoogleClient(
        signInGoogleClient: GoogleSignInClient,
        signOutCallback: (currentUser: FirebaseUser?) -> Unit,
        failure: (error: String?) -> Unit
    ) {
        signInGoogleClient.signOut().addOnSuccessListener {
            signOut {
                signInAnonymously(
                    callback = {fbu ->
                        signOutCallback(fbu)
                    },
                    failure = {error->
                        failure(error)
                    }
                )
            }
        }.addOnFailureListener { failure(it.message) }
    }
    companion object {
        const val NO_AUTHORISED_USER = "NO_AUTHORISED_USER"
    }
}