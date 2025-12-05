package com.deep.eyedonation.view.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.deep.eyedonation.R
import com.deep.eyedonation.databinding.ActivityHospitalRegisterationBinding
import com.deep.eyedonation.handler.LoadingDialog
import com.deep.eyedonation.model.Hospital
import com.deep.eyedonation.model.HospitalCrud
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HospitalRegisteration : AppCompatActivity() {
    private lateinit var binding: ActivityHospitalRegisterationBinding
    private lateinit var loadingDialog: LoadingDialog

    // Store selected eye bank details
    private var selectedEyeBankName: String = ""
    private var selectedEyeBankAddress: String = ""

    // Activity result launcher for Eye Bank Search
    private val eyeBankSearchLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                selectedEyeBankName = data.getStringExtra(EyeBankSearchScreen.EXTRA_EYE_BANK_NAME) ?: ""
                selectedEyeBankAddress = data.getStringExtra(EyeBankSearchScreen.EXTRA_EYE_BANK_ADDRESS) ?: ""
                val distance = data.getStringExtra(EyeBankSearchScreen.EXTRA_EYE_BANK_DISTANCE) ?: ""

                // Auto-fill the selected eye bank field with name
                binding.selectedEyeBank.setText(selectedEyeBankName)

                // Extract and set district from the eye bank address
                val extractedDistrict = extractDistrictFromAddress(selectedEyeBankAddress)
                binding.eyeBankDistrict.setText(extractedDistrict)

                // Extract and set state from the eye bank address
                val extractedState = extractStateFromAddress(selectedEyeBankAddress)
                if (extractedState.isNotEmpty()) {
                    binding.selectState.setText(extractedState, false)
                }
            }
        }
    }

    private fun extractDistrictFromAddress(address: String): String {
        val addressLower = address.lowercase()
        
        // Map of cities/localities to their actual districts
        val cityToDistrictMap = mapOf(
            // Karnataka
            "bangalore" to "Bengaluru Urban",
            "bengaluru" to "Bengaluru Urban",
            "frazer town" to "Bengaluru Urban",
            "koramangala" to "Bengaluru Urban",
            "whitefield" to "Bengaluru Urban",
            "electronic city" to "Bengaluru Urban",
            "jayanagar" to "Bengaluru Urban",
            "indiranagar" to "Bengaluru Urban",
            "hsr layout" to "Bengaluru Urban",
            "btm layout" to "Bengaluru Urban",
            "malleshwaram" to "Bengaluru Urban",
            "rajajinagar" to "Bengaluru Urban",
            "mysore" to "Mysuru",
            "mysuru" to "Mysuru",
            "mangalore" to "Dakshina Kannada",
            "mangaluru" to "Dakshina Kannada",
            "hubli" to "Dharwad",
            "dharwad" to "Dharwad",
            "belgaum" to "Belgavi",
            "belagavi" to "Belgavi",
            
            // Maharashtra
            "mumbai" to "Mumbai",
            "pune" to "Pune",
            "nagpur" to "Nagpur",
            "thane" to "Thane",
            "nashik" to "Nashik",
            "aurangabad" to "Aurangabad",
            "navi mumbai" to "Thane",
            
            // Tamil Nadu
            "chennai" to "Chennai",
            "coimbatore" to "Coimbatore",
            "madurai" to "Madurai",
            "salem" to "Salem",
            "trichy" to "Tiruchirappalli",
            "tiruchirappalli" to "Tiruchirappalli",
            
            // Delhi
            "delhi" to "Delhi",
            "new delhi" to "New Delhi",
            
            // Telangana
            "hyderabad" to "Hyderabad",
            "secunderabad" to "Hyderabad",
            "rangareddy" to "Rangareddy",
            
            // Kerala
            "kochi" to "Ernakulam",
            "ernakulam" to "Ernakulam",
            "thiruvananthapuram" to "Thiruvananthapuram",
            "trivandrum" to "Thiruvananthapuram",
            "kozhikode" to "Kozhikode",
            "calicut" to "Kozhikode",
            
            // West Bengal
            "kolkata" to "Kolkata",
            "calcutta" to "Kolkata",
            "howrah" to "Howrah",
            
            // Gujarat
            "ahmedabad" to "Ahmedabad",
            "surat" to "Surat",
            "vadodara" to "Vadodara",
            "rajkot" to "Rajkot",
            
            // Rajasthan
            "jaipur" to "Jaipur",
            "jodhpur" to "Jodhpur",
            "udaipur" to "Udaipur",
            
            // Uttar Pradesh
            "lucknow" to "Lucknow",
            "noida" to "Gautam Buddha Nagar",
            "greater noida" to "Gautam Buddha Nagar",
            "ghaziabad" to "Ghaziabad",
            "kanpur" to "Kanpur Nagar",
            "varanasi" to "Varanasi",
            "agra" to "Agra",
            "prayagraj" to "Prayagraj",
            "allahabad" to "Prayagraj",
            
            // Haryana
            "gurgaon" to "Gurugram",
            "gurugram" to "Gurugram",
            "faridabad" to "Faridabad",
            
            // Punjab
            "chandigarh" to "Chandigarh",
            "ludhiana" to "Ludhiana",
            "amritsar" to "Amritsar",
            
            // Madhya Pradesh
            "bhopal" to "Bhopal",
            "indore" to "Indore",
            "gwalior" to "Gwalior",
            
            // Andhra Pradesh
            "visakhapatnam" to "Visakhapatnam",
            "vizag" to "Visakhapatnam",
            "vijayawada" to "Krishna",
            "tirupati" to "Tirupati",
            
            // Odisha
            "bhubaneswar" to "Khordha",
            "cuttack" to "Cuttack",
            
            // Bihar
            "patna" to "Patna",
            
            // Jharkhand
            "ranchi" to "Ranchi",
            "jamshedpur" to "East Singhbhum",
            
            // Assam
            "guwahati" to "Kamrup Metropolitan",
            
            // Goa
            "goa" to "North Goa",
            "panaji" to "North Goa",
            "margao" to "South Goa"
        )
        
        // Check if any known city/locality is in the address
        for ((city, district) in cityToDistrictMap) {
            if (addressLower.contains(city)) {
                return district
            }
        }
        
        // Fallback: Try to extract from address parts
        // Google Places usually formats as: "Area, City, State" or "Area, City"
        val parts = address.split(",").map { it.trim() }
        
        // If we have multiple parts, check the last few parts for known cities
        if (parts.size >= 2) {
            for (i in parts.indices.reversed()) {
                val partLower = parts[i].lowercase()
                for ((city, district) in cityToDistrictMap) {
                    if (partLower.contains(city)) {
                        return district
                    }
                }
            }
            // If no known city found, return the second to last part
            return parts[parts.size - 1]
        }
        
        return if (parts.isNotEmpty()) parts[0] else ""
    }

    private fun extractStateFromAddress(address: String): String {
        // Get all Indian states from resources
        val allStates = resources.getStringArray(R.array.indian_states)
        val addressLower = address.lowercase()

        // Check if any state name is present in the address
        for (state in allStates) {
            if (state != "Select State" && addressLower.contains(state.lowercase())) {
                return state
            }
        }

        // Common state abbreviations/variations mapping
        val stateAbbreviations = mapOf(
            "ka" to "Karnataka",
            "karnataka" to "Karnataka",
            "bangalore" to "Karnataka",
            "bengaluru" to "Karnataka",
            "mh" to "Maharashtra",
            "maharashtra" to "Maharashtra",
            "mumbai" to "Maharashtra",
            "pune" to "Maharashtra",
            "tn" to "Tamil Nadu",
            "tamil nadu" to "Tamil Nadu",
            "chennai" to "Tamil Nadu",
            "dl" to "Delhi",
            "delhi" to "Delhi",
            "new delhi" to "Delhi",
            "up" to "Uttar Pradesh",
            "uttar pradesh" to "Uttar Pradesh",
            "lucknow" to "Uttar Pradesh",
            "noida" to "Uttar Pradesh",
            "gurgaon" to "Haryana",
            "gurugram" to "Haryana",
            "haryana" to "Haryana",
            "hyderabad" to "Telangana",
            "telangana" to "Telangana",
            "kerala" to "Kerala",
            "kochi" to "Kerala",
            "thiruvananthapuram" to "Kerala",
            "kolkata" to "West Bengal",
            "west bengal" to "West Bengal",
            "gujarat" to "Gujarat",
            "ahmedabad" to "Gujarat",
            "rajasthan" to "Rajasthan",
            "jaipur" to "Rajasthan",
            "punjab" to "Punjab",
            "chandigarh" to "Chandigarh",
            "goa" to "Goa",
            "mp" to "Madhya Pradesh",
            "madhya pradesh" to "Madhya Pradesh",
            "bhopal" to "Madhya Pradesh",
            "indore" to "Madhya Pradesh",
            "andhra pradesh" to "Andhra Pradesh",
            "ap" to "Andhra Pradesh",
            "vizag" to "Andhra Pradesh",
            "visakhapatnam" to "Andhra Pradesh"
        )

        // Check for city/state abbreviations
        for ((key, value) in stateAbbreviations) {
            if (addressLower.contains(key)) {
                return value
            }
        }

        return ""
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

            // Search Eye Bank click listener
            searchEyeBankTxt.setOnClickListener {
                val intent = Intent(this@HospitalRegisteration, EyeBankSearchScreen::class.java)
                eyeBankSearchLauncher.launch(intent)
            }

            // Also make the selected eye bank field clickable to search
            selectedEyeBank.setOnClickListener {
                val intent = Intent(this@HospitalRegisteration, EyeBankSearchScreen::class.java)
                eyeBankSearchLauncher.launch(intent)
            }




            submitBtn.setOnClickListener {
                val fullName = hospname.text.toString().trim()
                val email = email.text.toString().trim()
                val address = address.text.toString().trim()
                val District = eyeBankDistrict.text.toString().trim()
                val State = selectState.text.toString().trim()
                val PersonName = emergencyName.text.toString().trim()
                val PersonNumber = contactNum.text.toString().trim()
                val isDetailsCorrect = isCorrect.isChecked


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
                            PersonNumber,
                            selectedEyeBankName,
                            isDetailsCorrect
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
                    startActivity(
                        Intent(
                            this@HospitalRegisteration,
                            HospitalConfirmedScreen::class.java
                        )
                    )
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Snackbar.make(
                        binding.root,
                        "Failed to register hospital: ${e.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                    Log.e("HospitalRegistration", "${e.message}")
                }
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