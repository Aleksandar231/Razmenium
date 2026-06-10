package com.razmenium.app

import android.annotation.SuppressLint
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.razmenium.app.databinding.ItemListingBinding

class ListingAdapter(
    private val onFavoriteClick: (Listing) -> Unit,
    private val onEditClick: ((Listing) -> Unit)? = null,
    private val onDeleteClick: ((Listing) -> Unit)? = null,
    private val showOwnerActions: Boolean = true
) : ListAdapter<Listing, ListingAdapter.ListingViewHolder>(DIFF_CALLBACK) {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    /** Сет од ID-а на омилени огласи — ѕвездичката останува и по скролање. */
    var favoriteIds: Set<String> = emptySet()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ListingViewHolder(val binding: ItemListingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val binding = ItemListingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ListingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        val listing = getItem(position)
        val binding = holder.binding
        val context = binding.root.context

        binding.tvUserName.text = listing.userName
        binding.tvOffering.text = context.getString(R.string.offering_format, listing.offering)
        binding.tvSeeking.text = context.getString(R.string.seeking_format, listing.seeking)

        binding.tvDescription.text = listing.description
        binding.tvDescription.visibility =
            if (listing.description.isBlank()) View.GONE else View.VISIBLE

        if (listing.timestamp > 0) {
            binding.tvDate.visibility = View.VISIBLE
            binding.tvDate.text = DateUtils.getRelativeTimeSpanString(
                listing.timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
        } else {
            binding.tvDate.visibility = View.GONE
        }

        val isFavorite = listing.id in favoriteIds
        binding.btnFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_star_filled else R.drawable.ic_star_outline
        )
        binding.btnFavorite.setOnClickListener { onFavoriteClick(listing) }

        // Измена и бришење само за сопствените огласи
        val isOwner = showOwnerActions && listing.userId == currentUserId
        binding.btnEdit.visibility = if (isOwner && onEditClick != null) View.VISIBLE else View.GONE
        binding.btnDelete.visibility = if (isOwner && onDeleteClick != null) View.VISIBLE else View.GONE
        binding.btnEdit.setOnClickListener { onEditClick?.invoke(listing) }
        binding.btnDelete.setOnClickListener { onDeleteClick?.invoke(listing) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Listing>() {
            override fun areItemsTheSame(oldItem: Listing, newItem: Listing) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Listing, newItem: Listing) =
                oldItem == newItem
        }
    }
}
