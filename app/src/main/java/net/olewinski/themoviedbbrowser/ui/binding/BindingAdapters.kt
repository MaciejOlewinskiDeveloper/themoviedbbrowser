package net.olewinski.themoviedbbrowser.ui.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import net.olewinski.themoviedbbrowser.R

@BindingAdapter("imageUrl")
fun loadFromUrlToImageView(view: ImageView, imageUrl: String) {
    Glide.with(view.context)
        .load(imageUrl)
        .error(R.drawable.ic_image_black_64dp)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(view)
}
