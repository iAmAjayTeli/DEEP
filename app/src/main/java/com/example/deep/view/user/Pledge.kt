package com.example.deep.view.user

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.deep.R
import com.example.deep.databinding.ActivityPledgeBinding
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class Pledge : AppCompatActivity() {
    private lateinit var binding: ActivityPledgeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pledge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        clickListener()

    }

    private fun clickListener() {
        binding.apply {
            backButton.setOnClickListener {
                onBackPressed()
            }

            val bloodGroup = resources.getStringArray(R.array.bloodGroups)
            val allStates = resources.getStringArray(R.array.indian_states)

            spinnerSetup(bloodGroup, bloodGroupsTxt)
            spinnerSetup(allStates, selectState)

            dob.setOnClickListener {

            }


        }
    }

    private fun spinnerSetup(list: Array<String>, textview: AutoCompleteTextView) {
        val arrayList = ArrayAdapter(
            this@Pledge, android.R.layout.simple_spinner_dropdown_item, list
        )
        arrayList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        textview.setAdapter(arrayList)
        textview.setText(list[0], false)

        // Make the AutoCompleteTextView searchable
        textview.threshold = 1
    }

    fun showDatePickerDialog(onDateSet: (String) -> Unit, edtText: TextInputEditText) {
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
            },
            year,
            month,
            day
        ).show()

    }

}