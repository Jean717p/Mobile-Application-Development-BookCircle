package com.mad18.nullpointerexception.takeabook.chatActivity.recyclerview

import android.view.Gravity
import android.widget.FrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.mad18.nullpointerexception.takeabook.chatActivity.model.Message
import com.mad18.nullpointerexception.takeabook.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_text_message.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.wrapContent
import java.text.SimpleDateFormat


abstract class MessageItem(private val message: Message)
    : Item() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        setTimeText(viewHolder)
        setMessageRootGravity(viewHolder)
    }

    private fun setTimeText(viewHolder: ViewHolder) {
        val dateFormat = SimpleDateFormat
                .getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
        viewHolder.textView_message_time.text = dateFormat.format(message.time)
    }

    private fun setMessageRootGravity(viewHolder: ViewHolder) {
        if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
            viewHolder.item_text_message_message_root.apply {
                backgroundResource = R.drawable.rect_round_white
                val lParams = FrameLayout.LayoutParams(wrapContent, wrapContent, Gravity.END)
                this.layoutParams = lParams
            }
        }
        else {
            viewHolder.item_text_message_message_root.apply {
                backgroundResource = R.drawable.rect_round_primary_color
                val lParams = FrameLayout.LayoutParams(wrapContent, wrapContent, Gravity.START)
                this.layoutParams = lParams
            }
        }
    }
}