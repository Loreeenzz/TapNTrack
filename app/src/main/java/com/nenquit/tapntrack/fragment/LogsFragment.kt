package com.nenquit.tapntrack.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nenquit.tapntrack.R
import com.nenquit.tapntrack.adapter.LogsAdapter
import com.nenquit.tapntrack.models.Track
import com.nenquit.tapntrack.mvp.logs.LogsContract
import com.nenquit.tapntrack.mvp.logs.LogsPresenter
import java.text.SimpleDateFormat
import java.util.*

/**
 * LogsFragment: Displays RFID attendance logs with filtering, searching, and sorting
 */
class LogsFragment : Fragment(), LogsContract.View {

    private lateinit var presenter: LogsContract.Presenter
    private lateinit var adapter: LogsAdapter

    // UI Components
    private lateinit var rvLogs: RecyclerView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var etSearch: EditText
    private lateinit var btnDateFilter: Button
    private lateinit var btnStatusFilter: Button
    private lateinit var btnSort: Button
    private lateinit var tvLogCount: TextView
    private lateinit var tvPresentCount: TextView
    private lateinit var tvLateCount: TextView
    private lateinit var tvAbsentCount: TextView
    private lateinit var llBulkActions: LinearLayout

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_logs, container, false)

        initializeViews(view)
        setupPresenter()
        setupRecyclerView()
        setupListeners()

        // Load initial data
        presenter.loadLogs()

        return view
    }

    private fun initializeViews(view: View) {
        rvLogs = view.findViewById(R.id.rvLogs)
        llEmptyState = view.findViewById(R.id.llEmptyState)
        progressBar = view.findViewById(R.id.progressBar)
        etSearch = view.findViewById(R.id.etSearch)
        btnDateFilter = view.findViewById(R.id.btnDateFilter)
        btnStatusFilter = view.findViewById(R.id.btnStatusFilter)
        btnSort = view.findViewById(R.id.btnSort)
        tvLogCount = view.findViewById(R.id.tvLogCount)
        tvPresentCount = view.findViewById(R.id.tvPresentCount)
        tvLateCount = view.findViewById(R.id.tvLateCount)
        tvAbsentCount = view.findViewById(R.id.tvAbsentCount)
        llBulkActions = view.findViewById(R.id.llBulkActions)
    }

    private fun setupPresenter() {
        presenter = LogsPresenter(requireContext())
        presenter.attach(this)
    }

    private fun setupRecyclerView() {
        adapter = LogsAdapter(
            onLogClick = { log ->
                navigateToLogDetails(log)
            },
            onLogLongClick = { log ->
                // Long click handled by adapter for selection
                true
            }
        )

        rvLogs.layoutManager = LinearLayoutManager(requireContext())
        rvLogs.adapter = adapter
    }

    private fun setupListeners() {
        // Search functionality
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                presenter.searchLogs(s?.toString() ?: "")
            }
        })

        // Date filter
        btnDateFilter.setOnClickListener {
            showDateFilterDialog()
        }

        // Status filter
        btnStatusFilter.setOnClickListener {
            showStatusFilterDialog()
        }

        // Sort options
        btnSort.setOnClickListener {
            showSortDialog()
        }
    }

    override fun displayLogs(logs: List<Track>) {
        // Logs will be displayed through updateLogsList
    }

    override fun updateLogsList(logs: List<Track>) {
        adapter.updateLogs(logs)

        // Update log count
        val logCount = logs.size
        tvLogCount.text = if (logCount == 1) {
            "$logCount log"
        } else {
            "$logCount logs"
        }

        if (logs.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
        }
    }

    override fun updateSummary(totalPresent: Int, totalLate: Int, totalAbsent: Int) {
        tvPresentCount.text = totalPresent.toString()
        tvLateCount.text = totalLate.toString()
        tvAbsentCount.text = totalAbsent.toString()
    }

    override fun showLoading() {
        progressBar.visibility = View.VISIBLE
        rvLogs.visibility = View.GONE
        llEmptyState.visibility = View.GONE
    }

    override fun hideLoading() {
        progressBar.visibility = View.GONE
        rvLogs.visibility = View.VISIBLE
    }

    override fun showEmptyState() {
        llEmptyState.visibility = View.VISIBLE
        rvLogs.visibility = View.GONE
    }

    override fun hideEmptyState() {
        llEmptyState.visibility = View.GONE
        rvLogs.visibility = View.VISIBLE
    }

    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun clearSelection() {
        adapter.clearSelections()
    }

    override fun navigateToLogDetails(log: Track) {
        val fragment = LogDetailsFragment.newInstance(log.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    @Suppress("unused")
    private fun showDeleteConfirmation(logIds: List<String>) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Logs")
            .setMessage("Are you sure you want to delete ${logIds.size} log(s)?")
            .setPositiveButton("Delete") { _, _ ->
                presenter.bulkDeleteLogs(logIds)
                adapter.clearSelections()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDateFilterDialog() {
        val options = arrayOf(
            "Today",
            "Yesterday",
            "This Week",
            "This Month",
            "Custom Date"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Filter by Date")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> presenter.loadTodayLogs()
                    1 -> {
                        val yesterday = Calendar.getInstance()
                        yesterday.add(Calendar.DAY_OF_YEAR, -1)
                        presenter.loadLogs(dateFormat.format(yesterday.time))
                    }
                    2 -> presenter.loadWeekLogs()
                    3 -> presenter.loadMonthLogs()
                    4 -> showCustomDatePicker()
                }
                updateActiveFiltersDisplay()
            }
            .setNeutralButton("Clear Filter") { _, _ ->
                presenter.loadLogs()
                updateActiveFiltersDisplay()
            }
            .show()
    }

    private fun showCustomDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                presenter.loadLogs(dateFormat.format(calendar.time))
                updateActiveFiltersDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showStatusFilterDialog() {
        val statuses = arrayOf("ALL", "PRESENT", "LATE", "ABSENT")

        AlertDialog.Builder(requireContext())
            .setTitle("Filter by Status")
            .setItems(statuses) { _, which ->
                val selectedStatus = statuses[which]
                if (selectedStatus == "ALL") {
                    presenter.filterByStatus(null)
                } else {
                    presenter.filterByStatus(selectedStatus)
                }
                updateActiveFiltersDisplay()
            }
            .show()
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf(
            "Time (Newest First)",
            "Time (Oldest First)",
            "Student Name (A-Z)",
            "Student Name (Z-A)",
            "Status"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Sort By")
            .setItems(sortOptions) { _, which ->
                when (which) {
                    0 -> presenter.sortLogs("timeIn_desc")
                    1 -> presenter.sortLogs("timeIn_asc")
                    2 -> presenter.sortLogs("studentName_asc")
                    3 -> presenter.sortLogs("studentName_desc")
                    4 -> presenter.sortLogs("status")
                }
            }
            .show()
    }

    private fun updateActiveFiltersDisplay() {
        // Bulk actions bar will be implemented in Phase 3
        // For now, just hide it
        llBulkActions.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }

    companion object {
        fun newInstance(): LogsFragment {
            return LogsFragment()
        }
    }
}
