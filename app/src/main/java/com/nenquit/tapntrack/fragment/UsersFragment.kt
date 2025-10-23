package com.nenquit.tapntrack.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nenquit.tapntrack.R
import com.nenquit.tapntrack.adapter.UsersAdapter
import com.nenquit.tapntrack.models.User
import com.nenquit.tapntrack.mvp.users.UsersContract
import com.nenquit.tapntrack.mvp.users.UsersPresenter

/**
 * UsersFragment: Displays user management and administration features.
 * Implements MVP pattern with UsersPresenter as the business logic handler.
 * Features: User list, search, filter, sort, and bulk operations.
 */
class UsersFragment : Fragment(), UsersContract.View {
    private lateinit var presenter: UsersContract.Presenter
    private lateinit var adapter: UsersAdapter
    private var currentToast: Toast? = null

    // UI components
    private lateinit var userCountTextView: TextView
    private lateinit var searchEditText: EditText
    private lateinit var filterStatusButton: Button
    private lateinit var sortButton: Button
    private lateinit var resetFiltersButton: Button
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var bulkActionsBar: LinearLayout
    private lateinit var selectAllCheckBox: CheckBox
    private lateinit var selectionCountTextView: TextView
    private lateinit var bulkActionsMenuButton: Button
    private lateinit var emptyStateLayout: LinearLayout

    // State
    private var currentSortBy = "name"
    private var currentStatusFilter: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize presenter
        presenter = UsersPresenter()
        presenter.attach(this)

        // Initialize UI components
        initializeUIComponents(view)

        // Set up RecyclerView
        setupRecyclerView()

        // Set up click listeners
        setupClickListeners()

