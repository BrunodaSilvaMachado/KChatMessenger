package br.com.app.kchatmessenger.controller

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import br.com.app.kchatmessenger.MainActivity
import br.com.app.kchatmessenger.R
import br.com.app.kchatmessenger.firebaseHelper.Auth
import br.com.app.kchatmessenger.firebaseHelper.Firestore
import br.com.app.kchatmessenger.model.User
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import java.util.*

class RegisterFragment : Fragment() {
    companion object {
        private const val TAG = "KCM:RegisterActivity"
        private const val DEFAULT_USER_IMAGE = "android.resource://br.com.app.kchatmessenger/drawable/ic_person_blue_54dp.xml"
    }
    private var mSelectedUri: Uri? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        view.btn_register.setOnClickListener{ createUser() }
        view.register_btn_photo.setOnClickListener {selectPhoto()}

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            mSelectedUri = data?.data!!
            Log.i(TAG, mSelectedUri.toString())

            val bitmap = if(Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context?.contentResolver,mSelectedUri)
            } else {
                ImageDecoder.decodeBitmap (mSelectedUri?.let { context?.contentResolver?.let { cr ->
                    ImageDecoder.createSource(
                        cr, it)
                } }!!)
            }

            register_img_photo.setImageBitmap(bitmap)
            register_btn_photo.alpha = 0f
        }
    }

    private fun selectPhoto(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,0)
    }

    private fun createUser(){
        val email = register_email.text.toString()
        val password = register_password.text.toString()

        if (email.isEmpty() || password.isEmpty()) { // 2
            Toast.makeText(context,
                R.string.required_email_or_password, Toast.LENGTH_LONG).show()
            return
        }

        if(password.length < 6){
            Toast.makeText(context, getString(R.string.password_minor_6_chars), Toast.LENGTH_LONG).show()
            return
        }

        if (register_img_photo == null){
            mSelectedUri = Uri.parse(DEFAULT_USER_IMAGE)
        }

        Auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    Log.i(TAG, "UserID: ${it.result?.user?.uid}")
                    saveUserInFirebase()
                }
            }
            .addOnFailureListener{
                Log.e(TAG, it.message, it)
                Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            }
    }

    private fun saveUserInFirebase(){
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/${filename}")

        mSelectedUri?.let { it ->
            ref.putFile(it)
                .addOnSuccessListener{
                    ref.downloadUrl.addOnSuccessListener {
                        Log.i(TAG,it.toString())
                        val url = it.toString()
                        val name = register_name.text.toString()
                        val uid = Auth.getUID()!!
                        val user = User(uid, name, url)

                        Firestore.setUserInCollectionAndDocument("users", uid, user)
                            .addOnSuccessListener {
                                val intent = Intent(context, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                            .addOnFailureListener { exception ->  Log.e(TAG, exception.message, exception) }
                    }
                }
        }

    }
}