package br.com.app.kchatmessenger

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.app.kchatmessenger.controller.MessagesFragment
import br.com.app.kchatmessenger.firebaseHelper.Auth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val messagesFragment = MessagesFragment()

        if(supportFragmentManager.findFragmentByTag("MessagesFragment") == null){
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.add(R.id.main_frame, messagesFragment, "MessagesFragment").commit()
        }

        verifyAuthentication()
    }

    fun verifyAuthentication(){
        if(Auth.isAuthenticated()){
            val intent = Intent(baseContext, AuthOrRegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
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
