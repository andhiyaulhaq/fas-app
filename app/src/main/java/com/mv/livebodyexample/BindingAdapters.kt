package com.mv.livebodyexample

import android.content.res.ColorStateList
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter

object BindingAdapters {
    @JvmStatic
    @BindingAdapter("app:drawableTint")
    fun setDrawableTint(view: AppCompatTextView, color: Int) {
        TextViewCompat.setCompoundDrawableTintList(view, ColorStateList.valueOf(color))
    }
}