        // Load users
        presenter.loadUsers()
    }

    /**
     * Initialize UI components by finding views
     */
    private fun initializeUIComponents(view: View) {
        userCountTextView = view.findViewById(R.id.userCountTextView)
        searchEditText = view.findViewById(R.id.searchEditText)
        filterStatusButton = view.findViewById(R.id.filterStatusButton)
        sortButton = view.findViewById(R.id.sortButton)
        resetFiltersButton = view.findViewById(R.id.resetFiltersButton)
        usersRecyclerView = view.findViewById(R.id.usersRecyclerView)
        bulkActionsBar = view.findViewById(R.id.bulkActionsBar)
        selectAllCheckBox = view.findViewById(R.id.selectAllCheckBox)
        selectionCountTextView = view.findViewById(R.id.selectionCountTextView)
        bulkActionsMenuButton = view.findViewById(R.id.bulkActionsMenuButton)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
    }

    /**
     * Set up RecyclerView
     */
    private fun setupRecyclerView() {
        adapter = UsersAdapter(
            { user -> presenter.onUserSelected(user) },
            { selectedCount -> updateBulkActionsUI(selectedCount) }
        )

        usersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@UsersFragment.adapter
        }
    }

    /**
     * Set up click listeners for UI components
     */
    private fun setupClickListeners() {
        searchEditText.setOnEditorActionListener { _, _, _ ->
            val query = searchEditText.text.toString()
            presenter.searchUsers(query)
            false
        }

        filterStatusButton.setOnClickListener {
            showStatusFilterMenu()
        }

        sortButton.setOnClickListener {
            showSortMenu()
        }

        resetFiltersButton.setOnClickListener {
            searchEditText.text.clear()
            currentSortBy = "name"
            currentStatusFilter = null
            filterStatusButton.text = getString(R.string.users_filter_all_status)
            sortButton.text = getString(R.string.users_sort_name)
            presenter.resetFilters()
        }

        selectAllCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                adapter.selectAll()
            } else {
                adapter.clearSelection()
            }
        }

        bulkActionsMenuButton.setOnClickListener {
            showBulkActionsMenu()
        }
    }

    /**
     * Show status filter menu
     */
    private fun showStatusFilterMenu() {
        val options = arrayOf(
            getString(R.string.users_filter_all_status),
            getString(R.string.user_status_active),
            getString(R.string.user_status_inactive)
        )
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Filter by Status")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    currentStatusFilter = null
                    filterStatusButton.text = getString(R.string.users_filter_all_status)
                    presenter.resetFilters()
                }
                1 -> {
                    currentStatusFilter = true
                    filterStatusButton.text = getString(R.string.users_filter_status_active)
                    presenter.filterByStatus(true)
                }
                2 -> {
                    currentStatusFilter = false
                    filterStatusButton.text = getString(R.string.users_filter_status_inactive)
                    presenter.filterByStatus(false)
                }
            }
        }
        builder.show()
    }

    /**
     * Show sort menu
     */
    private fun showSortMenu() {
        val options = arrayOf(
            getString(R.string.users_sort_name),
            getString(R.string.users_sort_created),
            getString(R.string.users_sort_last_login)
        )
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Sort Users")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    currentSortBy = "name"
                    sortButton.text = getString(R.string.users_sort_name)
                    presenter.sortUsers("name")
                }
                1 -> {
                    currentSortBy = "createdAt"
                    sortButton.text = getString(R.string.users_sort_created)
                    presenter.sortUsers("createdAt")
                }
                2 -> {
                    currentSortBy = "lastLogin"
                    sortButton.text = getString(R.string.users_sort_last_login)
                    presenter.sortUsers("lastLogin")
                }
            }
        }
        builder.show()
    }

    /**
     * Show bulk actions menu
     */
    private fun showBulkActionsMenu() {
        val selectedCount = adapter.getSelectionCount()
        if (selectedCount == 0) {
            showError(getString(R.string.users_no_selection_error))
            return
        }

        val options = arrayOf("Activate Selected", "Deactivate Selected", "Delete Selected")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.users_bulk_actions_title, selectedCount))
        builder.setItems(options) { _, which ->
            val selectedIds = adapter.getSelectedUserIds()
            when (which) {
                0 -> {
                    showConfirmation(
                        getString(R.string.users_activate_confirm, selectedCount),
                        "Yes"
                    ) { presenter.bulkActivateUsers(selectedIds) }
                }
                1 -> {
                    showConfirmation(
                        getString(R.string.users_deactivate_confirm, selectedCount),
                        "Yes"
                    ) { presenter.bulkDeactivateUsers(selectedIds) }
                }
                2 -> {
                    showConfirmation(
                        getString(R.string.users_delete_confirm, selectedCount),
                        "Delete"
                    ) { presenter.bulkDeleteUsers(selectedIds) }
                }
            }
        }
        builder.show()
    }

    /**
     * Update bulk actions UI when selection changes
     */
    private fun updateBulkActionsUI(selectedCount: Int) {
        if (selectedCount > 0) {
            bulkActionsBar.visibility = View.VISIBLE
            selectionCountTextView.text = getString(R.string.users_selection_count_format, selectedCount)
        } else {
            bulkActionsBar.visibility = View.GONE
            selectAllCheckBox.isChecked = false
        }
    }

    /**
     * Show confirmation dialog
     */
    private fun showConfirmation(message: String, buttonText: String, onConfirm: () -> Unit) {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmation")
        builder.setMessage(message)
        builder.setPositiveButton(buttonText) { _, _ -> onConfirm() }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    override fun showLoading() {
        // Show progress indicator if needed
    }

    override fun hideLoading() {
        // Hide progress indicator if needed
    }

    override fun showError(message: String) {
        cancelCurrentToast()
        currentToast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }

    override fun showSuccess(message: String) {
        cancelCurrentToast()
        currentToast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }

    override fun displayUsers(users: List<User>) {
        adapter.submitList(users)
        val pluralSuffix = if (users.size != 1) "s" else ""
        userCountTextView.text = getString(R.string.users_user_count_format, users.size, pluralSuffix)
        updateEmptyState(users.isEmpty())
    }

    override fun updateUserList(users: List<User>) {
        adapter.submitList(users)
        val pluralSuffix = if (users.size != 1) "s" else ""
        userCountTextView.text = getString(R.string.users_user_count_format, users.size, pluralSuffix)
        updateEmptyState(users.isEmpty())
    }

    override fun navigateToUserDetails(user: User) {
        val fragment = UserDetailsFragment.newInstance(user.uid)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun showDeactivateConfirmation(userId: String, userName: String) {
        showConfirmation(
            "Deactivate $userName?",
            "Yes"
        ) { presenter.deactivateUser(userId) }
    }

    override fun showDeleteConfirmation(userId: String, userName: String) {
        showConfirmation(
            "Delete $userName? This cannot be undone.",
            "Delete"
        ) { presenter.deleteUser(userId) }
    }

    override fun setBulkActionsEnabled(enabled: Boolean) {
        bulkActionsMenuButton.isEnabled = enabled
    }

    override fun showBulkActionsDialog(selectedCount: Int) {
        showBulkActionsMenu()
    }

    override fun clearSelection() {
        adapter.clearSelection()
        bulkActionsBar.visibility = View.GONE
        selectAllCheckBox.isChecked = false
    }

    /**
     * Update empty state visibility
     */
    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            usersRecyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            usersRecyclerView.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
        }
    }

    /**
     * Cancel current toast if exists
     */
    private fun cancelCurrentToast() {
        currentToast?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detach()
        cancelCurrentToast()
    }

    companion object {
        fun newInstance(): UsersFragment {
            return UsersFragment()
        }
    }
}
