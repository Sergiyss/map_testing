package com.example.maptesting.adapters

import android.content.Context
import android.graphics.Typeface
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.example.maptesting.R
import com.example.maptesting.data.PlaceDataModel
import com.example.maptesting.google_map_util.getAutocomplete
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.*


class PlaceArrayAdapter(context: Context, val resource: Int, val mPlacesClient: PlacesClient) :
    ArrayAdapter<PlaceDataModel>(context, resource),
    Filterable{

    private var resultList = arrayListOf<PlaceDataModel>()

    override fun getCount(): Int {
        return when {
            resultList.isNullOrEmpty() -> 0
            else -> resultList.size
        }
    }

    override fun getItem(position: Int): PlaceDataModel? {
        return when {
            resultList.isNullOrEmpty() -> null
            else -> resultList[position]
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val viewHolder: ViewHolder
        if (view == null) {
            viewHolder = ViewHolder()
            view = LayoutInflater.from(context).inflate(resource, parent, false)
            viewHolder.description = view.findViewById(R.id.searchFullText) as TextView
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }
        bindView(viewHolder, resultList, position)
        return view!!
    }

    private fun bindView(viewHolder: ViewHolder, place: ArrayList<PlaceDataModel>, position: Int) {
        if (!place.isNullOrEmpty()) {
            viewHolder.description?.text = place[position].fullText
        }
    }

     override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged() //!!!!
                } else {
                    notifyDataSetInvalidated()
                }
            }

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    resultList.clear()
                    val address = getAutocomplete(mPlacesClient, constraint.toString())
                    address?.let {
                        for (i in address.indices) {
                            val item = address[i]
                            resultList.add(PlaceDataModel(item.placeId, item.getFullText(StyleSpan(
                                Typeface.BOLD)).toString()))
                        }
                    }
                    filterResults.values = resultList
                    filterResults.count = resultList.size
                }
                return filterResults
            }
        }
    }

    internal class ViewHolder {
        var description: TextView? = null
    }



    /// НЕ ОТОБРАЖАЕТСЯ СПИСОК

    fun getFilter3(): Filter {
        val placesClient = Places.createClient(context)
        val token = AutocompleteSessionToken.newInstance()

        return object : Filter() {
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                println("----------------------- 6 ")
                if (results != null && results.count > 0) {
                    notifyDataSetChanged() //!!!!
                } else {
                    notifyDataSetInvalidated()
                }
            }

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                filterResults.values = resultList
                filterResults.count = resultList.size

                if (constraint != null) {
                    resultList.clear()
                    val request =
                        FindAutocompletePredictionsRequest.builder()
                            // Call either setLocationBias() OR setLocationRestriction().
                            //  .setLocationBias(bounds)
                            //  .setLocationRestriction(bounds)
                            //  .setOrigin(LatLng(-33.8749937, 151.2041382))
                            .setCountries("UA", "RU")
                            .setTypeFilter(TypeFilter.ADDRESS)
                            .setSessionToken(token)
                            .setQuery(constraint.toString())
                            .build()
                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                            for (prediction in response.autocompletePredictions) {

                                resultList.add(
                                    PlaceDataModel(
                                        prediction.placeId, prediction.getFullText(
                                            StyleSpan(
                                                Typeface.BOLD)
                                        ).toString()
                                    )
                                )
                                println("prediction " + prediction.placeId)
                                println("prediction 1 " +  prediction.placeId)
                                println("prediction 2 " +  prediction.getPrimaryText(null).toString())
                            }
                            filterResults.values = resultList
                            filterResults.count = resultList.size

                            println("resultList.size "+resultList.size)
                        }
                }

                println("-------------- "+filterResults.count)
                return filterResults
            }
        }
    }

}