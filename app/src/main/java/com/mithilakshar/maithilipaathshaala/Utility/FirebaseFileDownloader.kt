package com.mithilakshar.maithilipaathshaala.Utility


import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.ProgressBar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream

class FirebaseFileDownloader(private val context: Context) {

    private val TAG = "FirebaseFileDownloader"
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun retrieveURL(documentPath: String, urlFieldName: String, callback: (File?) -> Unit) {
        // Retrieve the URL from Firestore
        firestore.document(documentPath)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val url = documentSnapshot.getString("test")
                    if (url != null) {
                        // Create a directory for storing downloaded files
                        val downloadDirectory = File(context.getExternalFilesDir(null), "test")
                        if (!downloadDirectory.exists()) {
                            downloadDirectory.mkdirs()
                        }

                        // Create a local file path
                        val localFile = File(downloadDirectory, urlFieldName)

                        if (localFile.exists()) {
                            // File already exists locally, return it
                            callback(localFile)
                        } else {
                            // File does not exist locally, download from Firebase Storage
                            downloadFile(url, localFile, callback)
                        }
                    } else {
                        Log.d(TAG, "URL field is null")
                        callback(null)
                    }
                } else {
                    Log.d(TAG, "Document does not exist")
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error retrieving document", e)
                callback(null)
            }
    }

    private fun downloadFile(url: String, localFile: File, callback: (File?) -> Unit) {
        val storageRef = storage.getReferenceFromUrl(url)
        storageRef.getFile(localFile)
            .addOnSuccessListener {
                // File downloaded successfully
                callback(localFile)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error downloading file", e)
                callback(null)
            }
    }



}

