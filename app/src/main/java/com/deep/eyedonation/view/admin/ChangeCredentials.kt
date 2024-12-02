package com.deep.eyedonation.view.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.deep.eyedonation.R
import com.deep.eyedonation.databinding.ActivityFogotPasswordBinding
import com.deep.eyedonation.handler.LoadingDialog
import com.deep.eyedonation.model.Admin
import com.deep.eyedonation.model.AdminCredentials
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChangeCredentials : AppCompatActivity() {
    private lateinit var binding: ActivityFogotPasswordBinding
    private var isChangePassword: Boolean = true
    private var isChangeUsername: Boolean = false
    private var isAdminSetting: Boolean = false

    private lateinit var usernameText: String
    private lateinit var passwordTxt: String
    private lateinit var adminCredentials: AdminCredentials
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fogot_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            window.statusBarColor = getColor(R.color.action_color)
            insets
        }

        isChangePassword = intent.getBooleanExtra("isChangePassword", true)
        isChangeUsername = intent.getBooleanExtra("isChangeUsername", false)
        isAdminSetting = intent.getBooleanExtra("isAdminSetting", false)


        lifecycleScope.launch(Dispatchers.Main) {
            try {
                adminCredentials = withContext(Dispatchers.IO) { Admin.getAdminCredentials()!! }
                usernameText = adminCredentials.username
                passwordTxt = adminCredentials.password

                setupClickListener()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ChangeCredentials", "${e.message}")
            }

        }


    }

    private fun setupClickListener() {
        binding.apply {
            backBtn.setOnClickListener {
                onBackPressed()
            }

            if (isChangePassword) {
                textView6.text = "Forgot admin password"
                username.visibility = View.VISIBLE
                checkUsernameBtn.visibility = View.VISIBLE
                updatePassword.text = "Update Password"
            } else {
                textView6.text = "Forgot admin username"
                username.visibility = View.GONE
                checkUsernameBtn.visibility = View.GONE
                password.visibility = View.VISIBLE
                password.hint = "Enter Password to validate"
                updatePassword.text = "Validate Password"
                updatePassword.visibility = View.VISIBLE
                confirmPassword.visibility = View.GONE
            }



            updatePassword.setOnClickListener {
                if (updatePassword.text == "Update Password") {
                    val loadingDialog = LoadingDialog(this@ChangeCredentials, "Updating...")
                    passwordTxt = password.text.toString().trim()
                    val confirmedPassword = confirmPassword.text.toString().trim()
                    if (passwordTxt.isNotEmpty() && confirmedPassword.isNotEmpty()) {
                        if (passwordTxt == confirmedPassword) {
                            loadingDialog.startLoading()
                            lifecycleScope.launch(Dispatchers.Main) {
                                withContext(Dispatchers.IO) {
                                    Admin.updateAdminCrediantials(
                                        usernameText,
                                        passwordTxt
                                    )
                                }
                                loadingDialog.isDismiss()
                                Snackbar.make(
                                    it,
                                    "Password Updated Successfully",
                                    Snackbar.LENGTH_SHORT
                                )
                                    .show()
                                delay(1000)

                                if (isAdminSetting) {
                                    startActivity(
                                        Intent(
                                            this@ChangeCredentials,
                                            AdminSetting::class.java
                                        )
                                    )
                                    finish()
                                } else {
                                    startActivity(
                                        Intent(
                                            this@ChangeCredentials,
                                            AdminLogin::class.java
                                        )
                                    )
                                    finish()
                                }
                            }
                        } else {
                            confirmPassword.error = "Password doesn't match"
                        }
                    } else {
                        password.error = "Please enter password"
                    }
                } else if (updatePassword.text == "Validate Password") {
                    val loadingDialog = LoadingDialog(this@ChangeCredentials, "Validating...")
                    val passwordTxt = password.text.toString().trim()
                    if (passwordTxt.isNotEmpty()) {
                        loadingDialog.startLoading()
                        lifecycleScope.launch(Dispatchers.Main) {
                            val adminCredentials =
                                withContext(Dispatchers.IO) { Admin.getAdminCredentials() }
                            val correctPassword = adminCredentials?.password

                            if (passwordTxt == correctPassword) {
                                loadingDialog.isDismiss()
                                checkUsernameBtn.visibility = View.VISIBLE
                                checkUsernameBtn.text = "Update Username"
                                updatePassword.visibility = View.GONE
                                password.visibility = View.GONE
                                username.visibility = View.VISIBLE
                                Snackbar.make(
                                    it,
                                    "Password is validated, Enter new username",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            } else {
                                loadingDialog.isDismiss()
                                Snackbar.make(
                                    it,
                                    "Password Validation failed, enter correctly",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }

                    } else {
                        updatePassword.error = "Enter password to validate"
                    }
                }
            }

            checkUsernameBtn.setOnClickListener {
                if (checkUsernameBtn.text == "Update Username") {
                    val loadingDialog = LoadingDialog(this@ChangeCredentials, "Updating...")
                    val usernameTxt = username.text.toString().trim()

                    if (usernameTxt.isNotEmpty()) {
                        loadingDialog.startLoading()
                        lifecycleScope.launch(Dispatchers.Main) {
                            withContext(Dispatchers.IO) {
                                Admin.updateAdminCrediantials(usernameTxt, passwordTxt)
                            }
                            loadingDialog.isDismiss()
                            Snackbar.make(
                                it,
                                "Username Updated Successfully",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            delay(1000)
                            startActivity(Intent(this@ChangeCredentials, AdminSetting::class.java))
                            finish()
                        }

                    } else {
                        username.error = "Please enter username"
                    }

                } else {
                    val loadingDialog = LoadingDialog(this@ChangeCredentials, "Checking...")
                    usernameText = username.text.toString().trim()
                    if (usernameText.isNotEmpty()) {
                        loadingDialog.startLoading()
                        lifecycleScope.launch(Dispatchers.Main) {
                            val credentials =
                                withContext(Dispatchers.Main) { Admin.getAdminCredentials() }
                            val correctUsername = credentials?.username
                            if (usernameText == correctUsername) {
                                loadingDialog.isDismiss()
                                Snackbar.make(
                                    it,
                                    "Validated, Enter new Password",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                password.visibility = View.VISIBLE
                                username.visibility = View.GONE
                                checkUsernameBtn.visibility = View.GONE
                                updatePassword.visibility = View.VISIBLE
                                confirmPassword.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        username.error = "Please enter username"
                    }
                }

            }
        }
    }
}