package com.deep.eyedonation.view.user

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.deep.eyedonation.R
import com.deep.eyedonation.databinding.ActivityPledgeBinding
import com.deep.eyedonation.handler.LoadingDialog
import com.deep.eyedonation.model.EmergencyContactDetails
import com.deep.eyedonation.model.EyeBankDetails
import com.deep.eyedonation.model.PersonalDetails
import com.deep.eyedonation.model.PledgeCrud
import com.deep.eyedonation.model.Pledges
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class PledgeScreen : AppCompatActivity() {
    private lateinit var binding: ActivityPledgeBinding
    private lateinit var loadingDialog: LoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pledge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            window.statusBarColor = resources.getColor(R.color.action_color, null)
            insets
        }

        loadingDialog = LoadingDialog(this, "Please wait...")

        clickListener()
        setupErrorClearing()

    }

    private fun clickListener() {
        binding.apply {
            backBtn.setOnClickListener {
                onBackPressed()
            }

            val bloodGroup = resources.getStringArray(R.array.bloodGroups)
            val allStates = resources.getStringArray(R.array.indian_states)

            spinnerSetup(bloodGroup, bloodGroupsTxt)
            spinnerSetup(allStates, selectState)


            dob.setOnClickListener {
                showDatePickerDialog({ selectedDate ->
                    dob.setText(selectedDate)
                }, dob)

            }

            submitBtn.setOnClickListener {
                val fullName = fullName.text.toString().trim()
                val email = email.text.toString().trim()
                val bloodType = bloodGroupsTxt.text.toString().trim()
                val dob = dob.text.toString().trim()
                val phoneNumber = phoneNumber.text.toString().trim()
                val district = district.text.toString().trim()
                val pinCode = pincode.text.toString().trim()
                val age = age.text.toString().trim()
                val address = address.text.toString().trim()

                val emergencyPersonName = emergencyName.text.toString().trim()
                val emergencyPersonNumber = contactNum.text.toString().trim()
                val emergencyPersonAddress = emergencyAddress.text.toString().trim()

                val eyeBankState = selectState.text.toString().trim()
                val eyBankDistrict = eyeBankDistrict.text.toString().trim()
                val eyeBankName = eyeBankName.text.toString().trim()
                val eyeBankContactNumber = eyeContactNumber.text.toString().trim()

                val isPledgeConfirm = pledgeConfirmation.isChecked

                // Validation
                when {

                    (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) -> {
                        binding.email.error = "Invalid Email"
                        binding.email.requestFocus()
                    }

                    pinCode.length != 6 -> {
                        pincode.error = "Invalid Pin Code"
                        pincode.requestFocus()
                    }

                    fullName.isEmpty() -> {
                        binding.fullName.error = "Full Name is required"
                        binding.fullName.requestFocus()
                    }

                    email.isEmpty() -> {
                        binding.email.error = "Email is required"
                        binding.email.requestFocus()
                    }

                    bloodType == "Select Blood Group" -> {
                        Snackbar.make(root, "Blood Group is required", Snackbar.LENGTH_SHORT).show()
                    }

                    dob.isEmpty() -> {
                        binding.dobLayout.error = "Date of Birth is required"
                        binding.dob.requestFocus()
                    }

                    phoneNumber.isEmpty() -> {
                        binding.phoneNumber.error = "Mobile number is required"
                        binding.phoneNumber.requestFocus()
                    }

                    phoneNumber.length < 10 -> {
                        binding.phoneNumber.error = "Invalid Mobile number"
                        binding.phoneNumber.requestFocus()
                    }

                    district.isEmpty() -> {
                        binding.district.error = "District is required"
                        binding.district.requestFocus()
                    }

                    pinCode.isEmpty() -> {
                        pincode.error = "Pin Code is required"
                        pincode.requestFocus()
                    }

                    age.isEmpty() -> {
                        binding.age.error = "Age is required"
                        binding.age.requestFocus()
                    }

                    address.isEmpty() -> {
                        binding.address.error = "Address is required"
                        binding.address.requestFocus()
                    }

                    emergencyPersonName.isEmpty() -> {
                        emergencyName.error = "Emergency Contact Name is required"
                        emergencyName.requestFocus()
                    }

                    emergencyPersonNumber.isEmpty() -> {
                        contactNum.error = "Emergency Contact Number is required"
                        contactNum.requestFocus()
                    }

                    emergencyPersonNumber.length < 10 -> {
                        contactNum.error = "Invalid Emergency Contact Number"
                        contactNum.requestFocus()
                    }


                    emergencyPersonAddress.isEmpty() -> {
                        emergencyAddress.error = "Emergency Address is required"
                        emergencyAddress.requestFocus()
                    }

                    eyeBankState == "Select State" -> {
                        Snackbar.make(root, "Eye Bank's State is required", Snackbar.LENGTH_SHORT)
                            .show()
                    }

                    eyBankDistrict.isEmpty() -> {
                        eyeBankDistrict.error = "Eye Bank District is required"
                        eyeBankDistrict.requestFocus()
                    }

                    eyeBankName.isEmpty() -> {
                        binding.eyeBankName.error = "Eye Bank Name is required"
                        binding.eyeBankName.requestFocus()
                    }

                    eyeBankContactNumber.isEmpty() -> {
                        eyeContactNumber.error = "Eye Bank Contact Number is required"
                        eyeContactNumber.requestFocus()
                    }

                    eyeBankContactNumber.length < 10 -> {
                        eyeContactNumber.error = "Invalid Eye Bank Contact Number"
                        eyeContactNumber.requestFocus()
                    }


                    else -> {
                        loadingDialog.startLoading()
                        clearErrors()

                        val personalDetails = PersonalDetails(
                            fullName,
                            email,
                            bloodType,
                            dob,
                            phoneNumber,
                            district,
                            pinCode,
                            age,
                            address
                        )

                        val emergencyPerson = EmergencyContactDetails(
                            emergencyPersonName,
                            emergencyPersonNumber,
                            emergencyPersonAddress
                        )

                        val eyeBankDetails =
                            EyeBankDetails(
                                eyeBankState,
                                eyBankDistrict,
                                eyeBankName,
                                eyeBankContactNumber
                            )

                        val pledge = Pledges(
                            personalDetails,
                            emergencyPerson,
                            eyeBankDetails,
                            isPledgeConfirm,
                            getFormattedDateTime(),
                            "Pending"
                        )

                        saveToDataBase(pledge)
                    }
                }
            }
        }
    }

    private fun getFormattedDateTime(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("d-M-yyyy")
        return currentDateTime.format(formatter)
    }

    private fun saveToDataBase(pledge: Pledges) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                PledgeCrud.addPledge(pledge)

