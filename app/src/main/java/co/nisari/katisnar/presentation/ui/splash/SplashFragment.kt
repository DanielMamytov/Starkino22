package co.nisari.katisnar.presentation.ui.splash

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.redProgress.animateToWithDelay(target = 72, delayMs = 400, durationMs = 700)

        viewLifecycleOwner.lifecycleScope.launch {
            delay(SPLASH_DELAY_MS)

            val prefs = requireContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            val onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
            val navController = findNavController()
            val destination = if (onboardingCompleted) {
                SplashFragmentDirections.actionSplashFragmentToDashBoardFragment()
            } else {
                SplashFragmentDirections.actionSplashFragmentToOnboardFragment()
            }

            navController.navigate(
                destination,
                navOptions {
                    popUpTo(R.id.splashFragment) {
                        inclusive = true
                    }
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PREFS_NAME = "starkino_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val SPLASH_DELAY_MS = 2500L
    }
}