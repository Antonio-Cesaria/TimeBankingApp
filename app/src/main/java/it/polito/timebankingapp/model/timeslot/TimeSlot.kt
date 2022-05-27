package it.polito.timebankingapp.model.timeslot

import android.util.Log
import com.google.firebase.firestore.Exclude
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class TimeSlot(
    var id: String = "",
    var userId: String = "",
    var title: String = "",
    var description: String = "",
    var date: String = "",
    var time: String = "",
    var duration: String = "",
    var location: String = "",
    var restrictions: String = "",
    var relatedSkill: String = ""
) : Serializable {

    override fun toString(): String = "{ title:$title, description: $description, date: $date, time: $time, duration: $duration, location: $location"

    @Exclude
    fun isValid() : Boolean{
        return title != "" && description != "" && date != "" && time != "" && duration != "" && location != "" && restrictions != "" && relatedSkill != ""
    }


    /*Here, I'm not checking that String is not empty, because if it's empty it will be used default image*/
    /*fun isValid(): Boolean {
        return fullName.isNotEmpty() && nick.isNotEmpty() && isValidEmail() && location.isNotEmpty() && description.isNotEmpty()
    }

    private fun isValidEmail(): Boolean {
        return if (TextUtils.isEmpty(email)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }*/
    @Exclude
    fun getCalendar(): Calendar {
        val cal = Calendar.getInstance(TimeZone.getDefault())


        if(date.isNotEmpty() && time.isNotEmpty()) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            cal.time = sdf.parse(this.date + " " + this.time) as Date // all done
        }

        Log.d("getCalendar", cal.timeInMillis.toString())
        return cal
    }

}

