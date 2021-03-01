package com.muglzm.opengl

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muglzm.opengl.util.Util

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Util.context = applicationContext

        val samplesList = findViewById<RecyclerView>(R.id.list)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        samplesList.layoutManager = layoutManager
        samplesList.adapter = MyAdapter()
    }

    inner class MyAdapter:RecyclerView.Adapter<VH>(){

        private val samplesNames = arrayOf(
            resources.getString(R.string.sample_0),
            resources.getString(R.string.sample_1),
            resources.getString(R.string.sample_2),
            resources.getString(R.string.sample_3),
            resources.getString(R.string.sample_4),
            resources.getString(R.string.sample_5),
            resources.getString(R.string.sample_6),
            resources.getString(R.string.sample_7),
            resources.getString(R.string.sample_8),
            resources.getString(R.string.sample_9),
            resources.getString(R.string.sample_10)
        )
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {

            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_sample_list_item,parent,false)
            return VH(view)
        }

        override fun getItemCount(): Int {
            return samplesNames.size
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            //Get and set sample name
            holder.button.text = samplesNames[position]
            holder.button.setOnClickListener{
                val intent = Intent(this@MainActivity,MySampleActivity::class.java)
                intent.putExtra(GlobalConstants.KEY_SAMPLE_INDEX,position)
                intent.putExtra(GlobalConstants.KEY_SAMPLE_NAME,samplesNames[position])
                this@MainActivity.startActivity(intent)

            }
        }
    }

    inner class VH(itemView: View):RecyclerView.ViewHolder(itemView){
        var button:Button = itemView.findViewById(R.id.button)
    }
}