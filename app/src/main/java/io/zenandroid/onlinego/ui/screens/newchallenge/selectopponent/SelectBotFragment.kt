package io.zenandroid.onlinego.ui.screens.newchallenge.selectopponent

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.BindableItem
import io.zenandroid.onlinego.R
import io.zenandroid.onlinego.data.model.local.Player
import io.zenandroid.onlinego.data.repositories.BotsRepository
import io.zenandroid.onlinego.databinding.FragmentSelectBotBinding
import io.zenandroid.onlinego.databinding.ItemGameInfoBinding
import org.koin.android.ext.android.get

class SelectBotFragment : Fragment() {

    interface OnOpponentSelected {
        fun onOpponentSelected(opponent: Player)
    }

    private val bots = Section()
    private val botsRepository: BotsRepository = get()
    private lateinit var binding: FragmentSelectBotBinding

    private var groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
        add(object: BindableItem<ItemGameInfoBinding>() {
            override fun bind(binding: ItemGameInfoBinding, position: Int) {
                binding.title.text = "Online Bots"
                binding.value.text = "Online bots are AI programs run and maintained by members of the community at their expense. Playing against them requires an active internet connection."
            }

            override fun getLayout() = R.layout.item_game_info
            override fun initializeViewBinding(view: View): ItemGameInfoBinding = ItemGameInfoBinding.bind(view)
        })
        add(bots)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSelectBotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = groupAdapter
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
        groupAdapter.setOnItemClickListener { item, _ ->
            if(item is OpponentItem) {
                (parentFragment as OnOpponentSelected).onOpponentSelected(item.opponent)
            }
        }
        bots.update(botsRepository
                .bots
                .sortedBy { it.rating }
                .map(::OpponentItem))
    }

    override fun onAttach(context: Context) {
        if(parentFragment !is OnOpponentSelected) {
            throw Exception("Parent context needs to implement OnOpponentSelected")
        }
        super.onAttach(context)
    }
}