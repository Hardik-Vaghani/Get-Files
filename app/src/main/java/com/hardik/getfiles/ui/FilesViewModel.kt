package com.hardik.getfiles.ui

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File

class FilesViewModel : ViewModel() {
    private val TAG = FilesViewModel::class.java.simpleName

    private val _allFiles = MutableLiveData<MutableList<File>>()
    val allFiles get() = _allFiles

    private val _extensions = MutableLiveData<MutableList<String>>()
    val extensions get() = _extensions


    private val _allFilesShorted = MutableLiveData<MutableList<File>>()
    val allFilesShorted get() = _allFilesShorted

    fun fetchAllFiles(context: Context) {
        viewModelScope.launch {
            val firstJob = launch {

                val allFiles = mutableListOf<File>()

                // region Fetch files from internal storage
                //endregion
                val internalFilesDir = context.filesDir
                Log.i(TAG, "fetchAllFiles: Internal Files Directory Path: ${internalFilesDir.absolutePath}")

                allFiles.addAll(findAllFiles(internalFilesDir))

                // region Fetch files from external storage
                //endregion
                val externalDirs = ContextCompat.getExternalFilesDirs(context, null)
                externalDirs.forEach { dir ->
                    if (dir != null && dir.exists()) {
                        // Log path of the external directory
                        Log.i(TAG, "External Files Directory Path: ${dir.absolutePath}")

                        allFiles.addAll(findAllFiles(dir))
                    }
                }

                // region Fetch files from the root of external storage (including SD card)
                //endregion
                val rootDir = Environment.getExternalStorageDirectory()
                Log.i(TAG, "fetchAllFiles: Root Files Directory Path:${rootDir.absolutePath}")

                allFiles.addAll(findAllFiles(rootDir))

                // Log the found files
                Log.e(TAG, "fetchAllFiles: ${allFiles.size}")
                _allFiles.postValue(allFiles)

            }
            // Wait for the first coroutine to finish
            firstJob.join()

            val secondJob = launch {
                delay(1000)
                val extensionsList = getExtensions() // Call the suspending function
                println("Unique extensions: ${extensionsList.toString()}")
                _extensions.postValue(extensionsList) // Update LiveData on the main thread
            }
            // Wait for the second coroutine to finish
            secondJob.join()

            val thirdJob = launch {
                println("thirdJob done")
            }

            // Wait for the third coroutine to finish
            thirdJob.join()
        }
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
    }

    private suspend fun getExtensions(): MutableList<String> {
        return withContext(Dispatchers.IO) {
            // Initialize a set for unique extensions
            val uniqueExtensions = mutableSetOf<String>()

            // Iterate over the file list
            for (file in allFiles.value ?: mutableListOf()) {
                uniqueExtensions.add("All")
                if (file.isDirectory) {
                    uniqueExtensions.add("noextension") // Placeholder for directories
                } else {
                    // Extract the file extension
                    val extension = file.name.substringAfterLast('.', "")
                    if (extension.isNotEmpty()) {
                        uniqueExtensions.add(extension) // Add to set if not empty
                    } else {
                        uniqueExtensions.add("noextension") // Placeholder for files without extensions
                    }
                }
            }

            // Convert unique extensions to a List
            uniqueExtensions.toMutableList() // Return as MutableList
        }
    }

    fun fetchAllShortedFiles(type: String? = "", fileList: MutableList<File> = allFiles.value ?: mutableListOf()){
        Log.d(TAG, "fetchAllShortedFiles: ")
        // Launch a coroutine to perform the filtering in the background
        viewModelScope.async (Dispatchers.IO) {
            // Get the current list of all files
            delay(1000)
            async {

                // Filter the files based on the given type
                val filteredFiles = fileList.filter { file ->
                    // Check if the file has the specified extension or no extension
    //                file.extension.equals(type, ignoreCase = true) || file.extension.isEmpty()
                    when {
                        type.equals(
                            "all",
                            ignoreCase = true
                        ) -> true // Return all files if type is "all"
                        type.isNullOrEmpty() -> file.extension.isEmpty() // If type is null or empty, return files with no extension
                        file.extension.equals(
                            type,
                            ignoreCase = true
                        ) -> true // Return files matching the specified extension
                        else -> false // Otherwise, ignore the file
                    }
                }.toMutableList()

                // Update the LiveData for the filtered files on the main thread
                _allFilesShorted.postValue(filteredFiles)

            }.await()
        }
    }
}