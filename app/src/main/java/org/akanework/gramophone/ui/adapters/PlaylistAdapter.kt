package org.akanework.gramophone.ui.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentManager
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import org.akanework.gramophone.MainActivity
import org.akanework.gramophone.R
import org.akanework.gramophone.logic.utils.MediaStoreUtils
import org.akanework.gramophone.ui.fragments.GeneralSubFragment

/**
 * [PlaylistAdapter] is an adapter for displaying artists.
 */
@androidx.annotation.OptIn(UnstableApi::class)
class PlaylistAdapter(
    private val playlistList: MutableList<MediaStoreUtils.Playlist>,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val mainActivity: MainActivity,
) : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder =
        ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.adapter_list_card_larger, parent, false),
        )

    override fun getItemCount(): Int = playlistList.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.songTitle.text =
            playlistList[position].title.ifEmpty {
                context.getString(R.string.unknown_playlist)
            }
        val songText =
            playlistList[position].songList.size.toString() + ' ' +
                if (playlistList[position].songList.size <= 1) {
                    context.getString(R.string.song)
                } else {
                    context.getString(R.string.songs)
                }
        holder.songArtist.text = songText

        Glide
            .with(holder.songCover.context)
            .load(
                playlistList[position]
                    .songList
                    .first()
                    .mediaMetadata
                    .artworkUri,
            ).placeholder(R.drawable.ic_default_cover_date)
            .into(holder.songCover)

        holder.itemView.setOnClickListener {
            fragmentManager
                .beginTransaction()
                .addToBackStack("SUBFRAG")
                .replace(
                    R.id.container,
                    GeneralSubFragment().apply {
                        arguments =
                            Bundle().apply {
                                putInt("Position", position)
                                putInt("Item", 6)
                                putString("Title", holder.songTitle.text as String)
                            }
                    },
                ).commit()
        }

        holder.moreButton.setOnClickListener {
            val popupMenu = PopupMenu(it.context, it)
            popupMenu.inflate(R.menu.more_menu_less)

            popupMenu.setOnMenuItemClickListener { it1 ->
                when (it1.itemId) {
                    R.id.play_next -> {
                        val mediaController = mainActivity.getPlayer()
                        mediaController.addMediaItems(
                            mediaController.currentMediaItemIndex + 1,
                            playlistList[holder.bindingAdapterPosition].songList,
                        )
                    }

                    R.id.details -> {
                    }
                }
                true
            }
            popupMenu.show()
        }
    }

    inner class ViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {
        val songCover: ImageView = view.findViewById(R.id.cover)
        val songTitle: TextView = view.findViewById(R.id.title)
        val songArtist: TextView = view.findViewById(R.id.artist)
        val moreButton: MaterialButton = view.findViewById(R.id.more)
    }

    fun updateList(newList: MutableList<MediaStoreUtils.Playlist>) {
        val diffResult = DiffUtil.calculateDiff(SongDiffCallback(playlistList, newList))
        playlistList.clear()
        playlistList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    private class SongDiffCallback(
        private val oldList: MutableList<MediaStoreUtils.Playlist>,
        private val newList: MutableList<MediaStoreUtils.Playlist>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int,
        ) = oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun areContentsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int,
        ) = oldList[oldItemPosition] == newList[newItemPosition]
    }
}