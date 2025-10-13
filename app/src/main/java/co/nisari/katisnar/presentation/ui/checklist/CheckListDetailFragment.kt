package co.nisari.katisnar.presentation.ui.checklist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentCheckListDetailBinding


class CheckListDetailFragment : Fragment() {

    private lateinit var binding: FragmentCheckListDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCheckListDetailBinding.inflate(inflater,container,false)
        return binding.root
    }


}