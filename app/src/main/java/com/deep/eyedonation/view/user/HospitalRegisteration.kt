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
import android.widget.AutoCompleteTextView
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
import com.deep.eyedonation.databinding.ActivityHospitalRegisterationBinding
import com.deep.eyedonation.handler.LoadingDialog
import com.deep.eyedonation.model.DonorRegistration
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

class HospitalRegisteration : AppCompatActivity() {
    private lateinit var binding: ActivityHospitalRegisterationBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Store selected eye bank details
    private var selectedEyeBankName: String = ""
    private var selectedEyeBankAddress: String = ""
    private var selectedEyeBankLatitude: Double = 0.0
    private var selectedEyeBankLongitude: Double = 0.0
    private var selectedEyeBankEmail: String = "" // Store email if found

    // Store hospital location coordinates
    private var hospitalLatitude: Double = 0.0
    private var hospitalLongitude: Double = 0.0

    // Gender options
    private val genderOptions = arrayOf("Male", "Female", "Other")

    // Location permission launcher
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineLocationGranted || coarseLocationGranted) {
            getCurrentLocation()
        } else {
            Snackbar.make(binding.root, "Location permission required for auto-detection", Snackbar.LENGTH_LONG).show()
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
                    Log.d("HospitalRegistration", "Found email for eye bank: $selectedEyeBankEmail")
                }
            } catch (e: Exception) {
                Log.e("HospitalRegistration", "Error fetching eye bank email: ${e.message}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_hospital_registeration)
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

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                hospitalLatitude = location.latitude
                hospitalLongitude = location.longitude

                // Get address from coordinates
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val geocoder = Geocoder(this@HospitalRegisteration, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            val addressText = buildString {
                                if (!address.featureName.isNullOrEmpty()) append(address.featureName).append(", ")
                                if (!address.locality.isNullOrEmpty()) append(address.locality).append(", ")
                                if (!address.subAdminArea.isNullOrEmpty()) append(address.subAdminArea).append(", ")
                                if (!address.adminArea.isNullOrEmpty()) append(address.adminArea)
                                if (!address.postalCode.isNullOrEmpty()) append(" - ").append(address.postalCode)
                            }
                            withContext(Dispatchers.Main) {
                                binding.hospitalLocation.setText(addressText)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("HospitalRegistration", "Geocoder error: ${e.message}")
                        withContext(Dispatchers.Main) {
                            binding.hospitalLocation.setText("${location.latitude}, ${location.longitude}")
                        }
                    }
                }
            } else {
                Snackbar.make(binding.root, "Unable to get current location", Snackbar.LENGTH_SHORT).show()
            }
        }
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

        // Date Picker
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // After date is selected, show Time Picker
                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)

                        // Format and display the selected date & time
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

        // Don't allow future dates
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun clickListener() {
        binding.apply {
            backButton.setOnClickListener {
                onBackPressed()
            }

            // Select Eye Bank button click listener
            selectEyeBankBtn.setOnClickListener {
                val intent = Intent(this@HospitalRegisteration, EyeBankSearchScreen::class.java)
                eyeBankSearchLauncher.launch(intent)
            }

            // Also make the selected eye bank field clickable to search
            selectedEyeBank.setOnClickListener {
                val intent = Intent(this@HospitalRegisteration, EyeBankSearchScreen::class.java)
                eyeBankSearchLauncher.launch(intent)
            }

            submitBtn.setOnClickListener {
                validateAndSubmit()
            }
        }
    }

    private fun validateAndSubmit() {
        binding.apply {
            val donorNameVal = donorName.text.toString().trim()
            val donorAgeVal = donorAge.text.toString().trim()
            val donorGenderVal = donorGender.text.toString().trim()
            val deathDateTimeVal = deathDateTime.text.toString().trim()
            val causeOfDeathVal = causeOfDeath.text.toString().trim()
            val hospitalNameVal = hospname.text.toString().trim()
            val departmentVal = department.text.toString().trim()
            val staffNameVal = staffName.text.toString().trim()
            val staffContactVal = staffContact.text.toString().trim()
            val hospitalLocationVal = hospitalLocation.text.toString().trim()
            val isDetailsCorrect = isCorrect.isChecked

            // Validation
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

                hospitalNameVal.isEmpty() -> {
                    hospname.error = "Hospital name is required"
                    hospname.requestFocus()
                }

                departmentVal.isEmpty() -> {
                    department.error = "Department/Ward is required"
                    department.requestFocus()
                }

                staffNameVal.isEmpty() -> {
                    staffName.error = "Staff name is required"
                    staffName.requestFocus()
                }

                staffContactVal.isEmpty() -> {
                    staffContact.error = "Contact number is required"
                    staffContact.requestFocus()
                }

                staffContactVal.length < 10 -> {
                    staffContact.error = "Invalid contact number"
                    staffContact.requestFocus()
                }

                hospitalLocationVal.isEmpty() -> {
                    Snackbar.make(root, "Hospital location is required", Snackbar.LENGTH_SHORT).show()
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

                    val donorRegistration = DonorRegistration(
                        donorName = donorNameVal,
                        donorAge = donorAgeVal,
                        donorGender = donorGenderVal,
                        deathDateTime = deathDateTimeVal,
                        causeOfDeath = causeOfDeathVal,
                        hospitalName = hospitalNameVal,
                        department = departmentVal,
                        staffName = staffNameVal,
                        staffContact = staffContactVal,
                        hospitalLocation = hospitalLocationVal,
                        hospitalLatitude = hospitalLatitude,
                        hospitalLongitude = hospitalLongitude,
                        eyeBankName = selectedEyeBankName,
                        eyeBankAddress = selectedEyeBankAddress,
                        eyeBankLatitude = selectedEyeBankLatitude,
                        eyeBankLongitude = selectedEyeBankLongitude,
                        isDetailsCorrect = isDetailsCorrect
                    )

                    saveToDataBase(donorRegistration)
                }
            }
        }
    }

    private fun saveToDataBase(donorRegistration: DonorRegistration) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Save donor registration to Firestore
                val registrationId = DonorRegistrationCrud.addDonorRegistration(donorRegistration)

                // Create notification entry for eye bank
                NotificationService.createEyeBankNotification(donorRegistration, registrationId)

                // Try to find email from registered eye banks if not already found
                if (selectedEyeBankEmail.isEmpty()) {
                    val registeredBank = RegisteredEyeBankCrud.findEyeBankByName(donorRegistration.eyeBankName)
                    if (registeredBank != null) {
                        selectedEyeBankEmail = registeredBank.email
                    }
                }

                withContext(Dispatchers.Main) {
                    loadingDialog.isDismiss()
                    // Show registration saved dialog
                    showRegistrationSavedDialog(donorRegistration, registrationId)
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
                    Log.e("HospitalRegistration", "${e.message}")
                }
            }
        }
    }

    private fun showRegistrationSavedDialog(donorRegistration: DonorRegistration, registrationId: String) {
        AlertDialog.Builder(this)
            .setTitle("✅ Registration Saved!")
            .setMessage("Donor registration has been saved successfully.\n\nDo you want to notify the Eye Bank?")
            .setPositiveButton("Yes, Notify") { _, _ ->
                sendEmailToEyeBank(donorRegistration)
            }
            .setNegativeButton("No, Skip") { _, _ ->
                goToConfirmationScreen()
            }
            .setCancelable(false)
            .show()
    }

    private fun sendEmailToEyeBank(donorRegistration: DonorRegistration) {
        // Generate email content
        val subject = "🚨 URGENT: Eye Donation Available - ${donorRegistration.hospitalName}"
        
        val body = """
Dear Eye Bank Team,

This is an urgent notification regarding an eye donation opportunity.

══════════════════════════════════
         DONOR DETAILS
══════════════════════════════════
• Name: ${donorRegistration.donorName}
• Age: ${donorRegistration.donorAge} years
• Gender: ${donorRegistration.donorGender}
• Time of Death: ${donorRegistration.deathDateTime}
• Cause of Death: ${donorRegistration.causeOfDeath}

══════════════════════════════════
         HOSPITAL DETAILS
══════════════════════════════════
• Hospital: ${donorRegistration.hospitalName}
• Department/Ward: ${donorRegistration.department}
• Location: ${donorRegistration.hospitalLocation}

══════════════════════════════════
         CONTACT PERSON
══════════════════════════════════
• Name: ${donorRegistration.staffName}
• Phone: +91 ${donorRegistration.staffContact}

══════════════════════════════════
         ⏰ TIME CRITICAL
══════════════════════════════════
Eyes must be retrieved within 6 hours of death for successful transplantation.

Please respond immediately and coordinate the eye retrieval process.

Thank you for your prompt attention to this matter.

Best regards,
${donorRegistration.staffName}
${donorRegistration.hospitalName}

---
Sent via DEEP Eye Donation App
        """.trimIndent()

        // Check if we have the eye bank email
        if (selectedEyeBankEmail.isNotEmpty()) {
            // Open Gmail with pre-filled email
            openGmailWithEmail(selectedEyeBankEmail, subject, body)
        } else {
            // Prompt user to enter email
            promptForEmailAndSend(donorRegistration, subject, body)
        }
    }

    private fun openGmailWithEmail(email: String, subject: String, body: String) {
        try {
            // Try to open Gmail specifically
            val gmailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                setPackage("com.google.android.gm") // Specifically open Gmail
            }
            
            startActivity(gmailIntent)
            
            // Show confirmation after sending
            showAfterEmailDialog()
            
        } catch (e: Exception) {
            // If Gmail is not installed, use generic email intent
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

    private fun promptForEmailAndSend(donorRegistration: DonorRegistration, subject: String, body: String) {
        val input = EditText(this).apply {
            hint = "Enter Eye Bank Email"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setPadding(50, 30, 50, 30)
        }

        AlertDialog.Builder(this)
            .setTitle("Enter Eye Bank Email")
            .setMessage("Email for ${donorRegistration.eyeBankName}")
            .setView(input)
            .setPositiveButton("Send Email") { _, _ ->
                val email = input.text.toString().trim()
                if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    openGmailWithEmail(email, subject, body)
                } else {
                    Snackbar.make(binding.root, "Please enter a valid email", Snackbar.LENGTH_SHORT).show()
                    promptForEmailAndSend(donorRegistration, subject, body)
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
        startActivity(Intent(this@HospitalRegisteration, HospitalConfirmedScreen::class.java))
        finish()
    }

    private fun clearErrors() {
        binding.apply {
            donorName.error = null
            donorAge.error = null
            causeOfDeath.error = null
            hospname.error = null
            department.error = null
            staffName.error = null
            staffContact.error = null
        }
    }

    private fun setupErrorClearing() {
        binding.apply {
            // Add TextWatchers to clear errors dynamically
            donorName.addTextChangedListener { clearError(donorNameLayout) }
            donorAge.addTextChangedListener { clearError(donorAgeLayout) }
            causeOfDeath.addTextChangedListener { clearError(causeOfDeathLayout) }
            hospname.addTextChangedListener { clearError(hospnameIc) }
            department.addTextChangedListener { clearError(departmentLayout) }
            staffName.addTextChangedListener { clearError(staffNameLayout) }
            staffContact.addTextChangedListener { staffContact.error = null }
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