package com.jskaleel.fte.model

import android.view.MenuItem

data class SelectedMenu(var menuItem: Int)
data class SelectedMenuItem(var menuItem: MenuItem)
data class ScrollList(var menuItem: Boolean)
data class NetWorkMessage(var message: String)