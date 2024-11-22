package com.example.deep.handler

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deep.R
import com.example.deep.adapter.DrawerAdapter
import com.example.deep.databinding.NavHeaderBinding
import com.example.deep.model.DrawerItem
import com.example.deep.view.user.AboutUs
import com.example.deep.view.user.EyeBank
import com.example.deep.view.user.FAQ
import com.example.deep.view.user.MainActivity
import com.example.deep.view.user.Pledge
import com.example.deep.view.user.SuccessStories

class
NavigationDrawerSetup(
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
        return listOf(
            DrawerItem(R.drawable.eye_bank, "Eye Bank"),
            DrawerItem(R.drawable.eye_pledge, "Pledge"),
            DrawerItem(R.drawable.terms_condition, "FAQ"),
            DrawerItem(R.drawable.about_us, "About Us"),
            DrawerItem(R.drawable.success, "Success Stories"),
            DrawerItem(R.drawable.login, "Admin Login")


        )
    }

    private fun handleDrawerItemClick(title: String) {
        when (title) {

            "Eye Bank" -> {
                navigateTo(EyeBank())
            }

            "Pledge" -> {
                navigateTo(Pledge())
            }

            "FAQ"->{
               navigateTo(FAQ())
            }

            "About Us" -> {
               navigateTo(AboutUs())
            }

            "Success Stories"->{
                navigateTo(SuccessStories())
            }

            "Admin Login"->{
                navigateTo(FAQ())
            }
        }
    }

    private fun navigateTo(activity: Activity) {
        context.startActivity(Intent(context, activity::class.java))
    }
}