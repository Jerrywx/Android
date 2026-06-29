package com.example.appdemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment

class ContactsFragment : Fragment(R.layout.fragment_contacts) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageButton>(R.id.btn_contacts_plus).setOnClickListener {
            startActivity(Intent(requireContext(), AndroidStudyActivity::class.java))
        }
    }
}
