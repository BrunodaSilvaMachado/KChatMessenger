package br.com.app.kchatmessenger.firebaseHelper

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class Firestore {

    companion object{
        private fun getInstance(): FirebaseFirestore {
            return FirebaseFirestore.getInstance()
        }

        fun setUserInCollectionAndDocument(collectionPath: String, documentPath: String, data: Any): Task<Void> {
            return getInstance().collection(collectionPath).document(documentPath).set(data)
        }

        fun getUserInCollectionAndDocument(collectionPath: String, documentPath: String): Task<DocumentSnapshot> {
            return getInstance().collection(collectionPath).document(documentPath).get()
        }

        fun collectionDocumentCollection(collectionPath: String, documentPath: String, otherCollectionPath: String): CollectionReference {
            return getInstance().collection(collectionPath).document(documentPath).collection(otherCollectionPath)
        }

        fun collection(collectionPath: String): CollectionReference {
            return getInstance().collection(collectionPath)
        }
    }
}