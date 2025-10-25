package com.nenquit.tapntrack.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nenquit.tapntrack.R
import com.nenquit.tapntrack.adapter.RecentActivityAdapter
import com.nenquit.tapntrack.models.Track
import com.nenquit.tapntrack.mvp.dashboard.DashboardContract
import com.nenquit.tapntrack.mvp.dashboard.DashboardPresenter

/**
 * HomeFragment: Displays the main dashboard with quick stats and overview.
 * Shows today's attendance, user statistics, and recent activity.
 */
class HomeFragment : Fragment(), DashboardContract.View {

    private lateinit var presenter: DashboardContract.Presenter
    private lateinit var recentActivityAdapter: RecentActivityAdapter

    // UI Components
    private lateinit var tvCurrentDateTime: TextView
    private lateinit var tvTotalStudents: TextView
    private lateinit var tvPresentCount: TextView
    private lateinit var tvLateCount: TextView
    private lateinit var tvAbsentCount: TextView
    private lateinit var tvTotalStudentsCount: TextView
    private lateinit var tvTotalTeachersCount: TextView
    private lateinit var tvTotalLogsCount: TextView
    private lateinit var rvRecentActivity: RecyclerView
    private lateinit var tvEmptyActivity: TextView
    private lateinit var btnViewAllLogs: Button
    private lateinit var btnViewAllUsers: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        initializeViews(view)
        setupPresenter()
        setupRecyclerView()
        setupListeners()

        // Load dashboard data
        presenter.loadDashboardData()

        return view
    }

    private fun initializeViews(view: View) {
        tvCurrentDateTime = view.findViewById(R.id.tvCurrentDateTime)
        tvTotalStudents = view.findViewById(R.id.tvTotalStudents)
        tvPresentCount = view.findViewById(R.id.tvPresentCount)
        tvLateCount = view.findViewById(R.id.tvLateCount)
        tvAbsentCount = view.findViewById(R.id.tvAbsentCount)
        tvTotalStudentsCount = view.findViewById(R.id.tvTotalStudentsCount)
        tvTotalTeachersCount = view.findViewById(R.id.tvTotalTeachersCount)
        tvTotalLogsCount = view.findViewById(R.id.tvTotalLogsCount)
        rvRecentActivity = view.findViewById(R.id.rvRecentActivity)
        tvEmptyActivity = view.findViewById(R.id.tvEmptyActivity)
        btnViewAllLogs = view.findViewById(R.id.btnViewAllLogs)
        btnViewAllUsers = view.findViewById(R.id.btnViewAllUsers)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupPresenter() {
        presenter = DashboardPresenter()
        presenter.attach(this)
    }

    private fun setupRecyclerView() {
        recentActivityAdapter = RecentActivityAdapter(emptyList()) { log ->
            presenter.onRecentActivityClick(log)
        }
        rvRecentActivity.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recentActivityAdapter
        }
    }

    private fun setupListeners() {
        btnViewAllLogs.setOnClickListener {
            presenter.onViewAllLogsClick()
        }

        btnViewAllUsers.setOnClickListener {
            presenter.onViewAllUsersClick()
        }
    }

    override fun displayTodayAttendance(presentCount: Int, lateCount: Int, absentCount: Int, totalStudents: Int) {
        tvTotalStudents.text = getString(R.string.dashboard_total_students_format, totalStudents)
        tvPresentCount.text = presentCount.toString()
        tvLateCount.text = lateCount.toString()
        tvAbsentCount.text = absentCount.toString()
    }

    override fun displayQuickStats(totalStudents: Int, totalTeachers: Int, logsThisWeek: Int) {
        tvTotalStudentsCount.text = totalStudents.toString()
        tvTotalTeachersCount.text = totalTeachers.toString()
        tvTotalLogsCount.text = logsThisWeek.toString()
    }

    override fun displayRecentActivity(recentLogs: List<Track>) {
        if (recentLogs.isEmpty()) {
            rvRecentActivity.visibility = View.GONE
            tvEmptyActivity.visibility = View.VISIBLE
        } else {
            rvRecentActivity.visibility = View.VISIBLE
            tvEmptyActivity.visibility = View.GONE
            recentActivityAdapter.updateData(recentLogs)
        }
    }

    override fun updateDateTime(dateTime: String) {
        tvCurrentDateTime.text = dateTime
    }

    override fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun navigateToLogs() {
        // Switch to Logs tab (position 1)
        activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
            ?.selectedItemId = R.id.menu_logs
    }

    override fun navigateToUsers() {
        // Switch to Users tab (position 2)
        activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
            ?.selectedItemId = R.id.menu_users
    }

    override fun navigateToLogDetails(log: Track) {
        // Navigate to log details fragment
        val logDetailsFragment = LogDetailsFragment.newInstance(log.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, logDetailsFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detach()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to dashboard
        presenter.refreshData()
    }

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}
