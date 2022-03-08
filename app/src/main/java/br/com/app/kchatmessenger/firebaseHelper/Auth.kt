package br.com.app.kchatmessenger.firebaseHelper

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class Auth {
    companion object{
        private fun getInstance(): FirebaseAuth {
            return FirebaseAuth.getInstance()
        }

        fun signInWithEmailAndPassword(email: String, password: String): Task<AuthResult> {
            return getInstance().signInWithEmailAndPassword(email, password)
        }

        fun createUserWithEmailAndPassword(email: String,password: String): Task<AuthResult> {
            return getInstance().createUserWithEmailAndPassword(email, password)
        }

        fun isAuthenticated(): Boolean {
            return getUID() == null
        }

        fun getUID(): String? {
            return getInstance().uid
        }

        fun signOut() {
            return getInstance().signOut()
        }
    }
}