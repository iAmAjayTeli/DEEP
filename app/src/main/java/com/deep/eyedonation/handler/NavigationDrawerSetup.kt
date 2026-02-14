package com.deep.eyedonation.handler

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deep.eyedonation.R
import com.deep.eyedonation.adapter.DrawerAdapter
import com.deep.eyedonation.databinding.NavHeaderBinding
import com.deep.eyedonation.model.DrawerItem
import com.deep.eyedonation.util.SharedPreference
import com.deep.eyedonation.view.admin.AddSuccessStories
import com.deep.eyedonation.view.admin.AdminLogin
import com.deep.eyedonation.view.admin.AdminSetting
import com.deep.eyedonation.view.admin.ManageHospital
import com.deep.eyedonation.view.admin.ManagePledge
import com.deep.eyedonation.view.user.AboutUs
import com.deep.eyedonation.view.user.ContactUsScreen
import com.deep.eyedonation.view.user.EyeBankScreen
import com.deep.eyedonation.view.user.FamilyDonorRegistration
import com.deep.eyedonation.view.user.FaqScreen
import com.deep.eyedonation.view.user.HospitalRegisteration
import com.deep.eyedonation.view.user.MainActivity
import com.deep.eyedonation.view.user.SuccessStoriesScreen

class
NavigationDrawerSetup(
    private val isAdmin: Boolean,
    private val context: Context,
    private val drawerLayout: DrawerLayout,
    private val recyclerView: RecyclerView,
) {
    fun setupDrawer() {
        val headerView = drawerLayout.findViewById<View>(R.id.navHeader)
        val navBinding: NavHeaderBinding = DataBindingUtil.bind(headerView)!!

        val drawerItems = getDrawerItems()
        val drawerAdapter = DrawerAdapter(drawerItems) { item ->
            handleDrawerItemClick(item.title)
            drawerLayout.closeDrawers()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = drawerAdapter
    }

    private fun getDrawerItems(): List<DrawerItem> {
        return commonDrawerItem()
    }

    private fun commonDrawerItem(): List<DrawerItem> {

        val list: List<DrawerItem>

        if (!isAdmin) {
            list = listOf(
                DrawerItem(R.drawable.eye_bank, "Eye Bank"),
                DrawerItem(R.drawable.eye_pledge, "Family Donor Registration"),
                DrawerItem(R.drawable.terms_condition, "FAQ"),
                DrawerItem(R.drawable.customer_care, "Contact Us"),
                DrawerItem(R.drawable.about_us, "About Us"),
                DrawerItem(R.drawable.success, "Success Stories"),
                DrawerItem(R.drawable.hospital, "Hospital Registration"),
                DrawerItem(R.drawable.login, "Admin Login")

            )
        } else {
            list = listOf(
                DrawerItem(R.drawable.eye_pledge, "Manage Pledge"),
                DrawerItem(R.drawable.success, "Add Success Stories"),
                DrawerItem(R.drawable.hospital, "Manage Hospital"),
                DrawerItem(R.drawable.setting, "Setting"),
                DrawerItem(R.drawable.login, "Logout")
            )
        }
        return list
    }

    private fun handleDrawerItemClick(title: String) {
        when (title) {

            "Eye Bank" -> {
                navigateTo(EyeBankScreen())
            }

            "Family Donor Registration" -> {
                navigateTo(FamilyDonorRegistration())
            }

            "FAQ" -> {
                navigateTo(FaqScreen())
            }

            "Contact Us" -> {
                navigateTo(ContactUsScreen())
            }

            "About Us" -> {
                navigateTo(AboutUs())
            }

            "Success Stories" -> {
                navigateTo(SuccessStoriesScreen())
            }


            "Hospital Registration" -> {
                navigateTo(HospitalRegisteration())
            }

            "Admin Login" -> {
                navigateTo(AdminLogin())
            }

            "Manage Pledge" -> {
                navigateTo(ManagePledge())
            }

            "Add Success Stories" -> {
                navigateTo(AddSuccessStories())
            }

            "Manage Hospital" -> {
                navigateTo(ManageHospital())
            }

            "Setting" -> {
                navigateTo(AdminSetting())
            }

            "Logout" -> {
                SharedPreference(context).logout(SharedPreference(context), MainActivity())
            }


        }
    }

    private fun navigateTo(activity: Activity) {
        context.startActivity(Intent(context, activity::class.java))
    }
}