package com.mithilakshar.maithilipaathshaala.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mithilakshar.maithilipaathshaala.Utility.FirebaseFileDownloader

class HomeViewModel(private val firebaseFileDownloader: FirebaseFileDownloader): ViewModel() {



    val downloadProgressLiveData: LiveData<Int> = firebaseFileDownloader.downloadProgressLiveData

    fun retrieveAndDownloadFile(documentPath: String, action: String, urlFieldName: String) {
        firebaseFileDownloader.retrieveURL(documentPath, action, urlFieldName) { file ->
            // Handle the downloaded file if needed
        }
    }

    class Factory(private val firebaseFileDownloader: FirebaseFileDownloader) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
             return HomeViewModel(firebaseFileDownloader) as T
        }
    }
}


