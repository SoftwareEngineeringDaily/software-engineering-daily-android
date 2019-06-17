package com.koalatea.sedaily.util

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

val Fragment.supportActionBar
    get() = (activity as? AppCompatActivity)?.supportActionBar
