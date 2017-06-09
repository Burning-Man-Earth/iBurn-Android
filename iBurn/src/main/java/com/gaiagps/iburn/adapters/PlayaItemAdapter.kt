package com.gaiagps.iburn.adapters

import android.content.Context
import android.location.Location
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.gaiagps.iburn.R
import com.gaiagps.iburn.database.Art
import com.gaiagps.iburn.database.Camp
import com.gaiagps.iburn.database.Event
import com.gaiagps.iburn.database.PlayaItem
import com.gaiagps.iburn.location.LocationProvider
import timber.log.Timber

/**
 * Created by dbro on 6/7/17.
 */
open class PlayaItemAdapter(val context: Context, val listener: AdapterListener) : RecyclerView.Adapter<PlayaItemAdapter.ViewHolder>() {

    var items: List<PlayaItem>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val normalPaddingBottom: Int
    private val lastItemPaddingBottom: Int
    private var deviceLocation: Location? = null

    init {
        // TODO : Trigger re-draw when location available / changed?
        LocationProvider.getLastLocation(context.applicationContext).subscribe { lastLocation -> deviceLocation = lastLocation }

        normalPaddingBottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
        lastItemPaddingBottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80f, context.resources.displayMetrics).toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.listview_playaitem, parent, false)

        val viewHolder = ViewHolder(view)
        setupClickListeners(viewHolder)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items?.get(position)

        item?.let { item ->

            if (item is Art) {
                holder.artistView.visibility = View.VISIBLE
                holder.artistView.text = item.artist

                holder.eventTypeView.visibility = View.GONE
                holder.eventTimeView.visibility = View.GONE

            } else if (item is Camp) {
                holder.artistView.visibility = View.GONE
                holder.eventTypeView.visibility = View.GONE
                holder.eventTimeView.visibility = View.GONE

            } else if (item is Event) {
                holder.eventTypeView.visibility = View.VISIBLE
                holder.eventTimeView.visibility = View.VISIBLE

                holder.eventTypeView.text = item.type
                holder.eventTimeView.text = item.startTimePretty

                holder.artistView.visibility = View.GONE

            } else {
                Timber.e("Unknown Item type! Display behavior will be unexpected")
            }

            holder.titleView.text = item.name
            holder.descView.text = item.description

            AdapterUtils.setDistanceText(deviceLocation, holder.walkTimeView, holder.bikeTimeView,
                    item.latitude, item.longitude)

            if (item.latitude == 0f && item.playaAddress == null) {
                // No location present, hide address views
                holder.addressView.visibility = View.GONE
            } else if (item.playaAddress != null) {
                holder.addressView.visibility = View.VISIBLE
                holder.addressView.text = item.playaAddress
            }

            if (item.isFavorite) {
                holder.favoriteView.setImageResource(R.drawable.ic_heart_full)
            } else {
                holder.favoriteView.setImageResource(R.drawable.ic_heart_empty)
            }

            holder.itemView.tag = item

            if (position == items?.lastIndex) {
                // Set footer padding
                holder.itemView.setPadding(normalPaddingBottom,
                        normalPaddingBottom,
                        normalPaddingBottom,
                        lastItemPaddingBottom)
            } else {
                // Set default padding
                holder.itemView.setPadding(normalPaddingBottom,
                        normalPaddingBottom,
                        normalPaddingBottom,
                        normalPaddingBottom)
            }
        }
    }

    /**
     * Convenience method to setup item click and favorite button click.
     * Splendidly suitable for calling from [.onCreateViewHolder]
     */
    protected fun setupClickListeners(viewHolder: ViewHolder) {
        viewHolder.itemView.setOnClickListener({ view ->
            if (view.tag != null) {
                listener.onItemSelected(view.tag as PlayaItem)
            }
        })

        viewHolder.favoriteView.setOnClickListener({ view ->
            if (view.tag != null) {
                listener.onItemFavoriteButtonSelected(view.tag as PlayaItem)
            }
        })
    }

    open class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val titleView = view.findViewById(R.id.title) as TextView
        val artistView = view.findViewById(R.id.artist) as TextView
        val descView = view.findViewById(R.id.description) as TextView
        val eventTypeView = view.findViewById(R.id.type) as TextView
        val eventTimeView = view.findViewById(R.id.time) as TextView

        val favoriteView = view.findViewById(R.id.heart) as ImageView
        val addressView = view.findViewById(R.id.address) as TextView

        val walkTimeView = view.findViewById(R.id.walk_time) as TextView
        val bikeTimeView = view.findViewById(R.id.bike_time) as TextView
    }

}