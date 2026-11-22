package ua.turskyi.travelling.features.account.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import ua.turskyi.domain.interactor.AuthInteractor
import ua.turskyi.domain.interactor.CloudSyncInteractor
import ua.turskyi.travelling.R
import ua.turskyi.travelling.databinding.ActivityAccountBinding
import ua.turskyi.travelling.features.account.viewmodel.AccountActivityViewModel

class AccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountBinding
    private val viewModel: AccountActivityViewModel by inject()
    private val authInteractor: AuthInteractor by inject()
    private val cloudSyncInteractor: CloudSyncInteractor by inject()

    private var pendingKeepSignedIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        initView()
        initObservers()
    }

    private fun initView() {
        binding.cbKeepSignedIn.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setKeepSignedIn(isChecked)
        }

        binding.btnSignInGoogle.setOnClickListener {
            pendingKeepSignedIn = binding.cbKeepSignedIn.isChecked
            authInteractor.setKeepSignedIn(pendingKeepSignedIn)
            signInWithGoogleCredentialManager()
        }

        binding.btnSignOut.setOnClickListener {
            viewModel.signOut()
        }

        binding.btnRestore.setOnClickListener {
            performRestore()
        }

        binding.btnDeleteAccount.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                getString(R.string.url_delete_account).toUri()
            )
            startActivity(intent)
        }
    }

    private fun signInWithGoogleCredentialManager() {
        lifecycleScope.launch {
            try {
                val credentialManager = CredentialManager.create(this@AccountActivity)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(this@AccountActivity, request)
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                val idToken = googleIdTokenCredential.idToken

                val signInResult = authInteractor.signInWithGoogleCredential(idToken, pendingKeepSignedIn)
                signInResult.onSuccess { user ->
                    Toast.makeText(
                        this@AccountActivity,
                        getString(R.string.status_signed_in_as, user.email.ifEmpty { user.displayName }),
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.loadUser()
                    val travellerRepo: ua.turskyi.domain.repository.TravellerRepository by inject()
                    travellerRepo.saveTraveller({}, {})
                    performRestore()
                }.onFailure { e ->
                    Toast.makeText(
                        this@AccountActivity,
                        getString(R.string.msg_sign_in_failed, e.localizedMessage ?: e.toString()),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: GetCredentialException) {
                Log.e("AccountActivity", "Credential Manager sign in canceled/failed", e)
                Toast.makeText(
                    this@AccountActivity,
                    getString(R.string.msg_google_signin_failed, e.localizedMessage ?: e.toString()),
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.e("AccountActivity", "Credential Manager sign in error", e)
                Toast.makeText(
                    this@AccountActivity,
                    getString(R.string.msg_google_signin_failed, e.localizedMessage ?: e.toString()),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun performRestore() {
        lifecycleScope.launch {
            Toast.makeText(this@AccountActivity, getString(R.string.msg_restoring_cloud_data), Toast.LENGTH_SHORT).show()
            val result = cloudSyncInteractor.restoreDataFromCloud()
            result.onSuccess {
                Toast.makeText(this@AccountActivity, getString(R.string.msg_cloud_restored_success), Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { e ->
                Log.e("AccountActivity", "Restore failed", e)
                Toast.makeText(
                    this@AccountActivity,
                    "${e.javaClass.simpleName}: ${e.localizedMessage ?: getString(R.string.msg_cloud_restore_failed)}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun initObservers() {
        viewModel.userAccount.observe(this) { user ->
            if (user != null) {
                binding.tvAccountStatus.text =
                    getString(R.string.status_signed_in_as, user.email.ifEmpty { user.displayName })
                binding.cbKeepSignedIn.isChecked = user.keepSignedIn
                binding.btnSignInGoogle.visibility = View.GONE
                binding.btnSignOut.visibility = View.VISIBLE
                binding.btnSignOut.isEnabled = true
                binding.btnRestore.isEnabled = true
                binding.btnDeleteAccount.visibility = View.VISIBLE
            } else {
                binding.tvAccountStatus.text = getString(R.string.status_not_signed_in)
                binding.cbKeepSignedIn.isChecked = false
                binding.btnSignInGoogle.visibility = View.VISIBLE
                binding.btnSignOut.visibility = View.GONE
                binding.btnRestore.isEnabled = false
                binding.btnDeleteAccount.visibility = View.GONE
            }
        }

        viewModel.message.observe(this) { msg ->
            if (msg.isNotEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
        }
    }
}
