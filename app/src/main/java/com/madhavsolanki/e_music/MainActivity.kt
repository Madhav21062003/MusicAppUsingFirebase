package com.madhavsolanki.e_music

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.madhavsolanki.e_music.adapter.CategoryAdapter
import com.madhavsolanki.e_music.adapter.SectionSongListAdapter
import com.madhavsolanki.e_music.databinding.ActivityMainBinding
import com.madhavsolanki.e_music.models.CategoryModel
import com.madhavsolanki.e_music.models.SongModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getCategories()

        setupSections("section_1", binding.section1MainLayout,binding.section1Title, binding.section1RecyclerView)
        setupSections("section_2", binding.section2MainLayout,binding.section2Title, binding.section2RecyclerView)
        setupSections("section_3", binding.section3MainLayout,binding.section3Title, binding.section3RecyclerView)
        setupMostlyPlayed("mostly_played", binding.mostlyPlayedMainLayout,binding.mostlyPlayedTitle, binding.mostlyPlayedRecyclerView)


    }

    override fun onResume() {
        super.onResume()
        showPlayerView()
    }


    fun showPlayerView(){
        binding.playerViewBottom.setOnClickListener {
            startActivity(Intent(this@MainActivity,PlayerActivity::class.java))
        }
        MyExoplayer.getCurrentSong()?.let{
            binding.playerViewBottom.visibility = View.VISIBLE
            binding.songTitleTextView.text = "Now Playing: "+it.title

            Glide.with(binding.songCoverImageView).load(it.coverUrl)
                .apply(
                    RequestOptions().transform(RoundedCorners(32))
                )
                .into(binding.songCoverImageView)

        }?:run {
            binding.playerViewBottom.visibility = View.GONE
        }
    }

    fun getCategories(){
        FirebaseFirestore.getInstance().collection("category").get()
            .addOnSuccessListener {
                val categoryList = it.toObjects(CategoryModel::class.java)
                setupCategoryRecyclerView(categoryList)
            }
    }

    fun setupCategoryRecyclerView(categoryList: List<CategoryModel>){
        categoryAdapter = CategoryAdapter(categoryList)
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }


    // Sections
   fun setupSections(id:String, mainLayout:RelativeLayout, titleView: TextView, recyclerView: RecyclerView){
       FirebaseFirestore.getInstance().collection("sections")
           .document(id)
           .get().addOnSuccessListener {
               val section = it.toObject(CategoryModel::class.java)
               section?.apply {
                   mainLayout.visibility = View.VISIBLE
                   titleView.text = name
                   recyclerView.layoutManager = LinearLayoutManager(this@MainActivity,LinearLayoutManager.HORIZONTAL,false)
                   recyclerView.adapter = SectionSongListAdapter(songs)

                  mainLayout.setOnClickListener {
                       SongsListActivity.category = section
                       startActivity(Intent(this@MainActivity, SongsListActivity::class.java))
                   }
               }
           }
   }


    fun setupMostlyPlayed(id:String, mainLayout:RelativeLayout, titleView: TextView, recyclerView: RecyclerView){
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener {

                // get most played songs
                FirebaseFirestore.getInstance().collection("songs")
                    .orderBy("count",Query.Direction.DESCENDING)
                    .limit(5)
                    .get().addOnSuccessListener{songListSnapshot->
                        val  songModelList = songListSnapshot.toObjects<SongModel>()
                        val  songsIdList = songModelList.map {
                            it.id
                        }.toList()

                        val section = it.toObject(CategoryModel::class.java)
                        section?.apply {
                            section.songs = songsIdList
                            mainLayout.visibility = View.VISIBLE
                            titleView.text = name
                            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity,LinearLayoutManager.HORIZONTAL,false)
                            recyclerView.adapter = SectionSongListAdapter(songs)

                            mainLayout.setOnClickListener {
                                SongsListActivity.category = section
                                startActivity(Intent(this@MainActivity, SongsListActivity::class.java))
                            }
                        }

                    }

            }
    }
}