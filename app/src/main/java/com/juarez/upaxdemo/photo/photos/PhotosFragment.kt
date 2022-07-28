package com.juarez.upaxdemo.photo.photos

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.juarez.upaxdemo.databinding.FragmentPhotosBinding
import com.juarez.upaxdemo.photo.photo.PhotoViewModel
import com.juarez.upaxdemo.utils.BaseFragment
import com.juarez.upaxdemo.utils.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhotosFragment : BaseFragment<FragmentPhotosBinding>(FragmentPhotosBinding::inflate) {

    private val viewModel: PhotoViewModel by activityViewModels()
    private val photosAdapter = PhotosAdapter { deletePhoto(it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerFirebasePhotos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = photosAdapter
            setHasFixedSize(true)
        }

        ItemTouchHelper(itemSwipe).also { it.attachToRecyclerView(binding.recyclerFirebasePhotos) }

        viewModel.getImages()
        lifecycleScope.launchWhenStarted {
            viewModel.photosState.collect {
                when (it) {
                    is GetPhotosState.Loading -> binding.progressFirePhotos.isVisible = it.isLoading
                    is GetPhotosState.Success -> photosAdapter.submitList(it.data)
                    is GetPhotosState.Error -> {
                        Snackbar.make(binding.root, it.message, Snackbar.LENGTH_INDEFINITE)
                            .setAction("Ok") { toast("close") }
                            .show()
                    }
                    else -> Unit
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.deletePhotoState.collect {
                when (it) {
                    is DeletePhotoState.Loading -> {
                        binding.progressFirePhotos.isVisible = it.isLoading
                    }
                    is DeletePhotoState.Success -> toast("Archivo Eliminado exitosamente")
                    else -> Unit
                }
            }
        }
    }

    private val itemSwipe = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder,
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            viewModel.deletePhoto(position, photosAdapter.currentList)
        }

    }

    private fun deletePhoto(position: Int) {
        viewModel.deletePhoto(position, photosAdapter.currentList)
    }
}