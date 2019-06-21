package com.koalatea.sedaily.feature.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.koalatea.sedaily.R
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.ui.dialog.BlockingProgressDialogFragment
import com.koalatea.sedaily.util.supportActionBar
import kotlinx.android.synthetic.main.fragment_auth.*
import kotlinx.android.synthetic.main.include_login.*
import kotlinx.android.synthetic.main.include_register.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG_DIALOG_PROGRESS = "auth_progress_dialog"

private const val VIEW_FLIPPER_CHILD_REGISTRATION = 0

class AuthFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModel()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_auth, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        supportActionBar?.elevation = resources.getDimension(R.dimen.toolbar_elevation)

        authToggleButton.setOnCheckedChangeListener { _, isChecked ->
            viewFlipper.displayedChild = if (isChecked) 1 else 0
        }

        loginButton.setOnClickListener {
            val username = usernameLoginTextInputEditText.text?.trim()?.toString() ?: ""
            val password = passwordLoginTextInputEditText.text?.trim()?.toString() ?: ""

            viewModel.login(username, password)
        }
        registerButton.setOnClickListener {
            val username = usernameRegisterTextInputEditText.text?.trim()?.toString() ?: ""
            val email = emailRegisterTextInputEditText.text?.trim()?.toString() ?: ""
            val password = passwordRegisterTextInputEditText.text?.trim()?.toString() ?: ""

            viewModel.register(username, email, password)
        }

        // Handle keyboard done action.
        passwordRegisterTextInputEditText.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener if (actionId == EditorInfo.IME_ACTION_DONE) {
                registerButton.performClick()
                true
            } else {
                false
            }
        }
        passwordLoginTextInputEditText.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginButton.performClick()
                true
            } else {
                false
            }
        }

        viewModel.authResponseLiveData.observe(this, Observer {
            it.getContentIfNotHandled()?.let { resource ->
                when(resource) {
                    is Resource.Loading -> showBlockingProgressDialog()
                    is Resource.Success -> {
                        hideProgressDialog()

                        // TODO :: Finish and switch to profile fragment
                    }
                    is Resource.Error -> {
                        hideProgressDialog()

                        if (!resource.isConnected) {
                            acknowledgeConnectionError()
                        } else {
                            if (viewFlipper.displayedChild == VIEW_FLIPPER_CHILD_REGISTRATION) {
                                acknowledgeRegistrationFailed(resource.exception.message)
                            } else {
                                acknowledgeLoginFailed(resource.exception.message)
                            }
                        }
                    }
                }
            }
        })

        viewModel.validationLiveData.observe(this, Observer {
            it.getContentIfNotHandled()?.let { validationStatus ->
                if (viewFlipper.displayedChild == VIEW_FLIPPER_CHILD_REGISTRATION) {
                    usernameRegisterTextInputLayout.error = if (validationStatus.isUsernameValid) null else getString(R.string.invalid_username)
                    emailRegisterTextInputLayout.error = if (validationStatus.isEmailValid) null else getString(R.string.invalid_email)
                    passwordRegisterTextInputLayout.error = if (validationStatus.isPasswordValid) null else getString(R.string.invalid_password)
                } else {
                    usernameLoginTextInputLayout.error = if (validationStatus.isUsernameValid) null else getString(R.string.invalid_username)
                    passwordLoginTextInputLayout.error = if (validationStatus.isPasswordValid) null else getString(R.string.invalid_password)
                }
            }
        })
    }

    private fun showBlockingProgressDialog(message: String = getString(R.string.please_wait), tag: String = TAG_DIALOG_PROGRESS)
            = BlockingProgressDialogFragment.show(requireFragmentManager(), message, tag)

    private fun hideProgressDialog(tag: String = TAG_DIALOG_PROGRESS)
            = (fragmentManager?.findFragmentByTag(tag) as? DialogFragment)?.dismiss()

    private fun acknowledgeConnectionError()
            = Snackbar.make(containerConstraintLayout, R.string.error_not_connected, Snackbar.LENGTH_SHORT).show()

    private fun acknowledgeLoginFailed(message: String?)
            = Snackbar.make(containerConstraintLayout, message ?: getString(R.string.error_log_in), Snackbar.LENGTH_SHORT).show()
    private fun acknowledgeRegistrationFailed(message: String?)
            = Snackbar.make(containerConstraintLayout, message ?: getString(R.string.error_generic), Snackbar.LENGTH_SHORT).show()

}