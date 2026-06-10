package com.razmenium.app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth

/**
 * Сите активности наследуваат од оваа класа за избраниот јазик
 * да важи насекаде, а не само на еден екран.
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    /** Заедничка одјава — иста логика од кој било екран. */
    protected fun logoutAndExit() {
        FirebaseAuth.getInstance().signOut()
        LoginManager.getInstance().logOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finishAffinity()
    }
}
