package com.nenquit.tapntrack.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nenquit.tapntrack.R
import com.nenquit.tapntrack.models.User
import java.text.SimpleDateFormat
import java.util.*

/**
 * UsersAdapter: RecyclerView adapter for displaying users with multi-select support.
 * Supports bulk operations through checkbox selection.
 */
class UsersAdapter(
    private val onUserClickListener: (User) -> Unit,
    private val onSelectionChangedListener: (Int) -> Unit
) : ListAdapter<User, UsersAdapter.UserViewHolder>(UserDiffCallback()) {

    private val selectedUsers = mutableSetOf<String>() // Set of selected user UIDs
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.userName)
        private val emailTextView: TextView = itemView.findViewById(R.id.userEmail)
        private val statusTextView: TextView = itemView.findViewById(R.id.userStatus)
        private val createdDateTextView: TextView = itemView.findViewById(R.id.userCreatedDate)
        private val lastLoginTextView: TextView = itemView.findViewById(R.id.userLastLogin)
        private val selectCheckBox: CheckBox = itemView.findViewById(R.id.userSelectCheckBox)

        fun bind(user: User) {
            nameTextView.text = user.name
            emailTextView.text = user.email

            // Status
            val statusText = if (user.isActive) {
                itemView.context.getString(R.string.user_status_active)
            } else {
                itemView.context.getString(R.string.user_status_inactive)
            }
            statusTextView.text = statusText
            statusTextView.setTextColor(
                itemView.context.getColor(
                    if (user.isActive) android.R.color.holo_green_dark
                    else android.R.color.holo_red_dark
                )
            )

            // Created date
            val createdDate = dateFormat.format(Date(user.createdAt))
            createdDateTextView.text = itemView.context.getString(
                R.string.user_created_format,
                createdDate
            )

            // Last login
            if (user.lastLoginTime > 0) {
                val lastLogin = dateFormat.format(Date(user.lastLoginTime))
                val lastLoginTime = timeFormat.format(Date(user.lastLoginTime))
                lastLoginTextView.text = itemView.context.getString(
                    R.string.user_last_login_format,
                    lastLogin,
                    lastLoginTime
                )
            } else {
                lastLoginTextView.text = itemView.context.getString(R.string.user_last_login_never)
            }

            // Checkbox
            selectCheckBox.isChecked = selectedUsers.contains(user.uid)
            selectCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedUsers.add(user.uid)
                } else {
                    selectedUsers.remove(user.uid)
                }
                onSelectionChangedListener(selectedUsers.size)
            }

            // Item click listener
            itemView.setOnClickListener {
                onUserClickListener(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Get list of selected user UIDs
     */
    fun getSelectedUserIds(): List<String> {
        return selectedUsers.toList()
    }

    /**
     * Select all users
     */
    fun selectAll() {
        selectedUsers.clear()
        selectedUsers.addAll(currentList.map { it.uid })
        onSelectionChangedListener(selectedUsers.size)
        notifyItemRangeChanged(0, itemCount)
    }

    /**
     * Clear all selections
     */
    fun clearSelection() {
        selectedUsers.clear()
        onSelectionChangedListener(0)
        notifyItemRangeChanged(0, itemCount)
    }

    /**
     * Get count of selected users
     */
    fun getSelectionCount(): Int = selectedUsers.size

    /**
     * DiffUtil callback for efficient list updates
     */
    private class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}

