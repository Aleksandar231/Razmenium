package com.razmenium.app

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.razmenium.app.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            setLoading(true)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { goToHome() }
                .addOnFailureListener { showAuthError(it) }
        } catch (e: ApiException) {
            // Откажување од корисникот не е грешка
            if (e.statusCode != GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                showAuthError(e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        // Ако корисникот е веќе логиран, оди директно на почетниот екран
        if (auth.currentUser != null) {
            goToHome()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            if (!validateInput(forRegister = false)) return@setOnClickListener
            setLoading(true)
            auth.signInWithEmailAndPassword(email(), password())
                .addOnSuccessListener { goToHome() }
                .addOnFailureListener { showAuthError(it) }
        }

        binding.btnRegister.setOnClickListener {
            if (!validateInput(forRegister = true)) return@setOnClickListener
            setLoading(true)
            auth.createUserWithEmailAndPassword(email(), password())
                .addOnSuccessListener { goToHome() }
                .addOnFailureListener { showAuthError(it) }
        }

        binding.btnAnonymous.setOnClickListener {
            setLoading(true)
            auth.signInAnonymously()
                .addOnSuccessListener { goToHome() }
                .addOnFailureListener { showAuthError(it) }
        }

        binding.btnGoogle.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        // Facebook логирање
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                    setLoading(true)
                    auth.signInWithCredential(credential)
                        .addOnSuccessListener { goToHome() }
                        .addOnFailureListener { showAuthError(it) }
                }

                override fun onCancel() {}

                override fun onError(error: FacebookException) {
                    showAuthError(error)
                }
            })

        binding.btnFacebook.setOnClickListener {
            LoginManager.getInstance()
                .logInWithReadPermissions(this, callbackManager, listOf("public_profile"))
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun email() = binding.etEmail.text.toString().trim()

    private fun password() = binding.etPassword.text.toString().trim()

    private fun validateInput(forRegister: Boolean): Boolean {
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        // Строга валидација само при регистрација — за логирање нека одлучи
        // Firebase, за да не блокираме постоечки сметки со поинаков формат
        var valid = true
        if (email().isEmpty()) {
            binding.tilEmail.error = getString(R.string.field_required)
            valid = false
        } else if (forRegister && !Patterns.EMAIL_ADDRESS.matcher(email()).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            valid = false
        }
        if (password().isEmpty()) {
            binding.tilPassword.error = getString(R.string.field_required)
            valid = false
        } else if (forRegister && password().length < 6) {
            binding.tilPassword.error = getString(R.string.error_short_password)
            valid = false
        }
        return valid
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
        binding.btnRegister.isEnabled = !loading
        binding.btnGoogle.isEnabled = !loading
        binding.btnAnonymous.isEnabled = !loading
        binding.btnFacebook.isEnabled = !loading
    }

    private fun showAuthError(e: Exception) {
        setLoading(false)
        val message = when (e) {
            is FirebaseAuthWeakPasswordException -> getString(R.string.error_weak_password)
            is FirebaseAuthInvalidCredentialsException -> getString(R.string.error_invalid_credentials)
            is FirebaseAuthInvalidUserException -> getString(R.string.error_invalid_credentials)
            is FirebaseAuthUserCollisionException -> getString(R.string.error_email_in_use)
            is FirebaseNetworkException -> getString(R.string.error_network)
            else -> e.localizedMessage ?: getString(R.string.error_generic)
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
