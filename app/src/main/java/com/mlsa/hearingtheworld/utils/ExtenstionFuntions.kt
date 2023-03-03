package com.mlsa.hearingtheworld.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import androidx.room.TypeConverter
import com.bumptech.glide.Glide
import com.mlsa.hearingtheworld.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


fun TextInputEditText.isNotNullOrEmpty(errorString: String): Boolean {
    val textInputLayout = this.parent.parent as TextInputLayout
    textInputLayout.errorIconDrawable = null
    this.onChange { textInputLayout.error = null }

    return if (this.text.toString().trim().isEmpty()) {
        textInputLayout.error = errorString
        false
    } else {
        true
    }
}

fun EditText.isNotNullOrEmpty(errorString: String): Boolean {
    this.onChange { this.error = null }

    return if (this.text.toString().trim().isBlank()) {
        this.error = errorString
        false
    } else {
        true
    }
}

fun EditText.onChange(cb: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            cb(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

fun hasLocationPermission(context: Context)=
    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }else{
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
    if (url!=null) {
        Glide.with(view.context).load(url).fitCenter()
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(view)
    }else{
        //view.visibility= View.GONE
    }
}
@BindingAdapter("imageUrlZm")
fun loadImageZm(view: ImageView, url: String?) {
    if (url!=null) {
        Glide.with(view.context).load(url)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(view)
    }else{
        //view.visibility= View.GONE
    }
}

fun ContentResolver.getFileName(fileUri: Uri): String {
    var name = ""
    val returnCursor = this.query(fileUri, null, null, null, null)
    if (returnCursor != null) {
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        name = returnCursor.getString(nameIndex)
        returnCursor.close()
    }
    return name
}

@SuppressLint("SimpleDateFormat")
fun getCurrentTimeOfDevice(): String {
    val getCurrentTimeOfDevice = Calendar.getInstance()
    val now  = getCurrentTimeOfDevice.timeInMillis
    val formatCurrentTimeOfDevice = SimpleDateFormat("yyyy-MM-dd")
    formatCurrentTimeOfDevice.timeZone = TimeZone.getTimeZone("UTC") //convert to UTC because the server is going to convert the time to local when it saves the
    //the record there
    return formatCurrentTimeOfDevice.format(now)
}

class AppTypeConverters {
    @TypeConverter
    fun fromJsonElement(jsonElement: JsonElement): String = jsonElement.toString()

    @TypeConverter
    fun stringToJsonElement(string: String): JsonElement = string.toJsonElement()
}

fun String.toJsonElement(): JsonElement = gson.fromJson(this@toJsonElement, JsonElement::class.java)

val gson: Gson by lazy {
    GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
}

fun convertDateTimeToAgoDate(dateString: String): String {
    val sb = StringBuilder()
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    val date = format.parse(dateString)
    val now = Date()
    val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(now.time - date.time)
    val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(now.time - date.time)
    val hours: Long = TimeUnit.MILLISECONDS.toHours(now.time - date.time)
    val days: Long = TimeUnit.MILLISECONDS.toDays(now.time - date.time)

    when {
        seconds<60 -> {
            sb.append(seconds, " seconds ago")
        }
        minutes < 60 -> {
            sb.append(minutes, " minutes ago")
        }
        hours < 24 -> {
            sb.append(hours, " hours ago")
        }
        else -> {
            sb.append(days, " days ago")
        }
    }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        HtmlCompat.fromHtml(sb.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    }
}

