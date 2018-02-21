package com.gjsalot.books.ui.volumes_list

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gjsalot.books.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.volume_item.view.*

class VolumesListAdapter(
        private val listener: Listener
): RecyclerView.Adapter<VolumesListAdapter.VolumeViewHolder>() {

    interface Listener {
        fun onScrolledNearBottom()
        fun onVolumeClicked(volume: VolumesListViewModel.Volume)
    }

    private var volumes: List<VolumesListViewModel.Volume> = emptyList()

    fun updateVolumes(volumes: List<VolumesListViewModel.Volume>, diffResult: DiffUtil.DiffResult?) {
        this.volumes = volumes

        if (diffResult != null)
            diffResult.dispatchUpdatesTo(this)
        else
            notifyDataSetChanged()
    }

    override fun getItemCount() = volumes.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VolumeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.volume_item, parent, false))

    override fun onBindViewHolder(holder: VolumeViewHolder, i: Int) {
        holder.bind(volumes[i])

        if (i >= volumes.size - 10)
            listener.onScrolledNearBottom()
    }

    inner class VolumeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private var volume: VolumesListViewModel.Volume? = null

        init {
            itemView.setOnClickListener {
                volume?.let {
                    listener.onVolumeClicked(it)
                }
            }
        }

        fun bind(volume: VolumesListViewModel.Volume) {
            this.volume = volume

            itemView.title.text = volume.title

            if (volume.thumbnail != null) {
                Picasso
                        .with(itemView.context)
                        .load(volume.thumbnail)
                        .into(itemView.thumbnail)
            } else {
                itemView.thumbnail.setImageResource(0)
            }
        }

    }

}