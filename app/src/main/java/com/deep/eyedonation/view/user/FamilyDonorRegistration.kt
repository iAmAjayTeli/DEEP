package com.deep.eyedonation.view.user

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.deep.eyedonation.R
import com.deep.eyedonation.databinding.ActivityFamilyDonorRegistrationBinding
import com.deep.eyedonation.handler.LoadingDialog
import com.deep.eyedonation.model.DonorRegistrationCrud
import com.deep.eyedonation.model.RegisteredEyeBankCrud
import com.deep.eyedonation.service.NotificationService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Family Donor Registration Screen
 * This form is for family members of a deceased person to request eye donation
 */
class FamilyDonorRegistration : AppCompatActivity() {
    private lateinit var binding: ActivityFamilyDonorRegistrationBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Store selected eye bank details
    private var selectedEyeBankName: String = ""
    private var selectedEyeBankAddress: String = ""
    private var selectedEyeBankLatitude: Double = 0.0
    private var selectedEyeBankLongitude: Double = 0.0
    private var selectedEyeBankEmail: String = ""

    // Gender options
    private val genderOptions = arrayOf("Male", "Female", "Other")

    // Location permission launcher
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (!fineLocationGranted && !coarseLocationGranted) {
            Snackbar.make(binding.root, "Location permission helps find nearest eye banks", Snackbar.LENGTH_LONG).show()
        }
    }

    // Activity result launcher for Eye Bank Search
    private val eyeBankSearchLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                selectedEyeBankName = data.getStringExtra(EyeBankSearchScreen.EXTRA_EYE_BANK_NAME) ?: ""
                selectedEyeBankAddress = data.getStringExtra(EyeBankSearchScreen.EXTRA_EYE_BANK_ADDRESS) ?: ""
                selectedEyeBankLatitude = data.getDoubleExtra(EyeBankSearchScreen.EXTRA_EYE_BANK_LATITUDE, 0.0)
                selectedEyeBankLongitude = data.getDoubleExtra(EyeBankSearchScreen.EXTRA_EYE_BANK_LONGITUDE, 0.0)

                // Auto-fill the selected eye bank fields
                binding.selectedEyeBank.setText(selectedEyeBankName)
                binding.eyeBankAddress.setText(selectedEyeBankAddress)

                // Try to find email from registered eye banks
                fetchEyeBankEmail(selectedEyeBankName)
            }
        }
    }

    private fun fetchEyeBankEmail(eyeBankName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val registeredBank = RegisteredEyeBankCrud.findEyeBankByName(eyeBankName)
                if (registeredBank != null) {
                    selectedEyeBankEmail = registeredBank.email
                    Log.d("FamilyDonorRegistration", "Found email for eye bank: $selectedEyeBankEmail")
                }
            } catch (e: Exception) {
                Log.e("FamilyDonorRegistration", "Error fetching eye bank email: ${e.message}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_family_donor_registration)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadingDialog = LoadingDialog(this, "Please wait...")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupGenderDropdown()
        setupDateTimePicker()
        clickListener()
        setupErrorClearing()
        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun setupGenderDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)
        binding.donorGender.setAdapter(adapter)
    }

    private fun setupDateTimePicker() {
        binding.deathDateTime.setOnClickListener {
            showDateTimePicker()
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)

                        val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        binding.deathDateTime.setText(dateTimeFormat.format(calendar.time))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun clickListener() {
        binding.apply {
            backButton.setOnClickListener {
                onBackPressed()
            }

            fetchLocationBtn.setOnClickListener {
                fetchCurrentLocation()
            }

            selectEyeBankBtn.setOnClickListener {
                val intent = Intent(this@FamilyDonorRegistration, EyeBankSearchScreen::class.java)
                eyeBankSearchLauncher.launch(intent)
            }

            selectedEyeBank.setOnClickListener {
                val intent = Intent(this@FamilyDonorRegistration, EyeBankSearchScreen::class.java)
                eyeBankSearchLauncher.launch(intent)
            }

            submitBtn.setOnClickListener {
                validateAndSubmit()
            }
        }
    }

    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
            Snackbar.make(binding.root, "Please grant location permission", Snackbar.LENGTH_SHORT).show()
            return
        }

        binding.fetchLocationBtn.text = "Fetching location..."
        binding.fetchLocationBtn.isEnabled = false

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val geocoder = Geocoder(this@FamilyDonorRegistration, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                        withContext(Dispatchers.Main) {
                            if (!addresses.isNullOrEmpty()) {
                                val address = addresses[0]
                                val fullAddress = buildString {
                                    address.getAddressLine(0)?.let { append(it) }
                                }
                                binding.fullAddress.setText(fullAddress)
                                Snackbar.make(binding.root, "Location fetched successfully!", Snackbar.LENGTH_SHORT).show()
                            } else {
                                Snackbar.make(binding.root, "Could not get address", Snackbar.LENGTH_SHORT).show()
                            }
                            binding.fetchLocationBtn.text = "Fetch My Location"
                            binding.fetchLocationBtn.isEnabled = true
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_SHORT).show()
                            binding.fetchLocationBtn.text = "Fetch My Location"
                            binding.fetchLocationBtn.isEnabled = true
                        }
                    }
                }
            } else {
                binding.fetchLocationBtn.text = "Fetch My Location"
                binding.fetchLocationBtn.isEnabled = true
                Snackbar.make(binding.root, "Unable to get location. Please try again.", Snackbar.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            binding.fetchLocationBtn.text = "Fetch My Location"
            binding.fetchLocationBtn.isEnabled = true
            Snackbar.make(binding.root, "Failed to get location", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun validateAndSubmit() {
        binding.apply {
            val donorNameVal = donorName.text.toString().trim()
            val donorAgeVal = donorAge.text.toString().trim()
            val donorGenderVal = donorGender.text.toString().trim()
            val deathDateTimeVal = deathDateTime.text.toString().trim()
            val causeOfDeathVal = causeOfDeath.text.toString().trim()
            val contactPersonNameVal = contactPersonName.text.toString().trim()
            val relationshipVal = relationship.text.toString().trim()
            val fullAddressVal = fullAddress.text.toString().trim()
            val contactNumberVal = contactNumber.text.toString().trim()
            val isDetailsCorrect = isCorrect.isChecked

            when {
                donorNameVal.isEmpty() -> {
                    donorName.error = "Donor's name is required"
                    donorName.requestFocus()
                }

                donorAgeVal.isEmpty() -> {
                    donorAge.error = "Donor's age is required"
                    donorAge.requestFocus()
                }

                donorGenderVal.isEmpty() -> {
                    Snackbar.make(root, "Please select gender", Snackbar.LENGTH_SHORT).show()
                }

                deathDateTimeVal.isEmpty() -> {
                    Snackbar.make(root, "Please select date & time of death", Snackbar.LENGTH_SHORT).show()
                }

                causeOfDeathVal.isEmpty() -> {
                    causeOfDeath.error = "Cause of death is required"
                    causeOfDeath.requestFocus()
                }

                contactPersonNameVal.isEmpty() -> {
                    contactPersonName.error = "Contact person name is required"
                    contactPersonName.requestFocus()
                }

                relationshipVal.isEmpty() -> {
                    relationship.error = "Relationship is required"
                    relationship.requestFocus()
                }

                fullAddressVal.isEmpty() -> {
                    fullAddress.error = "Full address is required"
                    fullAddress.requestFocus()
                }

                contactNumberVal.isEmpty() -> {
                    contactNumber.error = "Contact number is required"
                    contactNumber.requestFocus()
                }

                contactNumberVal.length < 10 -> {
                    contactNumber.error = "Invalid contact number"
                    contactNumber.requestFocus()
                }

                selectedEyeBankName.isEmpty() -> {
                    Snackbar.make(root, "Please select an eye bank", Snackbar.LENGTH_SHORT).show()
                }

                !isDetailsCorrect -> {
                    Snackbar.make(root, "Please confirm that the information is accurate", Snackbar.LENGTH_SHORT).show()
                }

                else -> {
                    loadingDialog.startLoading()
                    clearErrors()

                    // Create registration data
                    val registrationData = FamilyDonorData(
                        donorName = donorNameVal,
                        donorAge = donorAgeVal,
                        donorGender = donorGenderVal,
                        deathDateTime = deathDateTimeVal,
                        causeOfDeath = causeOfDeathVal,
                        contactPersonName = contactPersonNameVal,
                        relationship = relationshipVal,
                        fullAddress = fullAddressVal,
                        contactNumber = contactNumberVal,
                        eyeBankName = selectedEyeBankName,
                        eyeBankAddress = selectedEyeBankAddress
                    )

                    saveToDatabase(registrationData)
                }
            }
        }
    }

    data class FamilyDonorData(
        val donorName: String,
        val donorAge: String,
        val donorGender: String,
        val deathDateTime: String,
        val causeOfDeath: String,
        val contactPersonName: String,
        val relationship: String,
        val fullAddress: String,
        val contactNumber: String,
        val eyeBankName: String,
        val eyeBankAddress: String
    )

    private fun saveToDatabase(data: FamilyDonorData) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Create a DonorRegistration object for family registration
                val donorRegistration = com.deep.eyedonation.model.DonorRegistration(
                    donorName = data.donorName,
                    donorAge = data.donorAge,
                    donorGender = data.donorGender,
                    deathDateTime = data.deathDateTime,
                    causeOfDeath = data.causeOfDeath,
                    hospitalName = "Family Registration", // Mark as family registration
                    department = "N/A",
                    staffName = data.contactPersonName,
                    staffContact = data.contactNumber,
                    hospitalLocation = data.fullAddress,
                    eyeBankName = data.eyeBankName,
                    eyeBankAddress = data.eyeBankAddress,
                    eyeBankLatitude = selectedEyeBankLatitude,
                    eyeBankLongitude = selectedEyeBankLongitude,
                    status = "pending_family" // Different status for family registrations
                )

                val registrationId = DonorRegistrationCrud.addDonorRegistration(donorRegistration)

                // Create notification entry
                NotificationService.createEyeBankNotification(donorRegistration, registrationId)

                // Try to find email
                if (selectedEyeBankEmail.isEmpty()) {
                    val registeredBank = RegisteredEyeBankCrud.findEyeBankByName(data.eyeBankName)
                    if (registeredBank != null) {
                        selectedEyeBankEmail = registeredBank.email
                    }
                }

                withContext(Dispatchers.Main) {
                    loadingDialog.isDismiss()
                    showRegistrationSavedDialog(data)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    loadingDialog.isDismiss()
                    Snackbar.make(
                        binding.root,
                        "Failed to submit: ${e.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                    Log.e("FamilyDonorRegistration", "${e.message}")
                }
            }
        }
    }

    private fun showRegistrationSavedDialog(data: FamilyDonorData) {
        AlertDialog.Builder(this)
            .setTitle("✅ Registration Saved!")
            .setMessage("Donor registration has been saved successfully.\n\nDo you want to notify the Eye Bank?")
            .setPositiveButton("Yes, Notify") { _, _ ->
                sendEmailToEyeBank(data)
            }
            .setNegativeButton("No, Skip") { _, _ ->
                goToConfirmationScreen()
            }
            .setCancelable(false)
            .show()
    }

    private fun sendEmailToEyeBank(data: FamilyDonorData) {
        val subject = "🚨 URGENT: Eye Donation Request - Family Registration"

        val body = """
Dear Eye Bank Team,

This is an urgent notification regarding an eye donation opportunity from a family member.

══════════════════════════════════
         DONOR DETAILS
══════════════════════════════════
• Name: ${data.donorName}
• Age: ${data.donorAge} years
• Gender: ${data.donorGender}
• Time of Death: ${data.deathDateTime}
• Cause of Death: ${data.causeOfDeath}

══════════════════════════════════
         FAMILY MEMBER DETAILS
══════════════════════════════════
• Name: ${data.contactPersonName}
• Relationship: ${data.relationship}
• Address: ${data.fullAddress}
• Phone: +91 ${data.contactNumber}

══════════════════════════════════
         ⏰ TIME CRITICAL
══════════════════════════════════
Eyes must be retrieved within 6 hours of death for successful transplantation.

Please respond immediately and coordinate the eye retrieval process.

Thank you for your prompt attention to this matter.

Best regards,
${data.contactPersonName}

---
Sent via DEEP Eye Donation App
        """.trimIndent()

        if (selectedEyeBankEmail.isNotEmpty()) {
            openGmailWithEmail(selectedEyeBankEmail, subject, body)
        } else {
            promptForEmailAndSend(data, subject, body)
        }
    }

    private fun openGmailWithEmail(email: String, subject: String, body: String) {
        try {
            val gmailIntent = Intent(Intent.ACTION_SENDTO).apply {
                this.data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                setPackage("com.google.android.gm")
            }

            startActivity(gmailIntent)
            showAfterEmailDialog()

        } catch (e: Exception) {
            try {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$email")
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                startActivity(Intent.createChooser(emailIntent, "Send Email"))
                showAfterEmailDialog()
            } catch (ex: Exception) {
                Snackbar.make(binding.root, "No email app found", Snackbar.LENGTH_LONG).show()
                goToConfirmationScreen()
            }
        }
    }

    private fun promptForEmailAndSend(data: FamilyDonorData, subject: String, body: String) {
        val input = EditText(this).apply {
            hint = "Enter Eye Bank Email"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setPadding(50, 30, 50, 30)
        }

        AlertDialog.Builder(this)
            .setTitle("Enter Eye Bank Email")
            .setMessage("Email for ${data.eyeBankName}")
            .setView(input)
            .setPositiveButton("Send Email") { _, _ ->
                val email = input.text.toString().trim()
                if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    openGmailWithEmail(email, subject, body)
                } else {
                    Snackbar.make(binding.root, "Please enter a valid email", Snackbar.LENGTH_SHORT).show()
                    promptForEmailAndSend(data, subject, body)
                }
            }
            .setNegativeButton("Skip") { _, _ ->
                goToConfirmationScreen()
            }
            .setCancelable(false)
            .show()
    }

    private fun showAfterEmailDialog() {
        AlertDialog.Builder(this)
            .setTitle("Email Sent!")
            .setMessage("Please make sure to send the email from your Gmail app.\n\nDo you want to continue?")
            .setPositiveButton("Continue") { _, _ ->
                goToConfirmationScreen()
            }
            .setCancelable(false)
            .show()
    }

    private fun goToConfirmationScreen() {
        startActivity(Intent(this@FamilyDonorRegistration, HospitalConfirmedScreen::class.java))
        finish()
    }

    private fun clearErrors() {
        binding.apply {
            donorName.error = null
            donorAge.error = null
            causeOfDeath.error = null
            contactPersonName.error = null
            relationship.error = null
            fullAddress.error = null
            contactNumber.error = null
        }
    }

    private fun setupErrorClearing() {
        binding.apply {
            donorName.addTextChangedListener { clearError(donorNameLayout) }
            donorAge.addTextChangedListener { clearError(donorAgeLayout) }
            causeOfDeath.addTextChangedListener { clearError(causeOfDeathLayout) }
            contactPersonName.addTextChangedListener { clearError(contactPersonNameLayout) }
            relationship.addTextChangedListener { clearError(relationshipLayout) }
            fullAddress.addTextChangedListener { clearError(fullAddressLayout) }
            contactNumber.addTextChangedListener { contactNumber.error = null }
        }
    }

    private fun clearError(vararg views: Any) {
        views.forEach {
            when (it) {
                is TextInputLayout -> it.error = null
                is EditText -> it.error = null
            }
        }
    }
}
