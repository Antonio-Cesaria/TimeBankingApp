package it.polito.timebankingapp.ui.chats.chatslist

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.model.Helper.Companion.fromRequestToChat
import it.polito.timebankingapp.model.Chat


class ChatListViewModel(application: Application): AndroidViewModel(application) {


    private val _chatsList = MutableLiveData<List<Chat>>()
    val chatsList : LiveData<List<Chat>> = _chatsList

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _unreadChats = MutableLiveData<Int>()
    val unreadChats : LiveData<Int> = _unreadChats

    private lateinit var l: ListenerRegistration
    private lateinit var l2: ListenerRegistration

    /*
    private val _hasChatsListBeenCleared = MutableLiveData<Boolean?>()
    var hasChatsListBeenCleared: LiveData<Boolean?> = _hasChatsListBeenCleared


    init {
        _hasChatsListBeenCleared.value = false
    }

    fun setIsClearedFlag(value: Boolean) {
        _hasChatsListBeenCleared.value = value
    }
*/
//    fun retrieveChatMessages(timeslotId: String, requesterId: String ){
//        l = db.collection("chats").document(timeslotId).collection(requesterId).orderBy("timestamp")
//            .addSnapshotListener {
//                v,e ->
//                if(e == null){
//                    _chatMessages.value = v!!.mapNotNull { d -> d.toChatMessage() }
//                } else
//                    _chatMessages.value = emptyList()
//        }
//    }




//    fun addNewMessage(timeslotId: String, requesterId: String, cm : ChatMessage) {
//        //se la chat non esiste ancora, creane una nuova automaticamente
//        val newChatRef = db.collection("chats").document(timeslotId).collection(requesterId).document()
//
//        cm.messageId =newChatRef.id  //imposta id generato da firebase
//        cm.userId = Firebase.auth.currentUser?.uid ?: ""
//
//        newChatRef.set(cm).addOnSuccessListener{
//            Log.d("chat_create","Successfully added")
//        }.addOnFailureListener{
//            Log.d("timeSlots_add", "Error on adding")
//        }
//    }


    override fun onCleared() {
        super.onCleared()

        l.remove()
        if(this::l2.isInitialized)
            l2.remove()
    }

    fun updateAllChats() {
           Log.d("User", Firebase.auth.uid.toString())
            val currentId = Firebase.auth.uid.toString()
            l = db.collection("requests").whereArrayContains("users","${Firebase.auth.uid}")
                .orderBy("lastMessage.timestamp", Query.Direction.DESCENDING).addSnapshotListener{ v, e ->
                    if(e == null){
                        Log.d("chatList", "chatList: ${_chatsList.value}")
                        _chatsList.value = v!!.mapNotNull {  d -> d.toObject<Chat>() }

                            Log.d("chatsListValue", "success")
                    } else{
                        _chatsList.value = emptyList()
                        Log.d("chatsListValue", "failed")
                    }
                }
        }


    /* Download all the chat related to a specific offer that current user has published */
    fun downloadTimeSlotChats(tsId: String) {
//        Log.d("showRequests", "Arrived at ViewModel $tsId")
        Log.d("User", Firebase.auth.uid.toString())
        l = db.collection("requests").whereEqualTo("offerer.id",Firebase.auth.uid.toString()).whereEqualTo("timeSlot.id", tsId)
            .addSnapshotListener{v,e ->
            if(e == null){
                Log.d("chatList", "chatList: ${_chatsList.value}")
                val requests = v!!.mapNotNull {  d -> d.toObject<Chat>()  }
                _chatsList.value = requests.mapNotNull {  r ->
                    fromRequestToChat(r)
                }
                Log.d("chatsListValue", "success")
            } else{
                _chatsList.value = emptyList()
                Log.d("chatsListValue", "failed")
            }
        }
    }

    fun updateUnreadChats(){
        db.collection("requests").whereEqualTo("offerer.id", Firebase.auth.uid)
    }


    fun clearChatList() {
        _chatsList.value = listOf()
    }

}



