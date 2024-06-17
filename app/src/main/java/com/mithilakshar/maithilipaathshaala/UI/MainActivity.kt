package com.mithilakshar.maithilipaathshaala.UI

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.mithilakshar.maithilipaathshaala.R
import com.mithilakshar.maithilipaathshaala.Utility.FirebaseFileDownloader
import com.mithilakshar.maithilipaathshaala.Utility.dbHelper
import com.mithilakshar.maithilipaathshaala.databinding.ActivityMainBinding
import java.io.File

import android.os.Handler
import android.os.Looper
import android.view.View


import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fileDownloader: FirebaseFileDownloader
    private lateinit var dbHelper: dbHelper

    private var storagePaths: MutableList<String> = mutableListOf()
    private var localFileNames: MutableList<String> = mutableListOf()

    companion object {
        private const val REQUEST_WRITE_STORAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fileDownloader = FirebaseFileDownloader(this)
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("SQLdb")

        collectionRef.get().addOnSuccessListener {
            if (it != null) {
                for (document in it) {
                    val documentId = document.id
                    val storagePath = "SQLdb/$documentId"
                    val localFileName = "$documentId.db"
                    storagePaths.add(storagePath)
                    localFileNames.add(localFileName)


                }

                // Check for write external storage permission
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_WRITE_STORAGE
                    )
                } else {
                    // Permission already granted, proceed with download
                    startFileDownloads()
                }
            } else {
                Log.d(TAG, "No documents found in collection.")
            }
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error getting documents: ", exception)
        }

        val dbFolderPath = getExternalFilesDir(null)?.absolutePath + File.separator + "test"
        val dbFile = File(dbFolderPath, "math.db")
        if (dbFile.exists()) {
            // Database file exists, initialize dbHelper
            dbHelper = dbHelper(this, "math.db", )
            val tableNames = dbHelper?.getTableNames()
            val av = dbHelper.getRowCount("Book1")

            // Ensure av is positive before using it

            val avt = dbHelper.getRowValues("Book1", Random.nextInt(av))
            if (avt != null) {
                val formattedText = StringBuilder()
                for (value in avt) {
                    formattedText.append(value.toString()).append("\n")
                }
                binding.columnname.text = formattedText.toString()

                binding.tablename.text = "Table Names: ${tableNames?.joinToString(", ")}"
            }


        } else {
            binding.pbar.visibility=View.VISIBLE

            Handler(Looper.getMainLooper()).postDelayed({

                recreateActivity()
            }, 5000)
            // Handle case where the database file doesn't exist
            // You might want to create it, show an error message, or take other action
            // For example:
            // Toast.makeText(context, "Database file not found", Toast.LENGTH_SHORT).show()
        }


    }

    private fun startFileDownloads() {
        // Start downloading files based on stored paths and filenames
        for (i in storagePaths.indices) {
            downloadFile(storagePaths[i], localFileNames[i])
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, proceed with download
                startFileDownloads()
                Toast.makeText(
                    this,
                    "downloading",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // Permission denied, inform the user
                Toast.makeText(
                    this,
                    "Write permission is required to download files.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun downloadFile(storagePath: String, localFileName: String) {
        if (::fileDownloader.isInitialized) {
            fileDownloader.retrieveURL(
                storagePath,
                localFileName
            ) {
                if (it != null) {
                    // Use the retrieved URL to download the file
                    Toast.makeText(
                        this,
                        "downloaded url",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    // Handle the case where URL retrieval failed
                    Log.d(TAG, "Failed to retrieve URL")
                }
            }
        } else {
            Log.e(TAG, "fileDownloader is not initialized.")
        }
    }

    private fun recreateActivity() {
        val intent = intent // Get the current intent to pass extras if needed

        finish() // Finish the current activity
        startActivity(intent) // Start the activity again
    }

    override fun onDestroy() {
        super.onDestroy()
        // dbHelper.close()
        // Close database connection when activity is destroyed
    }
}