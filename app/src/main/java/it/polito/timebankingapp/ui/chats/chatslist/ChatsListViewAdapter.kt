package it.polito.timebankingapp.ui.chats.chatslist

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.chat.ChatsListItem

class ChatsListViewAdapter(
    private var data: List<ChatsListItem>,
    private var selectChat: (chatId: String) -> Unit?
) : RecyclerView.Adapter<ChatsListViewAdapter.ItemViewHolder>() {

    private var displayData = data.toMutableList()

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var fullNameText: TextView = itemView.findViewById(R.id.chat_list_item_fullname)
        var messageText: TextView = itemView.findViewById(R.id.chat_list_item_message)
        var timeText: TextView = itemView.findViewById(R.id.chat_list_item_timestamp)
        var numNotifiesText: TextView = itemView.findViewById(R.id.chat_list_item_notifies_number)
        private val imagePic: CircleImageView = itemView.findViewById(R.id.chat_list_item_profile_pic)

        fun bind(cli: ChatsListItem, openChatAction: (v: View) -> Unit) {
//            fullNameText.text = "Nome Cognome" //necessario riferimento usr o timeslotusr
//            messageText.text = cli.chatMessages[cli.chatMessages.size-1].messageText
//            timeText.text = cli.chatMessages[cli.chatMessages.size-1].timestamp.split("-")[1] //se è di oggi mostra l'orario, altrimenti la data
//            numNotifiesText.text =  "(1)" //logica conteggio non letti da implementare in futuro
            fullNameText.text = cli.userName
            messageText.text = cli.lastMessageText
            timeText.text = cli.lastMessageTime
            // sarebbe da mettere il last message della chat dentro il documento in userRooms (per l'anteprima)
            // e anche le altre info riguardo a tempo e conteggio non letti e foto profilo altro utente

            // Use Glide HERE!!!
            loadImageIntoView(imagePic, cli.userPic)

            this.itemView.setOnClickListener(openChatAction)
        }

        private fun loadImageIntoView(view: CircleImageView, url: String) {
            val storageReference = FirebaseStorage.getInstance().reference
            val picRef = storageReference.child(url)
            picRef.downloadUrl
                .addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    Glide.with(view.context)
                        .load(downloadUrl)
                        .into(view)
                }
                .addOnFailureListener { e ->
                    Log.w(
                        "loadImage",
                        "Getting download url was not successful.",
                        e
                    )
                }
        }
    }

    //inflate the item_layout-based structure inside each ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val destination =  R.layout.fragment_chats_list_item

        val vg = LayoutInflater
            .from(parent.context)
            .inflate(destination, parent, false) //attachToRoot: take all you measures
        //but do not attach it immediately to the ViewHolder tree of components (could be a ghost item)

        return ItemViewHolder(vg)
    }

    //populate data for each inflated ViewHolder
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val item = displayData[position]
        holder.bind(item, openChatAction =
        {
            val destination = R.id.action_nav_chatsList_to_nav_chat
            selectChat(item.chatId)
            Navigation.findNavController(it).navigate(
                destination//,
                //bundleOf("point_of_origin" to type, "userId" to item.userId)
            )
        }
        );
    }


    //how many items?
    override fun getItemCount(): Int = displayData.size
}