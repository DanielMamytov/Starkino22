package co.nisari.katisnar.presentation.ui.onboarding

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentOnboardBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardFragment : Fragment() {

    private var _binding: FragmentOnboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBeginTheJourney.setOnClickListener {
            completeOnboarding()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun completeOnboarding() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()

        findNavController().navigate(
            OnboardFragmentDirections.actionOnboardFragmentToDashBoardFragment(),
            navOptions {
                popUpTo(R.id.onboardFragment) {
                    inclusive = true
                }
            }
        )
    }

    companion object {
        private const val PREFS_NAME = "starkino_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}