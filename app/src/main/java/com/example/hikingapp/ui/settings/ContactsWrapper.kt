package com.example.hikingapp.ui.settings

import com.example.hikingapp.domain.users.Contact
import java.io.Serializable

class ContactsWrapper(var contacts: MutableList<Contact>): Serializable {

}