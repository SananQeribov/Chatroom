package com.legalist.newchat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.legalist.newchat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        if (user != null) {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)

        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        signUp()
        signIn()
    }

    private fun signUp() {
        binding.registrBt.setOnClickListener {
            val email = binding.emailedittext3.text.toString()
            val password = binding.passworddittext.text.toString()

            // Yoxlayın ki, e-poçt və şifrə boş deyil
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "E-poçt və şifrə boş olmamalıdır", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase qeydiyyatı
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        //  val user = mAuth.currentUser
                        // val userEmail = user?.email.toString()
                        // Log.e("UserEmail", "Current user email: $userEmail")
                        val intent = Intent(this, ChatActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(this, "Qeydiyyat uğurlu oldu!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Səhv mesajını logcat-a yazın
                        Log.e("SignUp", "Qeydiyyat zamanı xətalar: ${task.exception?.message}")
                        Toast.makeText(
                            this,
                            "Qeydiyyat xətası: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }


    fun signIn() {
        binding.loginTxt.setOnClickListener {
            val email = binding.emailedittext3.text.toString()
            val password = binding.passworddittext.text.toString()
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, ChatActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Giriş uğurlu oldu!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("Signİn", "Giriş zamanı xətalar: ${task.exception?.message}")
                    Toast.makeText(
                        this,
                        "Giriş xətası: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()

                }


            }
        }

    }
}
