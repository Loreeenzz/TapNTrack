package com.nenquit.tapntrack.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nenquit.tapntrack.R

/**
 * LogsFragment: Displays track logs and history.
 */
class LogsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_logs, container, false)
    }

    companion object {
        fun newInstance(): LogsFragment {
            return LogsFragment()
        }
    }
}
