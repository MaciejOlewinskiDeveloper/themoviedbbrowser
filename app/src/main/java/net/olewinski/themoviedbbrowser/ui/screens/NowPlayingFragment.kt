package net.olewinski.themoviedbbrowser.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import net.olewinski.themoviedbbrowser.databinding.FragmentNowPlayingBinding
import net.olewinski.themoviedbbrowser.viewmodels.NowPlayingViewModel

class NowPlayingFragment : Fragment() {

    private lateinit var nowPlayingBinding: FragmentNowPlayingBinding
    private lateinit var nowPlayingViewModel: NowPlayingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        nowPlayingBinding = FragmentNowPlayingBinding.inflate(inflater, container, false)
        nowPlayingBinding.lifecycleOwner = viewLifecycleOwner

        return nowPlayingBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
