package co.nisari.katisnar.presentation.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.nisari.katisnar.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashBoardFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dash_board, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<FrameLayout>(R.id.btn_star_location).setOnClickListener {
            findNavController().navigate(R.id.action_dashBoardFragment_to_starLocationFragment)
        }

        view.findViewById<FrameLayout>(R.id.btn_star_routes).setOnClickListener {
            findNavController().navigate(R.id.action_dashBoardFragment_to_starRouteFragment)
        }

        view.findViewById<FrameLayout>(R.id.btn_star_notes).setOnClickListener {
            findNavController().navigate(R.id.action_dashBoardFragment_to_starNoteFragment)
        }

        view.findViewById<FrameLayout>(R.id.btn_starkino_articles).setOnClickListener {
            findNavController().navigate(R.id.action_dashBoardFragment_to_starArticleFragment)
        }

        view.findViewById<FrameLayout>(R.id.btn_setting).setOnClickListener {
            findNavController().navigate(R.id.action_dashBoardFragment_to_settingsFragment)
        }
    }
}
