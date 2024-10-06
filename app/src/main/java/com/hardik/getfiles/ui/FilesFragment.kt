package com.hardik.getfiles.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.hardik.getfiles.R
import com.hardik.getfiles.adapter.IndexAdapter
import com.hardik.getfiles.adapter.IndexTypeAdapter
import com.hardik.getfiles.databinding.FragmentFilesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FilesFragment : Fragment() {

    companion object { fun newInstance() = FilesFragment() }

    private val TAG = FilesFragment::class.java.simpleName

    private var _binding: FragmentFilesBinding? = null
    private val binding get() = _binding!!

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>

    private lateinit var viewModel: FilesViewModel

    lateinit var indexAdapter: IndexAdapter
    lateinit var indexTypeAdapter: IndexTypeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the ActivityResultLauncher here
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var allGranted = true
            permissions.entries.forEach { entry ->
                if (!entry.value) {
                    allGranted = false
                }
            }

            if (allGranted) {
                // All permissions are granted, proceed with your functionality
                Toast.makeText(requireContext(), "All permissions granted ✔️", Toast.LENGTH_SHORT).show()
                showImageSourceDialog()
            } else {
                // Handle the case where permissions are not granted
                Toast.makeText(requireContext(), "Permissions denied ✖️", Toast.LENGTH_SHORT).show()
            }
        }

        videoPickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    // Fetching the existing files
                    Log.e(TAG, "Selected file URI: $uri")
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //        return inflater.inflate(R.layout.fragment_files, container, false)
        // Inflate the layout using view binding
        _binding = FragmentFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FilesViewModel::class.java)

        setUpRecyclerView()

        binding.extendedFabGetFiles.setOnClickListener { checkPermissions() }

        //region Adapter event /ViewModel observe/ SearchView event
        binding.extendedFabShortFiles.setOnClickListener {
            binding.tvDataDetails.text = ""
            binding.recyclerviewType.visibility = View.VISIBLE

            viewModel.extensions.observe(viewLifecycleOwner) {
                it?.let {
                    indexTypeAdapter.differ.submitList(it)

                    it.forEach { extension ->
                        //[noextension, jpg, png, mp4, jpeg, wav, heic] 2,132,341,4,1,2,4=486
                        Log.e(TAG, "onActivityCreated: extension:$extension"     )
                        CoroutineScope(Dispatchers.IO).launch {
                                async {
                                delay(500)
                                viewModel.fetchAllShortedFiles(if (extension == "noextension") "" else extension)
                            }.await()
                        }
                    }

                }
            }
        }

        indexTypeAdapter.setOnItemClickListener {
            binding.tvDataDetails.text = ""
            viewModel.fetchAllShortedFiles(if (it == "noextension") "" else it)
        }

        viewModel.allFiles.observe(viewLifecycleOwner){
            it?.let {
//                indexAdapter.differ.submitList(it)
                indexAdapter.setOriginalList(it)
                binding.tvDataDetails.text = "Total files:${it.size}"
            }
        }

        viewModel.allFilesShorted.observe(viewLifecycleOwner){ it ->
            it?.let { files ->
//                indexAdapter.differ.submitList(it)
                indexAdapter.setOriginalList(it)
                val extension = files.map { it.extension.ifEmpty { "No extension" } }
                binding.tvDataDetails.text = "${ binding.tvDataDetails.text }\n${extension.get(0)} :${files.size}"
                Log.e(TAG, "onActivityCreated: size:${it.size}")
            }

        }

        // Search for drawer index on Adapter
        binding.searchView.apply {
            // Set query text listener
            setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    // Handle query submission here
                    if (!query.isNullOrBlank()) {
                        // Perform search or filtering based on the query
                        indexAdapter.filter.filter(query)
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    // Handle query text changes here
                    indexAdapter.filter.filter(newText ?: "")
                    return true
                }
            })

            // Set close listener
            setOnCloseListener {
                // Handle close event here, reset any filters or clear search results
                indexAdapter.filter.filter("")
                false // Return true if you have consumed the event
            }
//            this.findViewById<EditText>(androidx.appcompat.R.id.search_src_text).background = null
        }
        //endregion
    }

    private fun checkPermissions() {
        val permissionsNeeded = arrayOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES // For Android 13 and higher
                    Manifest.permission.READ_MEDIA_VIDEO // For Android 13 and higher
                    Manifest.permission.READ_MEDIA_AUDIO // For Android 13 and higher
                }else{
                    ""
                }
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            }
        )

        val permissionsToRequest = permissionsNeeded.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (!permissionsToRequest.isNullOrEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest)
        } else {
            // All permissions are granted, proceed with your functionality
            showImageSourceDialog()
        }
    }

    private fun showImageSourceDialog() {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Select Video Source")
            .setItems(arrayOf("Files")) { dialog, which ->
                when (which) {
                    0 -> onFileSelected() // Select File
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun onFileSelected() {
        viewModel.fetchAllFiles(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up the binding reference to avoid memory leaks
        _binding = null
    }


    private fun setUpRecyclerView() {
        indexAdapter = IndexAdapter()
        binding.recyclerview.apply {
            adapter = indexAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
        indexTypeAdapter = IndexTypeAdapter()
        binding.recyclerviewType.apply {
            adapter = indexTypeAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }

    }
    //region This code using FilesViewModel so check out
    //endregion
   /*
    // region Before trying to access files, ensure that the SD card is mounted and accessible:
    // endregion
    private fun isSDCardAvailable(): Boolean {
        Log.i(TAG, "isSDCardAvailable: ")
        val state = Environment.getExternalStorageState()
        return state == Environment.MEDIA_MOUNTED || state == Environment.MEDIA_MOUNTED_READ_ONLY
    }

   private suspend fun fetchAllFiles() {
        val allFiles = mutableListOf<File>()


        // region Fetch files from internal storage
        //endregion
        val internalFilesDir = requireContext().filesDir
        Log.v(TAG, "fetchAllFiles: Internal Files Directory Path: ${internalFilesDir.absolutePath}")

        allFiles.addAll(findAllFiles(internalFilesDir))

        // region Fetch files from external storage
        //endregion
        val externalDirs = ContextCompat.getExternalFilesDirs(requireContext(), null)
        externalDirs.forEach { dir ->
            if (dir != null && dir.exists()) {
                // Log path of the external directory
                Log.v(TAG, "External Files Directory Path: ${dir.absolutePath}")

                allFiles.addAll(findAllFiles(dir))
            }
        }

        // region Fetch files from the root of external storage (including SD card)
        //endregion
        val rootDir = Environment.getExternalStorageDirectory()
        Log.v(TAG, "fetchAllFiles: Root Files Directory Path:${rootDir.absolutePath}")

        allFiles.addAll(findAllFiles(rootDir))

        // Log the found files
        allFiles.forEach { file ->
            Log.d(TAG, "fetchAllFiles: Found file: ${file.absolutePath}")
        }
        Log.e(TAG, "fetchAllFiles: ${allFiles.size}")
    }

    private suspend fun findAllFiles(directory: File): List<File> {
        return withContext(Dispatchers.IO) {
            val allFiles = mutableListOf<File>()

            // Check if the directory exists and is indeed a directory
            if (directory.exists() && directory.isDirectory) {
                // List files in the directory
                val files = directory.listFiles()
                files?.forEach { file ->
                    if (file.isDirectory) {
                        // Recursively search in subdirectories
                        allFiles.addAll(findAllFiles(file))
                    } else if (file.isFile) {
                        // Add file to the list
                        allFiles.add(file)
                    }
                }
            }

            allFiles
        }
    }*/




}

