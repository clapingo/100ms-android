package live.hms.roomkit.ui.diagnostic.item

import android.view.View
import androidx.annotation.DrawableRes
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.viewbinding.BindableItem
import live.hms.roomkit.R
import live.hms.roomkit.databinding.ItemDiagnosticHeaderBinding
import live.hms.roomkit.horizontalscroll
import live.hms.roomkit.setDrawables
import live.hms.roomkit.ui.theme.applyTheme

class ExpandableHeader(
    private val title: String,
    private val subTitle: String? = null,
    @DrawableRes private val subtitleIcon: Int,
    private val hideViewMoreUI : Boolean = false,
    private val onExpand : () -> Unit

    ) : BindableItem<ItemDiagnosticHeaderBinding>(), ExpandableItem {

    private  var expand: ExpandableGroup? = null


    override fun bind(binding: ItemDiagnosticHeaderBinding, position: Int) {

        binding.applyTheme()
        binding.root.setOnClickListener {
            expand?.onToggleExpanded()
            onExpand.invoke()
        }

        binding.header.text = title
        binding.header.horizontalscroll()
        binding.subheader.setDrawables(start = binding.subheader.context.resources.getDrawable(subtitleIcon, null))
        binding.subheader.text = subTitle

        binding.viewDetail.visibility = if (hideViewMoreUI) {
            View.GONE
        } else {
            View.VISIBLE

        }
        binding.viewDetail.text = "View detailed information"
    }


    override fun getLayout(): Int = R.layout.item_diagnostic_header
    override fun initializeViewBinding(view: View) = ItemDiagnosticHeaderBinding.bind(view)
    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        this.expand = onToggleListener
    }

}