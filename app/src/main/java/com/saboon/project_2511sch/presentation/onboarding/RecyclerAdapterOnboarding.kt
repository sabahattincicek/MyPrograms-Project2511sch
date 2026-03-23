package com.saboon.project_2511sch.presentation.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.databinding.RowOnboardingBinding

class RecyclerAdapterOnboarding (
    private val pages: List<OnboardingPage>
): RecyclerView.Adapter<RecyclerAdapterOnboarding.OnboardingViewHolder>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OnboardingViewHolder {
        val binding = RowOnboardingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: OnboardingViewHolder,
        position: Int
    ) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int {
        return  pages.size
    }

    inner class OnboardingViewHolder(private val binding: RowOnboardingBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(page: OnboardingPage){
            binding.tvTitle.text = page.title
            binding.tvDescription.text = page.description
            binding.ivOnboarding.setImageResource(page.imageRes)
        }
    }
}