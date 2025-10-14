package co.nisari.katisnar.presentation.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentSettingsBinding
import co.nisari.katisnar.presentation.data.local.StarDatabase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var database: StarDatabase

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnShareTheApp.setOnClickListener { shareTheApp() }
        binding.btnRateUs.setOnClickListener { rateUs() }
        binding.btnPrivacyPolicy.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_privacyPolicyFragment)
        }
        binding.btnTermsOfUse.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_termsOfUseFragment)
        }
        binding.btnClearData.setOnClickListener { clearAppData() }
    }

    private fun shareTheApp() {
        val context = requireContext()
        val packageName = context.packageName
        val shareMessage = getString(R.string.share_app_message, packageName)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_the_app)))
    }

    private fun rateUs() {
        val context = requireContext()
        val packageName = context.packageName
        val marketUri = Uri.parse("market://details?id=$packageName")
        val goToMarketIntent = Intent(Intent.ACTION_VIEW, marketUri)
        try {
            startActivity(goToMarketIntent)
        } catch (exception: ActivityNotFoundException) {
            val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    private fun clearAppData() {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                database.clearAllTables()
            }
            Toast.makeText(requireContext(), R.string.data_cleared_message, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
