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
import androidx.lifecycle.ViewModelProvider
import com.mithilakshar.maithilipaathshaala.ViewModel.HomeViewModel
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fileDownloader: FirebaseFileDownloader
    private lateinit var dbHelper: dbHelper
    private lateinit var homeviewModel: HomeViewModel

    private var storagePaths: MutableList<String> = mutableListOf()
    private var localFileNames: MutableList<String> = mutableListOf()
    private var actions: MutableList<String> = mutableListOf()
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
        val factory = HomeViewModel.Factory(fileDownloader)
        homeviewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)


        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("SQLdb")

        collectionRef.get().addOnSuccessListener {
            if (it != null) {
                for (document in it) {
                    val documentId = document.id
                    val storagePath = "SQLdb/$documentId"
                    val localFileName = "$documentId.db"
                    val action = document.getString("action") ?: "av"
                    storagePaths.add(storagePath)
                    localFileNames.add(localFileName)
                    actions.add(action)

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



        homeviewModel.downloadProgressLiveData.observe(this, {

            binding.tablename.text = it.toString()

        })



    }

    private fun startFileDownloads() {
        // Start downloading files based on stored paths and filenames
        for (i in storagePaths.indices) {
            downloadFile(storagePaths[i],actions[i], localFileNames[i])
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

    private fun downloadFile(storagePath: String, action: String, localFileName: String) {
        if (::fileDownloader.isInitialized) {
            fileDownloader.retrieveURL(storagePath, action, localFileName) { downloadedFile ->
                if (downloadedFile != null) {
                    // File downloaded successfully, do something with the file if needed
                    Log.d(TAG, "File downloaded successfully: $downloadedFile")

                    // Notify UI or perform tasks with downloaded file
                    handleDownloadedFile(downloadedFile)
                } else {
                    // Handle the case where download failed
                    Log.d(TAG, "Download failed for file: $localFileName")
                }
            }
        } else {
            Log.e(TAG, "fileDownloader is not initialized.")
        }
    }

    private fun handleDownloadedFile(downloadedFile: File) {
        // Handle the downloaded file here, e.g., update UI, save to database, etc.
        Toast.makeText(this, "File downloaded: ${downloadedFile.name}", Toast.LENGTH_SHORT).show()
        // Example: Save to database using dbHelper
        val dbFolderPath = getExternalFilesDir(null)?.absolutePath + File.separator + "test"
        val dbFile = File(dbFolderPath, "math.db")
        if (dbFile.exists()) {
            val dbHelper = dbHelper(applicationContext, "math.db")
            val tableNames = dbHelper?.getTableNames()
            val av = dbHelper.getRowCount("Book1")
            binding.tablename.text = "Table Names: ${tableNames?.joinToString(", ")}"

            if (av > 0) {
                val avt = dbHelper.getRowValues("Book1", Random.nextInt(av))
                if (avt != null) {
                    val formattedText = StringBuilder()
                    for (value in avt) {
                        formattedText.append(value.toString()).append("\n")
                    }


                }
            }
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