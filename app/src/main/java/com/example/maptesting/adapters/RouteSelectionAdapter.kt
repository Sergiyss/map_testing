package com.example.maptesting.adapters

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.maptesting.R
import com.example.maptesting.data.building_route.Duration

class RouteSelectionAdapter(layoutResId: Int, data: MutableList<Duration> ) :  BaseQuickAdapter<Duration, BaseViewHolder>(layoutResId, data) {
    override fun convert(holder: BaseViewHolder, item: Duration) {
        holder.setText(R.id.pickUpTextView , item.text)
    }

}