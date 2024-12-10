package com.legalist.newchat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
private val mAuth =FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        Handler(Looper.getMainLooper()).postDelayed({
           navigateToActivity()
            finish()
        }, 6000)
    }
    fun navigateToActivity(){
        val user = mAuth.currentUser
        if (user != null) {
            val intent = Intent(this,ChatActivity::class.java)
            startActivity(intent)
        }
        else{
            startActivity(Intent(this,MainActivity::class.java))
        }

    }
}

