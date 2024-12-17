package com.dicoding.asclepius.view
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModel:ViewModel (){
    private val _currentimage = MutableLiveData<Uri>()
    val currentimage:LiveData <Uri> = _currentimage

    fun setImage(uri: Uri) {
        _currentimage.value = uri
    }

}