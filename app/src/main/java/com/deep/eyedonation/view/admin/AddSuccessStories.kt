package com.deep.eyedonation.view.admin

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.deep.eyedonation.R
import com.deep.eyedonation.databinding.ActivityAddSuccessStoriesBinding
import com.deep.eyedonation.handler.LoadingDialog
import com.deep.eyedonation.model.Stories
import com.deep.eyedonation.model.SuccessStoriesCrud
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AddSuccessStories : AppCompatActivity() {
    private lateinit var binding: ActivityAddSuccessStoriesBinding
    private lateinit var loading: LoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_success_stories)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            window.statusBarColor = getColor(R.color.action_color)
            insets
        }

        loading = LoadingDialog(this, "Adding....")

        setUpClickListener()

    }

    private fun setUpClickListener() {
        binding.apply {
            backBtn.setOnClickListener {
                onBackPressed()
            }


            date.setOnClickListener {
                showDatePickerDialog({ selectedDate ->
                    date.setText(selectedDate)
                }, date)
            }

            viewAllBtn.setOnClickListener {
                startActivity(Intent(this@AddSuccessStories, ManageAddedStory::class.java))
                finish()
            }


            addStoriesBtn.setOnClickListener {
                val name = binding.nameInput.text.toString().trim()
                val rating = binding.rating.rating
                val date = binding.date.text.toString().trim()
                val message = binding.messageInput.text.toString().trim()

                if (name.isNotEmpty() && date.isNotEmpty() && message.isNotEmpty()) {
                    loading.startLoading()
                    val successStories = Stories(id = null, message, date, rating, name)
                    saveToDataBase(successStories)
                } else {
                    Snackbar.make(it, "Please fill all the fields", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveToDataBase(stories: Stories) {
        lifecycleScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) { SuccessStoriesCrud.addStory(stories) }
            loading.isDismiss()
            Snackbar.make(binding.root, "Story Added Successfully", Snackbar.LENGTH_SHORT).show()
            delay(1000)
            startActivity(Intent(this@AddSuccessStories, ManageAddedStory::class.java))
            finish()
        }
    }


    private fun showDatePickerDialog(onDateSet: (String) -> Unit, edtText: EditText) {
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