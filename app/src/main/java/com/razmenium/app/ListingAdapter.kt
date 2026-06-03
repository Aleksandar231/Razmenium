package com.razmenium.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListingAdapter(private val listings: List<Listing>) :
    RecyclerView.Adapter<ListingAdapter.ListingViewHolder>() {

    class ListingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOffering: TextView = itemView.findViewById(R.id.tvOffering)
        val tvSeeking: TextView = itemView.findViewById(R.id.tvSeeking)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listing, parent, false)
        return ListingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        val listing = listings[position]
        holder.tvOffering.text = "Нудам: ${listing.offering}"
        holder.tvSeeking.text = "Барам: ${listing.seeking}"
        holder.tvUserName.text = listing.userName
        holder.tvDescription.text = listing.description
    }

    override fun getItemCount() = listings.size
}