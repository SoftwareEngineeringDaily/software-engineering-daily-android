package com.koalatea.sedaily.feature.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.koalatea.sedaily.R
import com.koalatea.sedaily.network.Resource
import com.koalatea.sedaily.ui.dialog.BlockingProgressDialogFragment
import com.koalatea.sedaily.util.supportActionBar
import kotlinx.android.synthetic.main.fragment_auth.*
import kotlinx.android.synthetic.main.include_login.*
import kotlinx.android.synthetic.main.include_register.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG_DIALOG_PROGRESS = "auth_progress_dialog"

private const val VIEW_FLIPPER_CHILD_REGISTERATION = 0

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

        // FIXME :: Handle keyboard login and register

        viewModel.userLiveData.observe(this, Observer {
            it.getContentIfNotHandled()?.let { resource ->
                when(resource) {
                    is Resource.Loading -> showBlockingProgressDialog()
                    is Resource.Success -> {
                        hideProgressDialog()

                        // TODO :: Finish activity with result or check how to deliver message back to navigation lib
                    }
                    is Resource.Error -> {
                        hideProgressDialog()

                        if (!resource.isConnected) {
                            acknowledgeConnectionError()
                        } else {
                            if (viewFlipper.displayedChild == VIEW_FLIPPER_CHILD_REGISTERATION) {
                                acknowledgeRegisterationFailed()
                            } else {
                                acknowledgeLoginFailed()
                            }
                        }
                    }
                }
            }
        })

        viewModel.validationLiveData.observe(this, Observer {
            it.getContentIfNotHandled()?.let { validationStatus ->
                if (viewFlipper.displayedChild == VIEW_FLIPPER_CHILD_REGISTERATION) {
                    usernameRegisterTextInputEditText.error = if (validationStatus.isUsernameValid) null else getString(R.string.invalid_username)
                    emailRegisterTextInputEditText.error = if (validationStatus.isEmailValid) null else getString(R.string.invalid_email)

                    passwordRegisterTextInputLayout.endIconMode = if (validationStatus.isPasswordValid) TextInputLayout.END_ICON_PASSWORD_TOGGLE else TextInputLayout.END_ICON_NONE
                    passwordRegisterTextInputEditText.error = if (validationStatus.isPasswordValid) null else getString(R.string.invalid_password)
                } else {
                    usernameLoginTextInputEditText.error = if (validationStatus.isUsernameValid) null else getString(R.string.invalid_username)

                    passwordLoginTextInputLayout.endIconMode = if (validationStatus.isPasswordValid) TextInputLayout.END_ICON_PASSWORD_TOGGLE else TextInputLayout.END_ICON_NONE
                    passwordLoginTextInputEditText.error = if (validationStatus.isPasswordValid) null else getString(R.string.invalid_password)
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

    private fun acknowledgeLoginFailed()
            = Snackbar.make(containerConstraintLayout, R.string.error_log_in, Snackbar.LENGTH_SHORT).show()
    private fun acknowledgeRegisterationFailed()
            = Snackbar.make(containerConstraintLayout, R.string.error_generic, Snackbar.LENGTH_SHORT).show()

}