package br.com.app.kchatmessenger.controller

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import br.com.app.kchatmessenger.MainActivity
import br.com.app.kchatmessenger.R
import br.com.app.kchatmessenger.firebaseHelper.Auth
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*

class LoginFragment : Fragment() {
    companion object {private const val TAG = "KCM:LoginActivity"}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        view.login_create_accounts.setOnClickListener {
            val fragmentTransaction = fragmentManager?.beginTransaction()
            fragmentTransaction?.replace(R.id.auth_or_register_frame, RegisterFragment(), "RegisterFragment")?.commit()
            fragmentTransaction?.addToBackStack("LoginFragment")
        }
        view.login_button_enter.setOnClickListener{ singIn() }

        return view
    }

    private fun singIn(){
        val email = login_email.text.toString()
        val password = login_password.text.toString()

        if (email.isEmpty() || password.isEmpty()) { // 2
            Toast.makeText(context,
                R.string.required_email_or_password, Toast.LENGTH_LONG).show()
            return
        }

        Auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                Log.i(TAG, "UserID: ${it.result?.user?.uid}")
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }.addOnFailureListener { Log.e(TAG, it.message, it) }
    }
}
