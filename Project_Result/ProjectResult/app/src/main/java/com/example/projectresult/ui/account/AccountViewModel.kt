package com.example.projectresult.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AccountViewModel : ViewModel() {

    private var _email = MutableLiveData<String>().apply {
        value = "null"
    }
    var email: LiveData<String> = _email

    private var _passwd = MutableLiveData<String>().apply {
        value = "null"
    }
    var passwd: LiveData<String> = _passwd

    private var _name = MutableLiveData<String>().apply {
        value = "null"
    }
    var name: LiveData<String> = _name

    private var _age = MutableLiveData<Int>().apply {
        value = 0
    }
    var age: LiveData<Int> = _age

    private var _gender = MutableLiveData<String>().apply {
        value = "null"
    }
    var gender: LiveData<String> = _gender

    private var _weight = MutableLiveData<Int>().apply {
        value = 0
    }
    var weight: LiveData<Int> = _weight

    private var _height = MutableLiveData<Int>().apply {
        value = 0
    }
    var height: LiveData<Int> = _height
}