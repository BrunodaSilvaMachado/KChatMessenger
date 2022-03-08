package br.com.app.kchatmessenger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.app.kchatmessenger.controller.LoginFragment


class AuthOrRegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_or_register)

        val fragmentManager = supportFragmentManager

        if (fragmentManager.findFragmentByTag("LoginFragment") == null){
            fragmentManager.beginTransaction().add(R.id.auth_or_register_frame, LoginFragment(), "LoginFragment").commit()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