//                // Send Confirmation Email
//                val subject = "Thank You for Your Eye Donation Pledge!"
//                val body = """
//                Dear ${pledge.personalDetails.fullName},
//
//                Thank you for pledging your eyes for donation. Your generous act will make a significant difference in someone's life.
//
//                Regards,
//                Eye Donation Team
//            """.trimIndent()
//
//                EmailSender.sendEmail(pledge.personalDetails.email, subject, body)

                withContext(Dispatchers.Main) {
                    loadingDialog.isDismiss()
                    startActivity(Intent(this@PledgeScreen, PledgeConfirmedScreen::class.java))
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun clearErrors() {
        binding.apply {
            fullNameIc.error = null
            email.error = null
            bloodGroupsTxt.error = null
            dobLayout.error = null
            phoneNumber.error = null
            district.error = null
            pincode.error = null
            age.error = null
            address.error = null
            emergencyName.error = null
            contactNum.error = null
            emergencyAddress.error = null
            selectStateLayout.error = null
            eyeBankDistrict.error = null
            eyeBankName.error = null
            eyeContactNumber.error = null
        }
    }


    private fun spinnerSetup(list: Array<String>, textview: AutoCompleteTextView) {
        val arrayList = ArrayAdapter(
            this@PledgeScreen, android.R.layout.simple_spinner_dropdown_item, list
        )
        arrayList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        textview.setAdapter(arrayList)
        textview.setText(list[0], false)

        // Make the AutoCompleteTextView searchable
        textview.threshold = 1
    }

    private fun showDatePickerDialog(onDateSet: (String) -> Unit, edtText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = "$selectedDay-${selectedMonth + 1}-$selectedYear"
                edtText.setText(date)
                onDateSet(date)

                binding.dobLayout.error = null
            },
            year,
            month,
            day
        ).show()

    }

    private fun setupErrorClearing() {
        binding.apply {
            // Add TextWatchers to clear errors dynamically
            fullName.addTextChangedListener { clearError(fullNameIc) }
            email.addTextChangedListener { clearError(emailIcon) }
            phoneNumber.addTextChangedListener { clearError(phoneNumber) }
            pincode.addTextChangedListener { clearError(pincodeLayout) }
            dob.addTextChangedListener { clearError(dobLayout) }
            age.addTextChangedListener { clearError(ageLayout) }
            address.addTextChangedListener { clearError(addressLayout) }
            emergencyName.addTextChangedListener { clearError(emergencyNameLayout) }
            contactNum.addTextChangedListener { clearError(contactNum) }
            selectState.addTextChangedListener { clearError(selectStateLayout) }
            emergencyAddress.addTextChangedListener { clearError(emergencyAddressLayout) }
            eyeBankDistrict.addTextChangedListener { clearError(eyeBankDistrictLayout) }
            eyeBankName.addTextChangedListener { clearError(eyeBankNameLayout) }
            eyeContactNumber.addTextChangedListener { clearError(eyeContactNumber) }
            bloodGroupsTxt.setOnItemClickListener { _, _, _, _ ->
                clearError(
                    bloodGroupLayout
                )
            }
            selectState.setOnItemClickListener { _, _, _, _ -> clearError(selectStateLayout) }
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