package com.xando.moki.imagezoom.ui.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.xando.moki.imagezoom.R
import com.xando.moki.imagezoom.utils.FileUriUtil
import kotlinx.android.synthetic.main.main_fragment.*

internal class MainFragment : Fragment(R.layout.main_fragment) {

    companion object {
        fun newInstance(): Fragment = MainFragment()

        private const val GALLERY_REQUEST_CODE = 90

        private val galleryPermissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        private const val GALLERY_CODE_REQUEST_PERMISSION = 91
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sample_choose_image_from_gallery.setOnClickListener { startGallery() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == GALLERY_CODE_REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startGallery()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { sample_image_zoom.setImageURI(it) }
            /*val imagePath = data?.data?.let { getRealPathFromURIPath(it) } ?: ""
            if (imagePath.isNotEmpty()) {
                BitmapFactory.decodeFile(File(imagePath).absolutePath)?.let { bitmap ->
                    sample_image_zoom.setImageBitmap(bitmap)
                }
            }*/
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun startGallery() {
        if (checkGalleryPermission()) {
            val mimeTypes = arrayOf("image/jpeg", "image/png")

            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }

            if (intent.resolveActivity(requireActivity().packageManager) != null)
                startActivityForResult(intent, GALLERY_REQUEST_CODE)
        } else requestGalleryPermission()
    }

    private fun checkGalleryPermission(): Boolean =
        galleryPermissions.all {
            ActivityCompat.checkSelfPermission(
                requireActivity(),
                it
            ) == PackageManager.PERMISSION_GRANTED
        }

    private fun requestGalleryPermission() {
        requestPermissions(galleryPermissions, GALLERY_CODE_REQUEST_PERMISSION)
    }

    private fun getRealPathFromURIPath(contentUri: Uri): String =
        context?.let { ctx -> FileUriUtil.getPathFromURI(ctx, contentUri) } ?: ""
}