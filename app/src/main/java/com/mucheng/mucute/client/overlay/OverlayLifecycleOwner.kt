package com.mucheng.mucute.client.overlay

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

class OverlayLifecycleOwner : SavedStateRegistryOwner {

    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: LifecycleRegistry = LifecycleRegistry(this)

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    /**
     * Sets the current lifecycle state.
     */
    fun setCurrentState(state: Lifecycle.State) {
        lifecycle.currentState = state
    }

    /**
     * Handles a lifecycle event.
     */
    fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycle.handleLifecycleEvent(event)
    }

    /**
     * Restores saved state (typically called in initialization).
     */
    fun performRestore(savedState: Bundle?) {
        savedStateController.performRestore(savedState)
        lifecycle.currentState = Lifecycle.State.CREATED
    }

    /**
     * Saves the current state (if needed).
     */
    fun performSave(outBundle: Bundle) {
        savedStateController.performSave(outBundle)
    }
}
