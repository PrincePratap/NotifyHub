package com.cody.notifyhub.util

import android.text.format.DateUtils

object TimeFormatter {
    /** "5 minutes ago": used in the list */
    fun relative(ts: Long): String =
        DateUtils.getRelativeTimeSpanString(ts, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()

    /** "14 Nov 2026, 13:46": used on the Details screen */
    fun full(ts: Long): String =
        java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT)
            .format(java.util.Date(ts))
}