package com.example.presenskuy

import android.content.Intent
import android.widget.LinearLayout
import android.view.View
import android.widget.TextView
import android.graphics.Color
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.widget.Button
import android.content.Context
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import java.util.*

class BookmarksActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bookmark_item)

        // Set the toolbar as the action bar for the activity
        val toolbar = findViewById<Toolbar>(R.id.toolbar3)
        toolbar.title = ""

        setSupportActionBar(toolbar)

        // Add a back button to the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bookmarkedList = loadBookmarks()
        // Log the value of the bookmarked products list
        Log.d("BookmarksActivity", "The bookmarked products are: $bookmarkedList")
        val bookmarksParentLayout = findViewById<LinearLayout>(R.id.bookmark_item_layout)
        val emptyTextView = findViewById<LinearLayout>(R.id.empty_text_view)

        if (bookmarkedList != null && bookmarkedList.isNotEmpty()) {
            for (bookmarkedProduct in bookmarkedList) {
                // Create a layout for each product
                val productLayout = LinearLayout(this)
                productLayout.orientation = LinearLayout.HORIZONTAL
                productLayout.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                productLayout.setPadding(20, 20, 20, 20)

                // Add product to layout
                val productDetailsLayout = LinearLayout(this)
                productDetailsLayout.orientation = LinearLayout.VERTICAL
                productDetailsLayout.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                ) // Set weight to 1

                productDetailsLayout.setPadding(20, 0, 0, 0)

                val productNameTextView = TextView(this)
                productNameTextView.text = bookmarkedProduct.name
                productNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                productNameTextView.setTextColor(Color.BLACK)
                productDetailsLayout.addView(productNameTextView)

                val productPriceTextView = TextView(this)
                productPriceTextView.text = bookmarkedProduct.pass

                // Set the input type to password variation
                productPriceTextView.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                // Set the text appearance to display asterisks
                productPriceTextView.transformationMethod = PasswordTransformationMethod.getInstance()
                // Customize other attributes if needed
                productPriceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                productPriceTextView.setTextColor(Color.GRAY)

                productDetailsLayout.addView(productPriceTextView)


                productLayout.addView(productDetailsLayout)

                // Add bookmark button to layout
                val bookmarkButton = Button(this)
                bookmarkButton.layoutParams = LinearLayout.LayoutParams(
                    70,
                    70
                )

                // Load bookmarks from SharedPreferences and update the bookmark list and status map
                val bookmarkedList = loadBookmarks()
                val bookmarkStatusMap = bookmarkedList.associateBy({ it.name }, { true }).toMutableMap()

                // Check if the product is already bookmarked
                val isBookmarked = bookmarkStatusMap.getOrElse(bookmarkedProduct.name, { false })
                if (isBookmarked) {
                    bookmarkButton.setBackgroundResource(R.drawable.baseline_bookmark_24)
                } else {
                    bookmarkButton.setBackgroundResource(R.drawable.baseline_bookmark_border_24)
                }

                // Toggle the bookmarked status
                val IsBookmarked = !isBookmarked

                bookmarkButton.setOnClickListener {
                    // Load bookmarks from SharedPreferences and update the bookmark list and status map
                    val bookmarkedList = loadBookmarks()
                    val bookmarkStatusMap = bookmarkedList.associateBy({ it.name }, { true }).toMutableMap()

                    // Check if the product is already bookmarked
                    val isBookmarked = bookmarkStatusMap.getOrDefault(bookmarkedProduct.name, false)
                    if (isBookmarked) {
                        bookmarkButton.setBackgroundResource(R.drawable.baseline_bookmark_24)
                    } else {
                        bookmarkButton.setBackgroundResource(R.drawable.baseline_bookmark_border_24)
                    }

                    // Toggle the bookmarked status
                    val newIsBookmarked = !isBookmarked
                    // Change the bookmark
                    if (newIsBookmarked) {
                        bookmarkButton.setBackgroundResource(R.drawable.baseline_bookmark_24)
                        val bookmarkedProduct = BookmarkedProduct(bookmarkedProduct.name, bookmarkedProduct.pass)
                        bookmarkedList.add(bookmarkedProduct)
                        bookmarkStatusMap[bookmarkedProduct.name] = true
                        // Save the updated bookmarked items to SharedPreferences
                        saveBookmarks(bookmarkedList)
                    } else {
                        bookmarkButton.setBackgroundResource(R.drawable.baseline_bookmark_border_24)
                        val indexToRemove = bookmarkedList.indexOfFirst { it.name == bookmarkedProduct.name }
                        if (indexToRemove == -1) {
                            println("Product ${bookmarkedProduct.name} not found in list.")
                        } else {
                            try {
                                val removedProduct = bookmarkedList.removeAt(indexToRemove)
                                bookmarkStatusMap[removedProduct.name] = false
                                println("Product ${removedProduct.name} removed from list.")
                                // Debugging output:
                                println("List after removal: ${bookmarkedList.joinToString(", ") { it.name }}")
                                removeBookmark(bookmarkedList)
                            } catch (e: Exception) {
                                println("Error removing product ${bookmarkedProduct.name}: ${e.message}")
                            }
                        }
                    }

                    // Show a message indicating that the item has been bookmarked or unbookmarked
                    val message = "Item unbookmarked"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }

                productLayout.addView(bookmarkButton)

                // Add OnClickListener to product layout
                productLayout.setOnClickListener {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("productName", bookmarkedProduct.name)
                    intent.putExtra("productPass", bookmarkedProduct.pass)
                    startActivity(intent)
                }

                bookmarksParentLayout.addView(productLayout)

            }
        } else {
            // If the bookmarkedList is null or empty, hide the bookmarksParentLayout and show the empty text view
            bookmarksParentLayout.visibility = View.GONE
            emptyTextView.visibility = View.VISIBLE
        }


    }

    private fun loadBookmarks(): ArrayList<BookmarkedProduct> {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val bookmarksJson = sharedPreferences.getString("bookmarks", null)
        return if (bookmarksJson != null) {
            Gson().fromJson(bookmarksJson, object : TypeToken<ArrayList<BookmarkedProduct>>() {}.type)
        } else {
            ArrayList()
        }
    }

    private fun saveBookmarks(bookmarkedList: MutableList<BookmarkedProduct>) {
        val jsonString = Gson().toJson(bookmarkedList)

        // Load the existing bookmarks from SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val existingJsonString = sharedPreferences.getString("bookmarks", null)

        // If there are existing bookmarks, update them with any new ones
        if (existingJsonString != null) {
            val existingBookmarks = Gson().fromJson(existingJsonString, Array<BookmarkedProduct>::class.java).toMutableList()
            for (newBookmark in bookmarkedList) {
                val existingBookmarkIndex = existingBookmarks.indexOfFirst { it.name == newBookmark.name }
                if (existingBookmarkIndex >= 0) {
                    val existingBookmark = existingBookmarks[existingBookmarkIndex]
                    val updatedBookmark = BookmarkedProduct(
                        existingBookmark.name,
                        newBookmark.pass
                    )
                    existingBookmarks[existingBookmarkIndex] = updatedBookmark
                } else {
                    existingBookmarks.add(newBookmark)
                }
            }
            val updatedJsonString = Gson().toJson(existingBookmarks)
            sharedPreferences.edit().putString("bookmarks", updatedJsonString).apply()
        } else {
            // Otherwise, just save the new bookmarks to SharedPreferences
            sharedPreferences.edit().putString("bookmarks", jsonString).apply()
        }
    }

    private fun removeBookmark(bookmarkedList: MutableList<BookmarkedProduct>) {
        val jsonString = Gson().toJson(bookmarkedList)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        sharedPreferences.edit().putString("bookmarks", jsonString).apply()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}







