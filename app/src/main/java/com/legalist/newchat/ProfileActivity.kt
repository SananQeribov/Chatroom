package com.legalist.newchat

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.UUID
class ProfileActivity : AppCompatActivity() {
    private var selectedImageUri: Uri? = null
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var storageReference: StorageReference

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                launchNewPhotoPicker()
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Please grant permission", Toast.LENGTH_LONG).show()
            }
        }

    private val newPhotoPicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                findViewById<ImageView>(R.id.profile).setImageURI(it)
                selectedImageUri = it
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Firebase təyini
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference = firebaseDatabase.getReference("Profiles")
        mAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        enableEdgeToEdge()

        // Sistem çubuqları üçün boşluq əlavə edin
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        selectPhoto()
        setupUploadButton()
        setupBottomNavigationView()
        getData()
    }

    private fun launchNewPhotoPicker() {
        newPhotoPicker.launch("image/*")
    }

    private fun setupUploadButton() {
        findViewById<Button>(R.id.button).setOnClickListener {
            val uuid = UUID.randomUUID()
            val imageName = "images/$uuid.jpg"
            val storageRef = storageReference.child(imageName)

            selectedImageUri?.let { uri ->
                storageRef.putFile(uri).addOnSuccessListener {
                    val profileImageRef = FirebaseStorage.getInstance().getReference(imageName)
                    profileImageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val urlString = downloadUri.toString()
                        val uuidString = UUID.randomUUID().toString()
                        val userAge = findViewById<EditText>(R.id.AgeextText).text.toString()
                        val name = findViewById<EditText>(R.id.Name_edittext).text.toString()
                        val user = mAuth.currentUser
                        val email = user?.email.toString()

                        reference.child(uuidString).apply {
                            child("UserImageUrl").setValue(urlString)
                            child("Name").setValue(name)
                            child("UserEmail").setValue(email)
                            child("UserAge").setValue(userAge)
                        }

                        Toast.makeText(this, "Upload successful: $urlString", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, ChatActivity::class.java))
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, e.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun selectPhoto() {
        findViewById<ImageView>(R.id.profile).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(READ_MEDIA_IMAGES)
            } else {
                requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE.toString())
            }
        }
    }

    private fun setupBottomNavigationView() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomnav)
        bottomNavigationView.selectedItemId = R.id.options_menu_profile

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.options_chat -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    true
                }
                R.id.options_menu_profile -> true
                R.id.options_menu_sign_out -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    private fun getData() {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ageEditText = findViewById<EditText>(R.id.AgeextText)
                val nameEditText = findViewById<EditText>(R.id.Name_edittext)
                val imageView = findViewById<ImageView>(R.id.profile)

                for (ds in snapshot.children) {
                    val hashMap = ds.value as? HashMap<*, *>
                    val userEmail = hashMap?.get("UserEmail") as? String

                    if (userEmail == mAuth.currentUser?.email) {
                        val userAge = hashMap?.get("UserAge") as? String
                        val userImageUrl = hashMap?.get("UserImageUrl") as? String
                        val username = hashMap?.get("Name") as? String

                        if (userAge != null && userImageUrl != null && username != null) {
                            ageEditText.setText(userAge)
                            nameEditText.setText(username)
                            Picasso.get().load(userImageUrl).into(imageView)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Data loading failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
