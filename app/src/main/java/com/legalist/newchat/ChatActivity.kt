package com.legalist.newchat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener

import com.legalist.newchat.databinding.ActivityChatBinding
import java.util.UUID

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private val chatMessages = mutableListOf<String>()
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database.getReference()

        // Adapteri yarat
        recyclerViewAdapter = RecyclerViewAdapter(chatMessages)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            itemAnimator = DefaultItemAnimator()
            adapter = recyclerViewAdapter
        }

        binding.bottomnav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.options_chat -> {

                    true
                }

                R.id.options_menu_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                R.id.options_menu_sign_out -> {
                    mAuth.signOut()
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }

                else -> false
            }
        }

        sendMessage()
        // getData()  // Məlumatları al
    }

    private fun sendMessage() {
        binding.chatActivitySendButton.setOnClickListener {
            val messageToSend = binding.chatActivityMessageText.text.toString()
            if (messageToSend.isNotBlank()) {
                val uuidString = UUID.randomUUID().toString()
                val user = mAuth.currentUser
                val userEmail = user?.email ?: "Unknown User"
                reference.child("Chats").child(uuidString)
                    .child("usermessage").setValue(messageToSend)
                reference.child("Chats").child(uuidString)
                    .child("useremail").setValue(userEmail)
                reference.child("Chats").child(uuidString).child("messagetime")
                    .setValue(ServerValue.TIMESTAMP)
                binding.chatActivityMessageText.setText("")
            }
        }
        getData()
    }

    private fun getData() {
        val getReference = database.getReference("Chats")
        val query = getReference.orderByChild("messagetime")
        query.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                chatMessages.clear() // Mövcud siyahını təmizlə
                for (ds in snapshot.children) {
                    val hashMap = ds.value as? HashMap<String, String>
                    val userEmail = hashMap?.get("useremail")
                    val userMessage = hashMap?.get("usermessage")
                    if (userEmail != null && userMessage != null) {
                        chatMessages.add("$userEmail: $userMessage")
                    }
                }
                recyclerViewAdapter.notifyDataSetChanged() // Adapter-i yenilə
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_LONG).show()
            }
        })
    }
}
