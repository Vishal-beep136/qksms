package com.moez.QKSMS.presentation.compose

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RelativeLayout
import com.google.android.flexbox.FlexboxLayoutManager
import com.jakewharton.rxbinding2.view.keys
import com.jakewharton.rxbinding2.widget.textChanges
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.extensions.dpToPx
import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.presentation.base.QkAdapter
import com.moez.QKSMS.presentation.base.QkViewHolder
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.contact_chip.view.*

class ChipsAdapter(
        private val context: Context,
        private val view: RecyclerView,
        data: Flowable<List<Contact>>) : QkAdapter<Contact, QkViewHolder>(data) {

    companion object {
        private val TYPE_EDIT_TEXT = 0
        private val TYPE_ITEM = 1
    }

    private val hint: String = context.getString(R.string.title_compose)
    private val editText: EditText = AppCompatEditText(context)

    val chipDeleted: PublishSubject<Contact> = PublishSubject.create<Contact>()
    val textChanges = editText.textChanges()

    init {
        val wrap = ViewGroup.LayoutParams.WRAP_CONTENT
        editText.layoutParams = FlexboxLayoutManager.LayoutParams(wrap, wrap).apply {
            minWidth = 56.dpToPx(context)
            flexGrow = 8f
        }

        editText.hint = hint
        editText.textSize = 16f
        editText.setTextColor(ContextCompat.getColor(context, R.color.textPrimary))
        editText.setHintTextColor(ContextCompat.getColor(context, R.color.textTertiary))
        editText.setBackgroundResource(android.R.color.transparent)
        editText.inputType = InputType.TYPE_TEXT_VARIATION_FILTER or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        editText.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
        editText.privateImeOptions = "nm"

        editText.keys()
                .filter { event -> event.action == KeyEvent.ACTION_DOWN }
                .filter { event -> event.keyCode == KeyEvent.KEYCODE_DEL }
                .subscribe {
                    if (itemCount > 1 && editText.text.isEmpty()) {
                        chipDeleted.onNext(getItem(itemCount - 2))
                    }
                }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TYPE_EDIT_TEXT -> QkViewHolder(editText)
        else -> QkViewHolder(LayoutInflater.from(context).inflate(R.layout.contact_chip, parent, false))
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_EDIT_TEXT -> {
                if (itemCount == 1) {
                    editText.hint = hint
                } else {
                    editText.hint = null
                    editText.text = null
                }

                editText.requestFocus()
            }

            else -> {
                val contact = getItem(position)
                val view = holder.itemView

                view.avatar.contact = contact
                view.name.text = contact.name

                view.setOnClickListener { showDetailedChip(contact) }
            }
        }
    }

    override fun getItemCount() = super.getItemCount() + 1

    override fun getItemViewType(position: Int) = if (position == itemCount - 1) TYPE_EDIT_TEXT else TYPE_ITEM

    private fun showDetailedChip(contact: Contact) {
        val detailedChipView = DetailedChipView(context)
        detailedChipView.setContact(contact)

        val rootView = view.rootView as ViewGroup

        val layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

        layoutParams.topMargin = 24.dpToPx(context)
        layoutParams.marginStart = 56.dpToPx(context)

        rootView.addView(detailedChipView, layoutParams)
        detailedChipView.show()

        detailedChipView.setOnDeleteListener {
            chipDeleted.onNext(contact)
            detailedChipView.hide()
        }
    }

}