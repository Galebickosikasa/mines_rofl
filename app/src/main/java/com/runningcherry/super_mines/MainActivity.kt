package com.runningcherry.super_mines

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.util.*
import kotlin.collections.HashSet
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var grid: LinearLayout
    private val row = 12
    private val col = 9
    private val fill = 0.2
    private var flags : Array<IntArray> = Array (row) { IntArray(col) }
    private var mines : Array<IntArray> = Array (row) { IntArray(col) }
    private lateinit var buttons : Array<Array<Button>>
    private lateinit var last : TextView
    private var now = 0

    private val random = Random

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        grid = findViewById (R.id.grid)
        buttons = Array (row) { Array (col) { Button (this) } }
        for (i in 0 until row) {
            val linearLayout = LinearLayout (this)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            linearLayout.layoutParams = LinearLayout.LayoutParams (LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            linearLayout.weightSum = col.toFloat ()
            for (j in 0 until col) {
                val btn = Button (this)
                btn.layoutParams = LinearLayout.LayoutParams (LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                btn.setBackgroundResource (R.drawable.mybutton)

                btn.setOnClickListener {
                    if (flags[i][j] == 2) return@setOnClickListener
                    if (flags[i][j] == 0) {
                        btn.setBackgroundResource(R.drawable.ic_baseline_flag_24)
                        flags[i][j] = 1
                        --now
                    } else {
                        btn.setBackgroundResource (R.drawable.mybutton)
                        flags[i][j] = 0
                        ++now
                    }
                    last.text = "$now Mines left"
                }
                btn.setOnLongClickListener {
                    if (flags[i][j] != 0) return@setOnLongClickListener true
                    if (mines[i][j] == 1) {
                        boom ()
                        return@setOnLongClickListener true
                    }
                    flags[i][j] = 2
                    bfs (i, j)
                    true
                }
                buttons[i][j] = btn
                linearLayout.addView (btn)
            }
            grid.addView (linearLayout)
        }
        last = TextView (this)
        last.layoutParams = LinearLayout.LayoutParams (LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        last.gravity = Gravity.CENTER
        last.setTextColor (getColor (R.color.black))
        grid.addView (last)
        val resetBtn = Button (this)
        resetBtn.layoutParams = LinearLayout.LayoutParams (LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        resetBtn.text = "RESET"
        resetBtn.setOnClickListener {
            for (i in 0 until row) {
                for (j in 0 until col) {
                    buttons[i][j].setBackgroundResource (R.drawable.mybutton)
                    buttons[i][j].text = ""
                    mines[i][j] = 0
                    flags[i][j] = 0
                }
            }
            genMatrix()
        }
        grid.addView (resetBtn)
        genMatrix ()
    }

    private fun boom () {
        for (i in 0 until row) {
            for (j in 0 until col) {
                if (mines[i][j] == 1) {
                    buttons[i][j].background = getDrawable (R.drawable.mine)
                }
            }
        }
        Toast.makeText (this, "NOOB", Toast.LENGTH_SHORT).show ()

    }

    private fun genMatrix () {
        var cnt = (row * col * fill).toInt ()
        now = cnt
        last.text = "$now Mines left"
        while (cnt > 0) {
            val i = random.nextInt (0, row - 1)
            val j = random.nextInt (0, col - 1)
            if (mines[i][j] == 0) {
                mines[i][j] = 1
                --cnt
            }
        }
    }

    private fun corr (i : Int, j : Int) : Boolean {
        return i >= 0 && j >= 0 && i < row && j < col
    }

    private fun calc (i : Int, j : Int) : Int {
        var cnt = 0
        for (di in -1 until 2) {
            for (dj in -1 until 2) {
                if (di == 0 && dj == 0) continue
                val ii = i + di
                val jj = j + dj
                if (corr (ii, jj)) {
                    cnt += mines[ii][jj]
                }
            }
        }
        return cnt
    }

    private fun bfs (si : Int, sj : Int) {
        val a : Queue<Pair<Int, Int>> = LinkedList()
        val used : HashSet <Pair <Int, Int>> = hashSetOf()
        a.add (Pair (si, sj))
        used.add (Pair (si, sj))
        while (!a.isEmpty()) {
            val ptt = a.peek ()
            val (i, j) = ptt!!
            a.remove()
            val cnt = calc (i, j)
            if (cnt == 0) {
                for (di in -1 until 2) {
                    for (dj in -1 until 2) {
                        if (used.contains (Pair (i + di, j + dj))) continue
                        val ii = i + di
                        val jj = j + dj
                        if (corr (ii, jj) && mines[ii][jj] == 0) {
                            a.add (Pair (ii, jj))
                            used.add (Pair (ii, jj))
                        }
                    }
                }
            }
            buttons[i][j].text = cnt.toString()
            flags[i][j] = 2
            for (di in -1 until 2) {
                for (dj in -1 until 2) {
                    if (used.contains (Pair (i + di, j + dj))) continue
                    val ii = i + di
                    val jj = j + dj
                    if (corr (ii, jj) && calc (ii, jj) == 0 && mines[ii][jj] == 0) {
                        a.add (Pair (ii, jj))
                        used.add (Pair (ii, jj))
                    }
                }
            }
        }
    }
}