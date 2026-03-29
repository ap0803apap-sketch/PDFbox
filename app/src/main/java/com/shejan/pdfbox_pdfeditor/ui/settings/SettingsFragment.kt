package com.shejan.pdfbox_pdfeditor.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.ap.pdf.box.R
import com.ap.pdf.box.databinding.FragmentSettingsBinding
import com.shejan.pdfbox_pdfeditor.util.ThemeManager
import java.io.File

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var themeManager: ThemeManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        themeManager = ThemeManager(requireContext())
        setupSettings()
        setupDeveloperSection()
    }

    private fun setupSettings() {
        // Theme Selection
        val currentMode = themeManager.getThemeMode()
        when (currentMode) {
            ThemeManager.THEME_LIGHT -> binding.rbLight.isChecked = true
            ThemeManager.THEME_DARK -> binding.rbDark.isChecked = true
            ThemeManager.THEME_SYSTEM -> binding.rbSystem.isChecked = true
        }

        binding.rgTheme.setOnCheckedChangeListener { _: RadioGroup, checkedId: Int ->
            val newMode = when (checkedId) {
                R.id.rbLight -> ThemeManager.THEME_LIGHT
                R.id.rbDark -> ThemeManager.THEME_DARK
                else -> ThemeManager.THEME_SYSTEM
            }
            if (newMode != themeManager.getThemeMode()) {
                themeManager.setThemeMode(newMode)
                requireActivity().recreate()
            }
        }

        // Dynamic Colors
        binding.switchDynamic.apply {
            isEnabled = themeManager.isDynamicColorsAvailable()
            isChecked = themeManager.isDynamicColorsEnabled()
            setOnCheckedChangeListener { _, isChecked ->
                themeManager.setDynamicColors(isChecked)
                requireActivity().recreate()
            }
        }

        // AMOLED Mode
        binding.switchAmoled.apply {
            isChecked = themeManager.isAmoledModeEnabled()
            setOnCheckedChangeListener { _, isChecked ->
                themeManager.setAmoledMode(isChecked)
                if (themeManager.isDarkThemeActive()) {
                    requireActivity().recreate()
                }
            }
        }
        
        // Save Path Display
        val appName = getString(R.string.app_name)
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appFolder = File(downloadDir, appName)
        binding.txtSavePath.text = appFolder.absolutePath
    }

    private fun setupDeveloperSection() {
        binding.btnEmail.setOnClickListener {
            val email = getString(R.string.developer_email)
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, "Feedback for PDF Box")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                // No email app installed
            }
        }

        binding.btnGithub.setOnClickListener {
            val url = getString(R.string.developer_github_url)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
