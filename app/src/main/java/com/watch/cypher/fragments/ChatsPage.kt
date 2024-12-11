package com.watch.cypher.fragments

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.watch.cypher.R
import com.watch.cypher.adapters.ChatsAdapter
import com.watch.cypher.adapters.ContactsAdapter
import com.watch.cypher.dataManager.AppDao
import com.watch.cypher.dataManager.AppDatabase
import com.watch.cypher.dataModel.Chats
import com.watch.cypher.dataModel.ContactData
import com.watch.cypher.dataModel.MessageData
import com.watch.cypher.databinding.FragmentChatsPageBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatsPage : Fragment(R.layout.fragment_chats_page) {
    private lateinit var binding: FragmentChatsPageBinding
    private var allPosts = mutableListOf<ContactData>()
    private var allPosts2 = mutableListOf<ContactData>()
    private var allPosts3 = mutableListOf<Chats>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChatsPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val searchView = requireView().findViewById<androidx.appcompat.widget.SearchView>(R.id.srchv)
        val searchTextView = searchView.findViewById<TextView>(androidx.appcompat.R.id.search_src_text)
        searchTextView.setTextColor(Color.BLACK)
        searchTextView.setHintTextColor(Color.GRAY)
        val textSizeInPixels = 14 * resources.displayMetrics.density
        searchTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPixels)
        val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        searchIcon.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
        val closeIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeIcon.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
        allPosts2.addAll(allPosts)
        val db = AppDatabase.getDatabase(requireContext())
        val appDao = db.appDao()
        lifecycleScope.launch {
            val contacts = getContactsList(appDao)

            binding.swiper.setOnRefreshListener {
                lifecycleScope.launch {
                    allPosts3.clear()
                    allPosts3.addAll(getContactsList(appDao))
                    binding.mainRecyclerview.adapter?.notifyDataSetChanged()
                    binding.swiper.isRefreshing = false
                }
            }

            allPosts3.addAll(contacts)
            // Step 3: Set up the RecyclerView with the fetched contacts
            binding.mainRecyclerview.apply {
                layoutManager = LinearLayoutManager(this.context)
                adapter = ChatsAdapter(requireContext(), allPosts3).apply {
                    setOnItemClickListener(object : ChatsAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                            if (allPosts3[position].contact.conversationType.toString() == "ONLINE"){
                                val bundle = Bundle()
                                bundle.putInt("convoType", 0)
                                bundle.putParcelable("contactData", allPosts3[position].contact)
                                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                                val frg = ConversationPage()
                                frg.arguments = bundle
                                // Check if the fragment is already added
                                if (!frg.isAdded) {
                                    transaction.add(R.id.screensholder, frg, "ConversationPage")
                                }

                                // Hide all fragments
                                requireActivity().supportFragmentManager.fragments.forEach { transaction.hide(it) }

                                // Show the selected fragment
                                transaction.show(frg)
                                transaction.addToBackStack(null)
                                transaction.commit()
                            }
                            else{
                                val bundle = Bundle()
                                bundle.putParcelable("contactData", allPosts[position])
                                Log.d("checkfs", "un: ${allPosts[position].conversationId}")
                                Log.d("checkfs", "uno: ${allPosts[position].BTID!!}")
                                Log.d("checkfs", "unod: ${extractAddressFromUniqueId(allPosts[position].BTID!!)}")

                                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                                val frg = ConversationPage()
                                frg.arguments = bundle
                                // Check if the fragment is already added
                                if (!frg.isAdded) {
                                    transaction.add(R.id.screensholder, frg, "ConversationPage")
                                }

                                // Hide all fragments
                                requireActivity().supportFragmentManager.fragments.forEach { transaction.hide(it) }

                                // Show the selected fragment
                                transaction.show(frg)
                                transaction.addToBackStack(null)
                                transaction.commit()

                            }

                        }
                    })
                }
            }
        }

    }
    fun extractAddressFromUniqueId(uniqueId: String): String {
        val decodedId = String(Base64.decode(uniqueId, Base64.NO_WRAP)) // Decode Base64
        return decodedId.substringBefore("-") // Get the address part
    }

    fun filterContacts(query: String?) {
        if (query.isNullOrEmpty()) {
            // If the query is empty, reset the filtered list to show all posts
            allPosts2.clear()
            allPosts2.addAll(allPosts)
        } else {
            // Filter the contacts where the username starts with the query text (case insensitive)
            allPosts2.clear() // Clear previous results
            allPosts2.addAll(
                allPosts.filter { contact ->
                    contact.username.startsWith(query, ignoreCase = true)
                }
            )
        }

        binding.mainRecyclerview.adapter?.notifyDataSetChanged()

    }

    private suspend fun getContactsList(appDao: AppDao): List<Chats> {
        val contactsList = appDao.getAllContacts()
        val ChatList = mutableListOf<Chats>()
        for (i in contactsList){
            ChatList.add(Chats(i,getLastMessage(i.conversationId)))
        }
        return ChatList
    }

    private suspend fun getLastMessage(conversationId: String): MessageData? {
        return withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            val appDao = db.appDao()
            // Perform the database query in the background
            appDao.getLastMessageByConversation(conversationId)
        }
    }
}