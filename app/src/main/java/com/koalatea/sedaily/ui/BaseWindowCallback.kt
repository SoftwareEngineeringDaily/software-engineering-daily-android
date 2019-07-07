package com.koalatea.sedaily.ui

import android.annotation.SuppressLint
import android.view.*
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.CallSuper

abstract class BaseWindowCallback(private val localCallback: Window.Callback) : Window.Callback {

    @CallSuper
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return localCallback.dispatchKeyEvent(event)
    }

    @SuppressLint("NewApi")
    @CallSuper
    override fun dispatchKeyShortcutEvent(event: KeyEvent): Boolean {
        return localCallback.dispatchKeyShortcutEvent(event)
    }

    @CallSuper
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return localCallback.dispatchTouchEvent(event)
    }

    @CallSuper
    override fun dispatchTrackballEvent(event: MotionEvent): Boolean {
        return localCallback.dispatchTrackballEvent(event)
    }

    @SuppressLint("NewApi")
    @CallSuper
    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        return localCallback.dispatchGenericMotionEvent(event)
    }

    @CallSuper
    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent): Boolean {
        return localCallback.dispatchPopulateAccessibilityEvent(event)
    }

    @CallSuper
    override fun onCreatePanelView(featureId: Int): View? {
        return localCallback.onCreatePanelView(featureId)
    }

    @CallSuper
    override fun onCreatePanelMenu(featureId: Int, menu: Menu): Boolean {
        return localCallback.onCreatePanelMenu(featureId, menu)
    }

    @CallSuper
    override fun onPreparePanel(featureId: Int, view: View, menu: Menu): Boolean {
        return localCallback.onPreparePanel(featureId, view, menu)
    }

    @CallSuper
    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        return localCallback.onMenuOpened(featureId, menu)
    }

    @CallSuper
    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        return localCallback.onMenuItemSelected(featureId, item)
    }

    @CallSuper
    override fun onWindowAttributesChanged(attrs: WindowManager.LayoutParams) {
        localCallback.onWindowAttributesChanged(attrs)
    }

    @CallSuper
    override fun onContentChanged() {
        localCallback.onContentChanged()
    }

    @CallSuper
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        localCallback.onWindowFocusChanged(hasFocus)
    }

    @CallSuper
    override fun onAttachedToWindow() {
        localCallback.onAttachedToWindow()
    }

    @CallSuper
    override fun onDetachedFromWindow() {
        localCallback.onDetachedFromWindow()
    }

    @CallSuper
    override fun onPanelClosed(featureId: Int, menu: Menu) {
        localCallback.onPanelClosed(featureId, menu)
    }

    @CallSuper
    override fun onSearchRequested(): Boolean {
        return localCallback.onSearchRequested()
    }

    @SuppressLint("NewApi")
    @CallSuper
    override fun onWindowStartingActionMode(callback: ActionMode.Callback): ActionMode? {
        return localCallback.onWindowStartingActionMode(callback)
    }

    @SuppressLint("NewApi")
    @CallSuper
    override fun onActionModeStarted(mode: ActionMode) {
        localCallback.onActionModeStarted(mode)
    }

    @SuppressLint("NewApi")
    @CallSuper
    override fun onActionModeFinished(mode: ActionMode) {
        localCallback.onActionModeFinished(mode)
    }

    @SuppressLint("NewApi")
    @CallSuper
    override fun onSearchRequested(searchEvent: SearchEvent?): Boolean {
        return localCallback.onSearchRequested(searchEvent)
    }

    @SuppressLint("NewApi")
    @CallSuper
    override fun onWindowStartingActionMode(callback: ActionMode.Callback?, type: Int): ActionMode? {
        return localCallback.onWindowStartingActionMode(callback, type)
    }
    
}