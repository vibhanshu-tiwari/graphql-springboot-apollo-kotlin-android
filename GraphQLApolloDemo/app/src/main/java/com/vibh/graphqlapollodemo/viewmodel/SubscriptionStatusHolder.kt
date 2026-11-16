package com.vibh.graphqlapollodemo.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SubscriptionStatusHolder {
    private val _status =
        MutableStateFlow(SubscriptionStatus.CONNECTING)

    val status = _status.asStateFlow()

    fun updateStatus(newStatus: SubscriptionStatus) {
        _status.value = newStatus
    }
}