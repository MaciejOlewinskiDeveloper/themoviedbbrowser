package net.olewinski.themoviedbbrowser.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import net.olewinski.themoviedbbrowser.databinding.FragmentMoviesCollectionBinding

class MoviesCollectionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentMoviesCollectionBinding.inflate(inflater, container, false).root
}
