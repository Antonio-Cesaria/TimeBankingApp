package it.polito.timebankingapp.ui.timeslots.timeslots_list

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.view.View
import android.widget.ImageView

import androidx.core.os.bundleOf
import androidx.navigation.Navigation

import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.R


class TimeSlotAdapter(
    var data: MutableList<TimeSlot>,
    val selectTimeSlot: (Int) -> Unit,
    val type: String
) : RecyclerView.Adapter<TimeSlotAdapter.ItemViewHolder>() {

    //private var filter: Boolean = false
    private var displayData = data.toMutableList()


    class ItemViewHolder(val mainView: View, val type:String) : RecyclerView.ViewHolder(mainView) {
        private val title: TextView = mainView.findViewById(R.id.time_slots_item_title)
        private val location: TextView = mainView.findViewById(R.id.time_slots_item_location)
        private val start: TextView = mainView.findViewById(R.id.time_slots_item_start)
        private val duration: TextView = mainView.findViewById(R.id.time_slots_item_duration)
        private lateinit var editButton: ImageView


        fun bind(ts: TimeSlot, editAction: (v: View) -> Unit, detailAction: (v: View) -> Unit) {
            title.text = ts.title
            location.text = ts.location
            start.text = ts.date.plus(" ").plus(ts.time)
            duration.text = ts.duration.plus(" hour(s)")
            if(type != "skill_specific") {
                editButton = mainView.findViewById(R.id.time_slots_edit_button)
                editButton.setOnClickListener(editAction)
            }

            this.mainView.setOnClickListener(detailAction)
        }

        fun unbind() {
            if(type != "skill_specific") {
                editButton.setOnClickListener(null)
            }
        }
    }

    //inflate the item_layout-based structure inside each ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val destination = if (type === "skill_specific")
            R.layout.skill_specific_timeslots_item_layout
        else
            R.layout.personal_timeslots_item_layout

        val vg = LayoutInflater
            .from(parent.context)
            .inflate(destination, parent, false) //attachToRoot: take all you measures
        //but do not attach it immediately to the ViewHolder tree of components (could be a ghost item)

        return ItemViewHolder(vg, type)
    }

    //populate data for each inflated ViewHolder
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        //holder.name.text = data[position].name
        //holder.role.text = data[position].role

        val item = displayData[position]
        holder.bind(item, editAction =  {//1:17:00
            val pos = data.indexOf(item)
            if (pos != -1) {
                //click on edit button
                if(type != "skill_specific") {
                    Navigation.findNavController(it).navigate(
                        R.id.action_timeSlotListFragment_to_nav_timeSlotEdit,
                        //bundleOf( Pair("id",item.id)) //da fixare la prossima volta appena si aggiunge la shared activity viewmodel
                        bundleOf("timeslot" to item, "position" to position) //temp
                    )
                }
            }
        }, detailAction = {
            val destination = if (type === "skill_specific")
                R.id.action_skillSpecificTimeSlotListFragment_to_nav_timeSlotDetails
            else
                R.id.action_timeSlotListFragment_to_nav_timeSlotDetails

            selectTimeSlot(position)
            Navigation.findNavController(it).navigate(
                destination,
                bundleOf("point_of_origin" to type)
            )
        });



            //Navigation.createNavigateOnClickListener(R.id.action_timeSlotListFragment_to_nav_timeSlotDetails, bundleOf("timeslot" to item)) )
    }

    //how many items?
    override fun getItemCount(): Int = displayData.size
}




class MyDiffCallback(private val old: List<TimeSlot>, private val new: List<TimeSlot>): DiffUtil.Callback() {
    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] === new[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] == new[newItemPosition]
    }
}