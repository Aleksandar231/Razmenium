package com.razmenium.app

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.razmenium.app.databinding.ActivityProfileBinding
import kotlinx.coroutines.launch

class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private val repository by lazy { ListingRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        binding.toolbar.setNavigationOnClickListener { finish() }

        val user = auth.currentUser
        binding.tvName.text = user?.displayName
            ?: user?.email
            ?: getString(R.string.anonymous_user)
        binding.tvEmail.text = getString(
            R.string.profile_email_format,
            user?.email ?: getString(R.string.anonymous_user)
        )
        binding.tvUserId.text = getString(R.string.profile_id_format, user?.uid.orEmpty())

        binding.tvMyListingsCount.text = getString(R.string.my_listings_count, 0)
        binding.tvFavoritesCount.text = getString(R.string.favorites_count, 0)

        lifecycleScope.launch {
            binding.tvFavoritesCount.text =
                getString(R.string.favorites_count, repository.favoritesCount())

            val uid = user?.uid
            if (uid != null) {
                try {
                    binding.tvMyListingsCount.text =
                        getString(R.string.my_listings_count, repository.countUserListings(uid))
                } catch (_: Exception) {
                    // без интернет — остави 0
                }
            }
        }

        binding.btnLogout.setOnClickListener { logoutAndExit() }
    }
}
