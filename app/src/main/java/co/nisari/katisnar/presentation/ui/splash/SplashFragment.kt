package co.nisari.katisnar.presentation.ui.splash

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

    private lateinit var binding: FragmentSplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplashBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.redProgress.animateToWithDelay(target = 72, delayMs = 400, durationMs = 700)

//        Handler(Looper.getMainLooper()).postDelayed({
//            val prefs = getSharedPreferences("starkino_prefs", MODE_PRIVATE)
//            val firstLaunch = prefs.getBoolean("firstLaunch", true)
//
//            if (firstLaunch) {
//                prefs.edit().putBoolean("firstLaunch", false).apply()
//                startActivity(Intent(this, OnboardingActivity::class.java))
//            } else {
//                startActivity(Intent(this, DashboardActivity::class.java))
//            }
//            finish()
//        }, 2500)

    }
}