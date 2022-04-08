package com.example.hikingapp.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.R
import com.example.hikingapp.domain.users.Contact
import com.example.hikingapp.ui.settings.ContactsWrapper
import com.example.hikingapp.ui.settings.DeleteContactActivity
import com.example.hikingapp.ui.settings.EditContactActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseUser

class ContactAdapter(
    private var context: Context,
    private var authInfo: FirebaseUser?,
    private var contacts: MutableList<Contact>
) :
    RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

    class ViewHolder(
        context: Context,
        authInfo: FirebaseUser?,
        itemView: View,
        contacts: MutableList<Contact>
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var contactName: TextView = itemView.findViewById(R.id.contact_name) as TextView
        var contactMail: TextView = itemView.findViewById(R.id.contact_mail) as TextView
        var contactPhone: TextView = itemView.findViewById(R.id.contact_number) as TextView
        var editContactButton: ExtendedFloatingActionButton =
            itemView.findViewById(R.id.edit_contact_button) as ExtendedFloatingActionButton
        var deleteContactButton: ExtendedFloatingActionButton =
            itemView.findViewById(R.id.delete_contact_button) as ExtendedFloatingActionButton

        init {

            editContactButton.setOnClickListener {
                val intent = Intent(context, EditContactActivity::class.java)
                intent.putExtra("contactsWrapper", ContactsWrapper(contacts))
                intent.putExtra("position", adapterPosition)
                intent.putExtra("authInfo", authInfo)
                context.startActivity(intent)
            }

            deleteContactButton.setOnClickListener {
                val intent = Intent(context, DeleteContactActivity::class.java)
                intent.putExtra("contactsWrapper", ContactsWrapper(contacts))
                intent.putExtra("position", adapterPosition)
                intent.putExtra("authInfo", authInfo)
                context.startActivity(intent)
            }
        }

        override fun onClick(v: View?) {

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        return ViewHolder(context, authInfo, view, contacts)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.contactName.text = contacts[position].name
        holder.contactMail.text = contacts[position].email
        holder.contactPhone.text = contacts[position].phoneNumber
    }

    override fun getItemCount() = contacts.count()

}
