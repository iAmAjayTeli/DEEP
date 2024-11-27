package com.example.deep.view.user

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
import com.example.deep.R
import com.example.deep.databinding.ActivityHospitalRegisterationBinding
import com.example.deep.databinding.ActivityPledgeBinding
import com.example.deep.handler.LoadingDialog
import com.example.deep.model.Hospital
import com.example.deep.model.HospitalCrud
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HospitalRegisteration : AppCompatActivity() {
    private lateinit var binding: ActivityHospitalRegisterationBinding
    private lateinit var loadingDialog: LoadingDialog
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

        clickListener()
        setupErrorClearing()
    }


    private fun clickListener() {
        binding.apply {
            backButton.setOnClickListener {
                onBackPressed()
            }

            val allStates = resources.getStringArray(R.array.indian_states)

            spinnerSetup(allStates, selectState)




            submitBtn.setOnClickListener {
                val fullName = hospname.text.toString().trim()
                val email = email.text.toString().trim()
                val address = address.text.toString().trim()
                val District = eyeBankDistrict.text.toString().trim()
                val State = selectState.text.toString().trim()
                val PersonName = emergencyName.text.toString().trim()
                val PersonNumber = contactNum.text.toString().trim()



                val isPledgeConfirm = pledgeConfirmation.isChecked

                // Validation
                when {

                    fullName.isEmpty() -> {
                        binding.hospname.error = "Full Name is required"
                        binding.hospname.requestFocus()
                    }

                    email.isEmpty() -> {
                        binding.email.error = "Email is required"
                        binding.email.requestFocus()
                    }

                    (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) -> {
                        binding.email.error = "Invalid Email"
                        binding.email.requestFocus()
                    }



                    address.isEmpty() -> {
                        binding.address.error = "Address is required"
                        binding.address.requestFocus()
                    }

                    PersonName.isEmpty() -> {
                        emergencyName.error = " Contact Name is required"
                        emergencyName.requestFocus()
                    }

                    PersonNumber.isEmpty() -> {
                        contactNum.error = "Contact Number is required"
                        contactNum.requestFocus()
                    }

                    PersonNumber.length < 10 -> {
                        contactNum.error = "Invalid Emergency Contact Number"
                        contactNum.requestFocus()
                    }




                    State == "Select State" -> {
                        Snackbar.make(root, "Hospital's State is required", Snackbar.LENGTH_SHORT)
                            .show()
                    }

                    District.isEmpty() -> {
                        eyeBankDistrict.error = "Hospital's District is required"
                        eyeBankDistrict.requestFocus()
                    }


                    else -> {
                        loadingDialog.startLoading()
                        clearErrors()

                        val hospitalDetails = Hospital(
                            fullName,
                            email,
                            address,
                            District,
                            State,
                            PersonName,
                            PersonNumber

                        )



                        saveToDataBase(hospitalDetails)
                    }
                }
            }
        }
    }

    private fun saveToDataBase(hospital: Hospital) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                HospitalCrud.addHospital(hospital)

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
                    startActivity(Intent(this@HospitalRegisteration, HospitalConfirmedScreen::class.java))
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun clearErrors() {
        binding.apply {
            hospname.error = null
            email.error = null
            address.error = null
            emergencyName.error = null
            contactNum.error = null
            selectStateLayout.error = null
            eyeBankDistrict.error = null

        }
    }


    private fun spinnerSetup(list: Array<String>, textview: AutoCompleteTextView) {
        val arrayList = ArrayAdapter(
            this@HospitalRegisteration, android.R.layout.simple_spinner_dropdown_item, list
        )
        arrayList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        textview.setAdapter(arrayList)
        textview.setText(list[0], false)

        // Make the AutoCompleteTextView searchable
        textview.threshold = 1
    }


    private fun setupErrorClearing() {
        binding.apply {
            // Add TextWatchers to clear errors dynamically
            hospname.addTextChangedListener { clearError(hospnameIc) }
            email.addTextChangedListener { clearError(emailIcon) }

            address.addTextChangedListener { clearError(addressic) }
            emergencyName.addTextChangedListener { clearError(emergencyNameLayout) }
            contactNum.addTextChangedListener { clearError(contactNum) }
            selectState.addTextChangedListener { clearError(selectStateLayout) }
            eyeBankDistrict.addTextChangedListener { clearError(eyeBankDistrictLayout) }
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