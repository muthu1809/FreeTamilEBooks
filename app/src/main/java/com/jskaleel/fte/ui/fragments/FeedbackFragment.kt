package com.jskaleel.fte.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.jskaleel.fte.R
import com.jskaleel.fte.model.NetWorkMessage
import com.jskaleel.fte.ui.activities.MainActivity
import com.jskaleel.fte.utils.ApiInterface
import com.jskaleel.fte.utils.DeviceUtils
import com.jskaleel.fte.utils.PrintLog
import com.jskaleel.fte.utils.RxBus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_feedback.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class FeedbackFragment : Fragment() {
    lateinit var disposable: Disposable

    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context;
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feedback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolBar.setNavigationOnClickListener {
            DeviceUtils.hideSoftKeyboard(activity!!)
            activity!!.findNavController(R.id.navHostFragment).navigateUp()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ivSend.setOnClickListener {
            submitFeedBack()
        }
    }

    private fun submitFeedBack() {
        val builder = AlertDialog.Builder(mContext, R.style.CustomAlertDialog)
        builder.setCancelable(false)
        builder.setView(R.layout.loader_view)
        val dialog = builder.create()

        val name = edtName.text.toString()
        val email = edtEmail.text.toString()
        val comments = edtComments.text.toString()

        if (name.isBlank() || name.isEmpty()) {
            edtName.error = "Mandatory"
            return
        }

        if (email.isBlank() || email.isEmpty()) {
            edtEmail.error = "Mandatory"
            return
        }

        if (!DeviceUtils.isEmailValid(email)) {
            edtEmail.error = "Invalid e-mail"
            return
        }

        if (comments.isBlank() || comments.isEmpty()) {
            edtComments.error = "Mandatory"
            return
        }

        dialog.show()
        DeviceUtils.hideSoftKeyboard(activity!!)

        val retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://docs.google.com/forms/d/")
            .build().create(ApiInterface::class.java)

        disposable = retrofit.postFeedBack("application/json", name, email, comments)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ _ ->
                run {
                    dialog.dismiss()

                    edtName.setText("")
                    edtEmail.setText("")
                    edtComments.setText("")

                    activity!!.findNavController(R.id.navHostFragment).navigateUp()
                    (activity!! as MainActivity).displayMaterialSnackBar(getString(R.string.thanks_comments))
                }
            }, { error ->
                run {
                    PrintLog.info(error.toString())
                    RxBus.publish(NetWorkMessage(mContext.getString(R.string.try_again_later)))
                }
            })
    }

    /*https://docs.google.com/forms/d/e/1FAIpQLSc_BbE7RfJdUCEgzwGSeiaiUe3ugBdITgJIZY71ED93puqQ3g/formResponse
    * Name : entry_359626196
    * Email: entry_1250945452
    * Comments : entry_1221905149
    * */
}