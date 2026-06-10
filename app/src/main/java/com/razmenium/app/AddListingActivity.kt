package com.razmenium.app

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.razmenium.app.databinding.ActivityAddListingBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Екран за нов оглас. Ако се отвори со EXTRA_ID, работи во режим
 * на измена на постоечки оглас.
 */
class AddListingActivity : BaseActivity() {

    private lateinit var binding: ActivityAddListingBinding
    private val repository by lazy { ListingRepository(applicationContext) }

    private var editListingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        editListingId = intent.getStringExtra(EXTRA_ID)
        if (editListingId != null) {
            binding.toolbar.title = getString(R.string.edit_listing)
            binding.btnPublish.text = getString(R.string.save_changes)
            binding.etOffering.setText(intent.getStringExtra(EXTRA_OFFERING).orEmpty())
            binding.etSeeking.setText(intent.getStringExtra(EXTRA_SEEKING).orEmpty())
            binding.etDescription.setText(intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty())
        }

        binding.btnPublish.setOnClickListener { publish() }
    }

    private fun publish() {
        val offering = binding.etOffering.text.toString().trim()
        val seeking = binding.etSeeking.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        binding.tilOffering.error = null
        binding.tilSeeking.error = null
        var valid = true
        if (offering.isEmpty()) {
            binding.tilOffering.error = getString(R.string.field_required)
            valid = false
        }
        if (seeking.isEmpty()) {
            binding.tilSeeking.error = getString(R.string.field_required)
            valid = false
        }
        if (!valid) return

        setLoading(true)
        val isEdit = editListingId != null
        lifecycleScope.launch {
            try {
                // Firestore чека потврда од серверот. Без интернет записот се реди
                // локално и се синхронизира подоцна, па по истек на времето
                // продолжуваме како успех наместо да виси спинерот засекогаш.
                withTimeoutOrNull(WRITE_TIMEOUT_MS) {
                    val id = editListingId
                    if (id != null) {
                        repository.updateListing(id, offering, seeking, description)
                    } else {
                        val user = FirebaseAuth.getInstance().currentUser
                        // Константа (не локализиран стринг) — ова се запишува во базата
                        val userName = user?.displayName ?: user?.email ?: ANONYMOUS_USER_NAME
                        repository.addListing(
                            mapOf(
                                ListingRepository.FIELD_USER_ID to (user?.uid ?: ""),
                                ListingRepository.FIELD_USER_NAME to userName,
                                ListingRepository.FIELD_OFFERING to offering,
                                ListingRepository.FIELD_SEEKING to seeking,
                                ListingRepository.FIELD_DESCRIPTION to description,
                                ListingRepository.FIELD_TIMESTAMP to System.currentTimeMillis()
                            )
                        )
                    }
                }
                Toast.makeText(
                    this@AddListingActivity,
                    if (isEdit) R.string.listing_updated else R.string.listing_published,
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(
                    this@AddListingActivity, R.string.error_generic, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnPublish.isEnabled = !loading
    }

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_OFFERING = "extra_offering"
        const val EXTRA_SEEKING = "extra_seeking"
        const val EXTRA_DESCRIPTION = "extra_description"

        private const val WRITE_TIMEOUT_MS = 8000L
        private const val ANONYMOUS_USER_NAME = "Анонимен"
    }
}
