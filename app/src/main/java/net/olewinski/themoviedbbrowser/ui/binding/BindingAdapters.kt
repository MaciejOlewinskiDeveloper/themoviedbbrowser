package net.olewinski.themoviedbbrowser.ui.binding

import android.net.Uri
import androidx.databinding.BindingAdapter
import com.facebook.drawee.view.SimpleDraweeView

/**
 * [BindingAdapter] for Android Data Binding library to handle loading images from URLs.
 */
@BindingAdapter("app:imageUrl")
fun loadFromUrlToSimpleDraweeView(view: SimpleDraweeView, imageUrl: String) {
    view.setImageURI(Uri.parse(imageUrl), null)
}

