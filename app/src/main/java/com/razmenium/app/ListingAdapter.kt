package com.razmenium.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListingAdapter(
    private val listings: List<Listing>,
    private val onFavoriteClick: ((Listing) -> Unit)? = null
) : RecyclerView.Adapter<ListingAdapter.ListingViewHolder>() {

    class ListingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOffering: TextView = itemView.findViewById(R.id.tvOffering)
        val tvSeeking: TextView = itemView.findViewById(R.id.tvSeeking)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listing, parent, false)
        return ListingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        val listing = listings[position]
        holder.tvOffering.text = "${holder.itemView.context.getString(R.string.offering_label)}: ${listing.offering}"
        holder.tvSeeking.text = "${holder.itemView.context.getString(R.string.seeking_label)}: ${listing.seeking}"
        holder.tvUserName.text = listing.userName
        holder.tvDescription.text = listing.description

        var isFavorite = false

        holder.btnFavorite.setOnClickListener {
            isFavorite = !isFavorite
            if (isFavorite) {
                holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_on)
                onFavoriteClick?.invoke(listing)
            } else {
                holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_off)
            }
        }
    }

    override fun getItemCount() = listings.size
}