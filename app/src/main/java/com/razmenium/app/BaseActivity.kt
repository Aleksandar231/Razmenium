package com.razmenium.app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

/**
 * Сите активности наследуваат од оваа класа за избраниот јазик
 * да важи насекаде, а не само на еден екран.
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }
}
