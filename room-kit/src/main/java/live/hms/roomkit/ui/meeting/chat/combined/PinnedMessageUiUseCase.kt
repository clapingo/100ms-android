package live.hms.roomkit.ui.meeting.chat.combined

import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.Group
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Section
import live.hms.roomkit.ui.meeting.SessionMetadataUseCase
import live.hms.roomkit.ui.meeting.participants.PinnedMessageItem

class PinnedMessageUiUseCase {
    private val pinnedMessagesAdapter = GroupieAdapter()
    fun init(pinnedMessageRecyclerView: RecyclerView) {
        pinnedMessageRecyclerView.adapter = pinnedMessagesAdapter
        pinnedMessageRecyclerView.layoutManager = LinearLayoutManager(pinnedMessageRecyclerView.context)
        pinnedMessageRecyclerView.addItemDecoration(LinePagerIndicatorDecoration())
        PagerSnapHelper().attachToRecyclerView(pinnedMessageRecyclerView)
    }

    fun messagesUpdate(pinnedMessages : Array<SessionMetadataUseCase.PinnedMessage>,
                       pinnedMessagesContainer : ConstraintLayout
                       ) {
        if(pinnedMessages.isEmpty()) {
            pinnedMessagesContainer.visibility = View.GONE
        } else {
            pinnedMessagesContainer.visibility = View.VISIBLE
        }
        val group: Group = Section().apply {
            addAll(pinnedMessages.map { PinnedMessageItem(it) })
        }
        pinnedMessagesAdapter.update(listOf(group))
    }

}